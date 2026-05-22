package com.aquacomunidad.backend.features.caso.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquacomunidad.backend.common.enums.EstadoCaso;
import com.aquacomunidad.backend.common.enums.EstadoReporte;
import com.aquacomunidad.backend.exception.ExcepcionApi;
import com.aquacomunidad.backend.features.caso.dto.CasoSolicitudDto;
import com.aquacomunidad.backend.features.caso.dto.CasoRespuestaDto;
import com.aquacomunidad.backend.features.caso.dto.CasoActualizacionDto;
import com.aquacomunidad.backend.features.caso.entity.CasoEntidad;
import com.aquacomunidad.backend.features.caso.mapper.CasoMapeador;
import com.aquacomunidad.backend.features.caso.repository.CasoRepositorio;
import com.aquacomunidad.backend.features.caso.service.CasoServicio;
import com.aquacomunidad.backend.features.historial.service.HistorialEstadoServicio;
import com.aquacomunidad.backend.features.reporte.entity.ReporteEntidad;
import com.aquacomunidad.backend.features.reporte.repository.ReporteRepositorio;
import com.aquacomunidad.backend.features.usuario.entity.UsuarioEntidad;
import com.aquacomunidad.backend.features.usuario.repository.UsuarioRepositorio;
import com.aquacomunidad.backend.security.SeguridadContextoUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CasoServicioImpl implements CasoServicio {

  private final CasoRepositorio casoRepositorio;
  private final ReporteRepositorio reporteRepositorio;
  private final UsuarioRepositorio usuarioRepositorio;
  private final CasoMapeador casoMapeador;
  private final HistorialEstadoServicio historialEstadoServicio;

  @Override
  @Transactional
  public CasoRespuestaDto crear(CasoSolicitudDto request) {
    UsuarioEntidad actor = SeguridadContextoUtil.usuarioAutenticado();

    ReporteEntidad reporte = reporteRepositorio.findById(request.getReporteId())
        .orElseThrow(() -> new ExcepcionApi(HttpStatus.NOT_FOUND, "Reporte no encontrado"));
    UsuarioEntidad responsable = usuarioRepositorio.findById(request.getResponsableId())
        .orElseThrow(() -> new ExcepcionApi(HttpStatus.NOT_FOUND, "Responsable no encontrado"));

    CasoEntidad entity = new CasoEntidad();
    entity.setReporteOrigen(reporte);
    entity.setResponsable(responsable);
    entity.setPrioridad(request.getPrioridad());

    EstadoReporte estadoAnteriorReporte = reporte.getEstado();
    reporte.setEstado(EstadoReporte.EN_PROCESO);
    reporteRepositorio.save(reporte);

    CasoEntidad saved = casoRepositorio.save(entity);

    historialEstadoServicio.registrarCambioReporte(
        reporte,
        estadoAnteriorReporte,
        EstadoReporte.EN_PROCESO,
        "Derivado a caso operativo",
        actor);

    historialEstadoServicio.registrarCambioCaso(
        saved,
        null,
        saved.getEstado(),
        "Creacion de caso operativo",
        actor);

    return casoMapeador.aRespuesta(saved);
  }

  @Override
  @Transactional
  public CasoRespuestaDto actualizarEstado(Long id, CasoActualizacionDto request) {
    UsuarioEntidad actor = SeguridadContextoUtil.usuarioAutenticado();
    CasoEntidad entity = casoRepositorio.findById(id)
        .orElseThrow(() -> new ExcepcionApi(HttpStatus.NOT_FOUND, "Caso no encontrado"));

    EstadoCaso estadoAnteriorCaso = entity.getEstado();
    EstadoReporte estadoAnteriorReporte = entity.getReporteOrigen().getEstado();

    if (request.getEstado() == EstadoCaso.RESUELTO
        && (request.getEvidenciaCierre() == null || request.getEvidenciaCierre().trim().isEmpty())) {
      throw new ExcepcionApi(HttpStatus.BAD_REQUEST, "Para cerrar como RESUELTO debe enviar evidencia_cierre");
    }

    entity.setEstado(request.getEstado());
    entity.setObservaciones(request.getObservaciones());
    entity.setEvidenciaCierre(request.getEvidenciaCierre());

    if (request.getEstado() == EstadoCaso.RESUELTO) {
      entity.setFechaCierre(LocalDateTime.now());
      entity.getReporteOrigen().setEstado(EstadoReporte.RESUELTO);
    }

    CasoEntidad saved = casoRepositorio.save(entity);

    historialEstadoServicio.registrarCambioCaso(
        saved,
        estadoAnteriorCaso,
        saved.getEstado(),
        request.getObservaciones(),
        actor);

    if (saved.getReporteOrigen().getEstado() != estadoAnteriorReporte) {
      historialEstadoServicio.registrarCambioReporte(
          saved.getReporteOrigen(),
          estadoAnteriorReporte,
          saved.getReporteOrigen().getEstado(),
          request.getObservaciones(),
          actor);
    }

    return casoMapeador.aRespuesta(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public List<CasoRespuestaDto> listarTodos() {
    return casoRepositorio.findAll().stream().map(casoMapeador::aRespuesta).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<CasoRespuestaDto> listarPorResponsable(Long responsableId) {
    return casoRepositorio.findByResponsableId(responsableId).stream().map(casoMapeador::aRespuesta).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<CasoRespuestaDto> listarPorEstado(EstadoCaso estado) {
    return casoRepositorio.findByEstado(estado).stream().map(casoMapeador::aRespuesta).toList();
  }
}
