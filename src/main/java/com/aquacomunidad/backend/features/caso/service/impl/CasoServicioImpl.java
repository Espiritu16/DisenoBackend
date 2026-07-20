package com.aquacomunidad.backend.features.caso.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquacomunidad.backend.common.enums.EstadoCaso;
import com.aquacomunidad.backend.common.enums.EstadoReporte;
import com.aquacomunidad.backend.common.enums.EstadoUsuario;
import com.aquacomunidad.backend.common.enums.RolUsuario;
import com.aquacomunidad.backend.exception.ExcepcionApi;
import com.aquacomunidad.backend.features.caso.dto.CasoSolicitudDto;
import com.aquacomunidad.backend.features.caso.dto.CasoRespuestaDto;
import com.aquacomunidad.backend.features.caso.dto.CasoActualizacionDto;
import com.aquacomunidad.backend.features.caso.entity.CasoEntidad;
import com.aquacomunidad.backend.features.caso.entity.CasoEvidenciaEntidad;
import com.aquacomunidad.backend.features.caso.mapper.CasoMapeador;
import com.aquacomunidad.backend.features.caso.repository.CasoEvidenciaRepositorio;
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
  private static final Map<EstadoCaso, Set<EstadoCaso>> TRANSICIONES_PERMITIDAS = Map.of(
      EstadoCaso.EN_PROCESO, Set.of(EstadoCaso.EN_PROCESO, EstadoCaso.ESCALADO, EstadoCaso.RESUELTO, EstadoCaso.RECHAZADO),
      EstadoCaso.ESCALADO, Set.of(EstadoCaso.ESCALADO, EstadoCaso.EN_PROCESO, EstadoCaso.RESUELTO, EstadoCaso.RECHAZADO),
      EstadoCaso.RESUELTO, Set.of(EstadoCaso.RESUELTO),
      EstadoCaso.RECHAZADO, Set.of(EstadoCaso.RECHAZADO));

  private final CasoRepositorio casoRepositorio;
  private final ReporteRepositorio reporteRepositorio;
  private final UsuarioRepositorio usuarioRepositorio;
  private final CasoEvidenciaRepositorio casoEvidenciaRepositorio;
  private final CasoMapeador casoMapeador;
  private final HistorialEstadoServicio historialEstadoServicio;

  @Override
  @Transactional
  public CasoRespuestaDto crear(CasoSolicitudDto request) {
    UsuarioEntidad actor = SeguridadContextoUtil.usuarioAutenticado();
    if (actor.getRol() == RolUsuario.OPERADOR && !actor.getId().equals(request.getResponsableId())) {
      throw new ExcepcionApi(HttpStatus.FORBIDDEN, "El operador solo puede autoasignarse casos");
    }

    ReporteEntidad reporte = reporteRepositorio.findById(request.getReporteId())
        .orElseThrow(() -> new ExcepcionApi(HttpStatus.NOT_FOUND, "Reporte no encontrado"));
    if (reporte.getEstado() != EstadoReporte.PENDIENTE) {
      throw new ExcepcionApi(HttpStatus.BAD_REQUEST, "Solo reportes PENDIENTE pueden derivarse a caso");
    }
    if (casoRepositorio.existsByReporteOrigenId(request.getReporteId())) {
      throw new ExcepcionApi(HttpStatus.CONFLICT, "El reporte ya tiene un caso operativo");
    }

    UsuarioEntidad responsable = usuarioRepositorio.findById(request.getResponsableId())
        .orElseThrow(() -> new ExcepcionApi(HttpStatus.NOT_FOUND, "Responsable no encontrado"));
    if (responsable.getRol() != RolUsuario.OPERADOR) {
      throw new ExcepcionApi(HttpStatus.BAD_REQUEST, "El responsable asignado debe tener rol OPERADOR");
    }
    if (responsable.getEstado() != EstadoUsuario.ACTIVO) {
      throw new ExcepcionApi(HttpStatus.BAD_REQUEST, "El responsable asignado debe estar ACTIVO");
    }

    CasoEntidad entity = new CasoEntidad();
    entity.setReporteOrigen(reporte);
    entity.setResponsable(responsable);
    entity.setCreadoPor(actor);
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
    validarAutorizacionCaso(actor, entity);
    validarTransicionEstado(entity.getEstado(), request.getEstado());

    EstadoCaso estadoAnteriorCaso = entity.getEstado();
    EstadoReporte estadoAnteriorReporte = entity.getReporteOrigen().getEstado();

    List<String> evidencias = evidenciasSolicitadas(request);
    if (request.getEstado() == EstadoCaso.RESUELTO && evidencias.isEmpty()
        && (entity.getEvidenciaCierre() == null || entity.getEvidenciaCierre().trim().isEmpty())) {
      throw new ExcepcionApi(HttpStatus.BAD_REQUEST, "Para cerrar como RESUELTO debe enviar evidencia_cierre");
    }

    entity.setEstado(request.getEstado());
    entity.setObservaciones(request.getObservaciones());
    if (!evidencias.isEmpty()) {
      entity.setEvidenciaCierre(evidencias.get(0));
    }

    if (request.getEstado() == EstadoCaso.RESUELTO) {
      entity.setFechaCierre(LocalDateTime.now());
    }
    entity.getReporteOrigen().setEstado(estadoReporteSegunEstadoCaso(request.getEstado()));

    CasoEntidad saved = casoRepositorio.save(entity);
    guardarEvidencias(saved, evidencias);

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

  private List<String> evidenciasSolicitadas(CasoActualizacionDto request) {
    if (request.getEvidenciaCierreUrls() != null && !request.getEvidenciaCierreUrls().isEmpty()) {
      return request.getEvidenciaCierreUrls().stream()
          .filter(url -> url != null && !url.isBlank())
          .map(String::trim)
          .toList();
    }
    if (request.getEvidenciaCierre() != null && !request.getEvidenciaCierre().isBlank()) {
      return List.of(request.getEvidenciaCierre().trim());
    }
    return List.of();
  }

  private void guardarEvidencias(CasoEntidad caso, List<String> urls) {
    int baseOrden = caso.getEvidencias().size();
    for (int i = 0; i < urls.size(); i++) {
      CasoEvidenciaEntidad evidencia = new CasoEvidenciaEntidad();
      evidencia.setCaso(caso);
      evidencia.setUrl(urls.get(i));
      evidencia.setOrden(baseOrden + i);
      casoEvidenciaRepositorio.save(evidencia);
      caso.getEvidencias().add(evidencia);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<CasoRespuestaDto> listarTodos() {
    UsuarioEntidad actor = SeguridadContextoUtil.usuarioAutenticado();
    if (actor.getRol() == RolUsuario.OPERADOR) {
      return casoRepositorio.findByResponsableIdOrderByFechaAsignacionDesc(actor.getId()).stream()
          .map(casoMapeador::aRespuesta)
          .toList();
    }
    return casoRepositorio.findAllByOrderByFechaAsignacionDesc().stream().map(casoMapeador::aRespuesta).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<CasoRespuestaDto> listarPorResponsable(Long responsableId) {
    UsuarioEntidad actor = SeguridadContextoUtil.usuarioAutenticado();
    if (actor.getRol() == RolUsuario.OPERADOR && !actor.getId().equals(responsableId)) {
      throw new ExcepcionApi(HttpStatus.FORBIDDEN, "No puede ver casos de otro responsable");
    }
    return casoRepositorio.findByResponsableIdOrderByFechaAsignacionDesc(responsableId).stream()
        .map(casoMapeador::aRespuesta)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<CasoRespuestaDto> listarPorEstado(EstadoCaso estado) {
    UsuarioEntidad actor = SeguridadContextoUtil.usuarioAutenticado();
    if (actor.getRol() == RolUsuario.OPERADOR) {
      return casoRepositorio.findByResponsableIdAndEstadoOrderByFechaAsignacionDesc(actor.getId(), estado).stream()
          .map(casoMapeador::aRespuesta)
          .toList();
    }
    return casoRepositorio.findByEstadoOrderByFechaAsignacionDesc(estado).stream().map(casoMapeador::aRespuesta).toList();
  }

  private void validarAutorizacionCaso(UsuarioEntidad actor, CasoEntidad caso) {
    if (actor.getRol() == RolUsuario.OPERADOR && !actor.getId().equals(caso.getResponsable().getId())) {
      throw new ExcepcionApi(HttpStatus.FORBIDDEN, "No puede actualizar un caso no asignado");
    }
  }

  private void validarTransicionEstado(EstadoCaso actual, EstadoCaso nuevo) {
    Set<EstadoCaso> permitidos = TRANSICIONES_PERMITIDAS.getOrDefault(actual, Set.of(actual));
    if (!permitidos.contains(nuevo)) {
      throw new ExcepcionApi(HttpStatus.BAD_REQUEST, "Transicion de estado no permitida");
    }
  }

  private EstadoReporte estadoReporteSegunEstadoCaso(EstadoCaso estadoCaso) {
    return switch (estadoCaso) {
      case EN_PROCESO -> EstadoReporte.EN_PROCESO;
      case RESUELTO -> EstadoReporte.RESUELTO;
      case ESCALADO -> EstadoReporte.ESCALADO;
      case RECHAZADO -> EstadoReporte.RECHAZADO;
    };
  }
}
