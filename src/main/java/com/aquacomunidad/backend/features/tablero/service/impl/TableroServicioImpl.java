package com.aquacomunidad.backend.features.tablero.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquacomunidad.backend.common.enums.EstadoCaso;
import com.aquacomunidad.backend.common.enums.EstadoReporte;
import com.aquacomunidad.backend.features.caso.repository.CasoRepositorio;
import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto;
import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto.ActividadSemanalDto;
import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto.ReportePorZonaDto;
import com.aquacomunidad.backend.features.tablero.service.TableroServicio;
import com.aquacomunidad.backend.features.reporte.entity.ReporteEntidad;
import com.aquacomunidad.backend.features.reporte.repository.ReporteRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TableroServicioImpl implements TableroServicio {

  private final ReporteRepositorio reporteRepositorio;
  private final CasoRepositorio casoRepositorio;

  @Override
  @Transactional(readOnly = true)
  public TableroKpiDto getKpis() {
    return TableroKpiDto.builder()
        .reportesPendientes(reporteRepositorio.countByEstado(EstadoReporte.PENDIENTE))
        .reportesEnProceso(reporteRepositorio.countByEstado(EstadoReporte.EN_PROCESO))
        .reportesResueltos(reporteRepositorio.countByEstado(EstadoReporte.RESUELTO))
        .casosAbiertos(casoRepositorio.countByEstado(EstadoCaso.EN_PROCESO))
        .actividadSemanal(actividadSemanal())
        .reportesPorZona(reportesPorZona())
        .build();
  }

  private List<ActividadSemanalDto> actividadSemanal() {
    LocalDate hoy = LocalDate.now();
    LocalDate inicio = hoy.minusDays(6);
    LocalDateTime desde = inicio.atStartOfDay();
    LocalDateTime hasta = hoy.plusDays(1).atStartOfDay();

    Map<LocalDate, Long> conteoPorDia = reporteRepositorio
        .findByFechaCreacionBetweenOrderByFechaCreacionAsc(desde, hasta)
        .stream()
        .collect(Collectors.groupingBy(
            reporte -> reporte.getFechaCreacion().toLocalDate(),
            Collectors.counting()));

    return IntStream.rangeClosed(0, 6)
        .mapToObj(inicio::plusDays)
        .map(dia -> ActividadSemanalDto.builder()
            .dia(etiquetaDia(dia, hoy))
            .valor(conteoPorDia.getOrDefault(dia, 0L))
            .build())
        .toList();
  }

  private List<ReportePorZonaDto> reportesPorZona() {
    Map<String, Long> conteoPorZona = reporteRepositorio.findAll()
        .stream()
        .collect(Collectors.groupingBy(
            reporte -> normalizarZona(reporte.getZona()),
            LinkedHashMap::new,
            Collectors.counting()));

    return conteoPorZona.entrySet()
        .stream()
        .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
            .thenComparing(Map.Entry.comparingByKey()))
        .limit(5)
        .map(entry -> ReportePorZonaDto.builder()
            .nombre(entry.getKey())
            .cantidad(entry.getValue())
            .build())
        .toList();
  }

  private String etiquetaDia(LocalDate dia, LocalDate hoy) {
    if (dia.equals(hoy)) {
      return "Hoy";
    }
    String nombre = dia.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es-PE"));
    return nombre.substring(0, 1).toUpperCase(Locale.ROOT) + nombre.substring(1).replace(".", "");
  }

  private String normalizarZona(String zona) {
    if (zona == null || zona.isBlank()) {
      return "Sin zona";
    }
    return zona.trim();
  }
}
