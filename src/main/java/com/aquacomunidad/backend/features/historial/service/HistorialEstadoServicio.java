package com.aquacomunidad.backend.features.historial.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquacomunidad.backend.common.enums.EstadoCaso;
import com.aquacomunidad.backend.common.enums.EstadoReporte;
import com.aquacomunidad.backend.features.caso.entity.CasoEntidad;
import com.aquacomunidad.backend.features.historial.dto.DetalleTrazabilidadReporteDto;
import com.aquacomunidad.backend.features.historial.dto.HistorialCambioDto;
import com.aquacomunidad.backend.features.historial.entity.HistorialEstadoCasoEntidad;
import com.aquacomunidad.backend.features.historial.entity.HistorialEstadoReporteEntidad;
import com.aquacomunidad.backend.features.historial.repository.HistorialEstadoCasoRepositorio;
import com.aquacomunidad.backend.features.historial.repository.HistorialEstadoReporteRepositorio;
import com.aquacomunidad.backend.features.reporte.entity.ReporteEntidad;
import com.aquacomunidad.backend.features.usuario.entity.UsuarioEntidad;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistorialEstadoServicio {

  private final HistorialEstadoReporteRepositorio historialEstadoReporteRepositorio;
  private final HistorialEstadoCasoRepositorio historialEstadoCasoRepositorio;

  @Transactional
  public void registrarCambioReporte(
      ReporteEntidad reporte,
      EstadoReporte estadoAnterior,
      EstadoReporte estadoNuevo,
      String observacion,
      UsuarioEntidad cambiadoPor) {
    HistorialEstadoReporteEntidad historial = new HistorialEstadoReporteEntidad();
    historial.setReporte(reporte);
    historial.setEstadoAnterior(estadoAnterior);
    historial.setEstadoNuevo(estadoNuevo);
    historial.setObservacion(observacion);
    historial.setCambiadoPor(cambiadoPor);
    historialEstadoReporteRepositorio.save(historial);
  }

  @Transactional
  public void registrarCambioCaso(
      CasoEntidad caso,
      EstadoCaso estadoAnterior,
      EstadoCaso estadoNuevo,
      String observacion,
      UsuarioEntidad cambiadoPor) {
    HistorialEstadoCasoEntidad historial = new HistorialEstadoCasoEntidad();
    historial.setCaso(caso);
    historial.setEstadoAnterior(estadoAnterior);
    historial.setEstadoNuevo(estadoNuevo);
    historial.setObservacion(observacion);
    historial.setCambiadoPor(cambiadoPor);
    historialEstadoCasoRepositorio.save(historial);
  }

  @Transactional(readOnly = true)
  public DetalleTrazabilidadReporteDto obtenerDetalle(Long reporteId, Long casoId) {
    List<HistorialCambioDto> historialReporte = historialEstadoReporteRepositorio
        .findByReporteIdOrderByFechaCambioAsc(reporteId)
        .stream()
        .map(h -> HistorialCambioDto.builder()
            .tipo("REPORTE")
            .estadoAnterior(h.getEstadoAnterior() == null ? null : h.getEstadoAnterior().name())
            .estadoNuevo(h.getEstadoNuevo().name())
            .observacion(h.getObservacion())
            .cambiadoPor(h.getCambiadoPor().getId())
            .fechaCambio(h.getFechaCambio())
            .build())
        .toList();

    List<HistorialCambioDto> historialCaso = casoId == null
        ? List.of()
        : historialEstadoCasoRepositorio.findByCasoIdOrderByFechaCambioAsc(casoId)
            .stream()
            .map(h -> HistorialCambioDto.builder()
                .tipo("CASO")
                .estadoAnterior(h.getEstadoAnterior() == null ? null : h.getEstadoAnterior().name())
                .estadoNuevo(h.getEstadoNuevo().name())
                .observacion(h.getObservacion())
                .cambiadoPor(h.getCambiadoPor().getId())
                .fechaCambio(h.getFechaCambio())
                .build())
            .toList();

    return DetalleTrazabilidadReporteDto.builder()
        .reporteId(reporteId)
        .casoId(casoId)
        .historialReporte(historialReporte)
        .historialCaso(historialCaso)
        .build();
  }
}
