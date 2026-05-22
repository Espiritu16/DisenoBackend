package com.aquacomunidad.backend.features.reporte.service.impl;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquacomunidad.backend.common.enums.EstadoReporte;
import com.aquacomunidad.backend.exception.ExcepcionApi;
import com.aquacomunidad.backend.features.caso.entity.CasoEntidad;
import com.aquacomunidad.backend.features.caso.repository.CasoRepositorio;
import com.aquacomunidad.backend.features.historial.dto.DetalleTrazabilidadReporteDto;
import com.aquacomunidad.backend.features.historial.service.HistorialEstadoServicio;
import com.aquacomunidad.backend.features.reporte.dto.ReporteSolicitudDto;
import com.aquacomunidad.backend.features.reporte.dto.ReporteRespuestaDto;
import com.aquacomunidad.backend.features.reporte.entity.ReporteEntidad;
import com.aquacomunidad.backend.features.reporte.mapper.ReporteMapeador;
import com.aquacomunidad.backend.features.reporte.repository.ReporteRepositorio;
import com.aquacomunidad.backend.features.reporte.service.ReporteServicio;
import com.aquacomunidad.backend.features.usuario.entity.UsuarioEntidad;
import com.aquacomunidad.backend.features.usuario.repository.UsuarioRepositorio;
import com.aquacomunidad.backend.security.SeguridadContextoUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReporteServicioImpl implements ReporteServicio {

  private final ReporteRepositorio reporteRepositorio;
  private final UsuarioRepositorio usuarioRepositorio;
  private final CasoRepositorio casoRepositorio;
  private final ReporteMapeador reporteMapeador;
  private final HistorialEstadoServicio historialEstadoServicio;

  @Override
  @Transactional
  public ReporteRespuestaDto crear(ReporteSolicitudDto request) {
    Long usuarioIdAutenticado = SeguridadContextoUtil.idUsuarioAutenticado();
    UsuarioEntidad usuario = usuarioRepositorio.findById(usuarioIdAutenticado)
        .orElseThrow(() -> new ExcepcionApi(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

    ReporteEntidad entidad = reporteMapeador.toEntidad(request, usuario);
    boolean posibleDuplicado = reporteRepositorio.existePosibleDuplicado(
        request.getTipo(),
        request.getZona(),
        LocalDateTime.now().minusHours(24));
    entidad.setPosibleDuplicado(posibleDuplicado);

    ReporteEntidad saved = reporteRepositorio.save(entidad);

    historialEstadoServicio.registrarCambioReporte(
        saved,
        null,
        saved.getEstado(),
        "Creacion de reporte",
        usuario);

    return reporteMapeador.aRespuesta(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReporteRespuestaDto> listarTodos() {
    return reporteRepositorio.findAll().stream().map(reporteMapeador::aRespuesta).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReporteRespuestaDto> listarPorUsuario(Long usuarioId) {
    return reporteRepositorio.findByUsuarioId(usuarioId).stream().map(reporteMapeador::aRespuesta).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReporteRespuestaDto> listarPorEstado(EstadoReporte estado) {
    return reporteRepositorio.findByEstado(estado).stream().map(reporteMapeador::aRespuesta).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReporteRespuestaDto> listarMisReportes(Long usuarioId) {
    return reporteRepositorio.findByUsuarioId(usuarioId).stream().map(reporteMapeador::aRespuesta).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public DetalleTrazabilidadReporteDto obtenerDetalleTrazabilidad(Long reporteId, Long usuarioId, boolean esCiudadano) {
    ReporteEntidad reporte = reporteRepositorio.findById(reporteId)
        .orElseThrow(() -> new ExcepcionApi(HttpStatus.NOT_FOUND, "Reporte no encontrado"));

    if (esCiudadano && !reporte.getUsuario().getId().equals(usuarioId)) {
      throw new ExcepcionApi(HttpStatus.FORBIDDEN, "No puede ver trazabilidad de reportes de otro usuario");
    }

    CasoEntidad caso = casoRepositorio.findByReporteOrigenId(reporteId).orElse(null);
    Long casoId = caso == null ? null : caso.getId();
    return historialEstadoServicio.obtenerDetalle(reporteId, casoId);
  }
}
