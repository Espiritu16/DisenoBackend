package com.aquacomunidad.backend.features.reporte.service;

import java.util.List;
import java.time.LocalDateTime;

import com.aquacomunidad.backend.common.enums.EstadoReporte;
import com.aquacomunidad.backend.features.historial.dto.DetalleTrazabilidadReporteDto;
import com.aquacomunidad.backend.features.reporte.dto.ReporteSolicitudDto;
import com.aquacomunidad.backend.features.reporte.dto.ReporteRespuestaDto;

public interface ReporteServicio {
  ReporteRespuestaDto crear(ReporteSolicitudDto request);

  List<ReporteRespuestaDto> listarTodos();

  List<ReporteRespuestaDto> listarPorUsuario(Long usuarioId);

  List<ReporteRespuestaDto> listarPorEstado(EstadoReporte estado);

  List<ReporteRespuestaDto> listarConFiltros(
      Long usuarioId,
      EstadoReporte estado,
      String tipo,
      String zona,
      LocalDateTime fechaDesde,
      LocalDateTime fechaHasta);

  List<ReporteRespuestaDto> listarMisReportes(Long usuarioId);

  DetalleTrazabilidadReporteDto obtenerDetalleTrazabilidad(Long reporteId, Long usuarioId, boolean esCiudadano);
}
