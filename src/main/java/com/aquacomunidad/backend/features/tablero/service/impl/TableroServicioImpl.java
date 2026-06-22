package com.aquacomunidad.backend.features.tablero.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquacomunidad.backend.common.enums.EstadoCaso;
import com.aquacomunidad.backend.common.enums.EstadoReporte;
import com.aquacomunidad.backend.features.caso.entity.CasoEntidad;
import com.aquacomunidad.backend.features.caso.repository.CasoRepositorio;
import com.aquacomunidad.backend.features.iot.service.IotServicio;
import com.aquacomunidad.backend.features.reporte.entity.ReporteEntidad;
import com.aquacomunidad.backend.features.reporte.repository.ReporteConteoPorZona;
import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto;
import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto.ActividadSemanalDto;
import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto.ReportePorZonaDto;
import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto.TendenciaZonaDto;
import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto.TiempoAtencionPorZonaDto;
import com.aquacomunidad.backend.features.tablero.service.TableroServicio;
import com.aquacomunidad.backend.features.reporte.repository.ReporteConteoEstado;
import com.aquacomunidad.backend.features.reporte.repository.ReporteRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TableroServicioImpl implements TableroServicio {

  private final ReporteRepositorio reporteRepositorio;
  private final CasoRepositorio casoRepositorio;
  private final IotServicio iotServicio;

  @Override
  @Transactional(readOnly = true)
  public TableroKpiDto getKpis() {
    Map<EstadoReporte, Long> reportesPorEstado = reporteRepositorio.contarPorEstadoGlobal()
        .stream()
        .collect(Collectors.toMap(
            ReporteConteoEstado::getEstado,
            ReporteConteoEstado::getTotal,
            (actual, reemplazo) -> actual,
            () -> new EnumMap<>(EstadoReporte.class)));
    List<CasoEntidad> casosResueltos = casoRepositorio.findByEstado(EstadoCaso.RESUELTO);

    return TableroKpiDto.builder()
        .reportesPendientes(reportesPorEstado.getOrDefault(EstadoReporte.PENDIENTE, 0L))
        .reportesEnProceso(reportesPorEstado.getOrDefault(EstadoReporte.EN_PROCESO, 0L))
        .reportesResueltos(reportesPorEstado.getOrDefault(EstadoReporte.RESUELTO, 0L))
        .casosAbiertos(casoRepositorio.countByEstado(EstadoCaso.EN_PROCESO))
        .casosResueltos(casosResueltos.size())
        .promedioHorasResolucion(promedioHorasResolucion(casosResueltos))
        .actividadSemanal(actividadSemanal())
        .reportesPorZona(reportesPorZona())
        .tiemposPorZona(tiemposPorZona(casosResueltos))
        .zonasCriticas(zonasCriticas())
        .nivelesAgua(iotServicio.listarNiveles())
        .build();
  }

  private List<ActividadSemanalDto> actividadSemanal() {
    LocalDate hoy = LocalDate.now();
    LocalDate inicio = hoy.minusDays(6);
    LocalDateTime desde = inicio.atStartOfDay();
    LocalDateTime hasta = hoy.plusDays(1).atStartOfDay();

    Map<LocalDate, Long> conteoPorDia = reporteRepositorio.contarPorDia(desde, hasta)
        .stream()
        .collect(Collectors.toMap(
            conteo -> conteo.getFecha(),
            conteo -> conteo.getTotal(),
            (actual, reemplazo) -> actual,
            LinkedHashMap::new));

    return IntStream.rangeClosed(0, 6)
        .mapToObj(inicio::plusDays)
        .map(dia -> ActividadSemanalDto.builder()
            .dia(etiquetaDia(dia, hoy))
            .valor(conteoPorDia.getOrDefault(dia, 0L))
            .build())
        .toList();
  }

  private List<ReportePorZonaDto> reportesPorZona() {
    return reporteRepositorio.contarZonas(PageRequest.of(0, 5))
        .stream()
        .map(conteo -> ReportePorZonaDto.builder()
            .nombre(normalizarZona(conteo.getZona()))
            .cantidad(conteo.getTotal())
            .build())
        .toList();
  }

  private double promedioHorasResolucion(List<CasoEntidad> casosResueltos) {
    return redondearDosDecimales(casosResueltos.stream()
        .filter(this::tieneFechasResolucion)
        .mapToDouble(this::horasResolucion)
        .average()
        .orElse(0D));
  }

  private List<TiempoAtencionPorZonaDto> tiemposPorZona(List<CasoEntidad> casosResueltos) {
    Map<String, List<CasoEntidad>> casosPorZona = casosResueltos.stream()
        .filter(this::tieneFechasResolucion)
        .collect(Collectors.groupingBy(
            caso -> normalizarZona(caso.getReporteOrigen().getZona()),
            LinkedHashMap::new,
            Collectors.toList()));

    return casosPorZona.entrySet()
        .stream()
        .map(entry -> TiempoAtencionPorZonaDto.builder()
            .zona(entry.getKey())
            .casosResueltos(entry.getValue().size())
            .promedioHoras(redondearDosDecimales(entry.getValue().stream()
                .mapToDouble(this::horasResolucion)
                .average()
                .orElse(0D)))
            .build())
        .sorted(Comparator
            .comparingDouble(TiempoAtencionPorZonaDto::getPromedioHoras).reversed()
            .thenComparing(TiempoAtencionPorZonaDto::getZona))
        .toList();
  }

  private List<TendenciaZonaDto> zonasCriticas() {
    LocalDateTime hasta = LocalDate.now().plusDays(1).atStartOfDay();
    LocalDateTime inicioActual = LocalDate.now().minusDays(29).atStartOfDay();
    LocalDateTime inicioPrevio = LocalDate.now().minusDays(59).atStartOfDay();
    Map<String, List<ReporteEntidad>> reportesPorZona = reporteRepositorio
        .findByFechaCreacionBetweenOrderByFechaCreacionAsc(inicioPrevio, hasta)
        .stream()
        .collect(Collectors.groupingBy(
            reporte -> normalizarZona(reporte.getZona()),
            LinkedHashMap::new,
            Collectors.toList()));

    return reporteRepositorio.contarZonas(PageRequest.of(0, 5))
        .stream()
        .map(ReporteConteoPorZona::getZona)
        .map(this::normalizarZona)
        .map(zona -> tendenciaZona(zona, reportesPorZona.getOrDefault(zona, List.of()), inicioActual))
        .toList();
  }

  private TendenciaZonaDto tendenciaZona(String zona, List<ReporteEntidad> reportes, LocalDateTime inicioActual) {
    long reportesActuales = reportes.stream()
        .filter(reporte -> !reporte.getFechaCreacion().isBefore(inicioActual))
        .count();
    long reportesPrevios = reportes.size() - reportesActuales;

    return TendenciaZonaDto.builder()
        .zona(zona)
        .reportesUltimos30Dias(reportesActuales)
        .reportes30DiasPrevios(reportesPrevios)
        .variacionPorcentual(variacionPorcentual(reportesActuales, reportesPrevios))
        .build();
  }

  private boolean tieneFechasResolucion(CasoEntidad caso) {
    return caso.getFechaAsignacion() != null
        && caso.getFechaCierre() != null
        && !caso.getFechaCierre().isBefore(caso.getFechaAsignacion())
        && caso.getReporteOrigen() != null;
  }

  private double horasResolucion(CasoEntidad caso) {
    return Duration.between(caso.getFechaAsignacion(), caso.getFechaCierre()).toMinutes() / 60D;
  }

  private double variacionPorcentual(long actual, long previo) {
    if (previo == 0L) {
      return actual > 0L ? 100D : 0D;
    }
    return redondearDosDecimales(((double) actual - previo) * 100D / previo);
  }

  private double redondearDosDecimales(double valor) {
    return BigDecimal.valueOf(valor)
        .setScale(2, RoundingMode.HALF_UP)
        .doubleValue();
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
