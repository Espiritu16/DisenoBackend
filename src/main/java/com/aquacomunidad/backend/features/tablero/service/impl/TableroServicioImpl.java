package com.aquacomunidad.backend.features.tablero.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
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
import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto.CategoriaCrecimientoDto;
import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto.ProyeccionMensualDto;
import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto.ReportePorCategoriaDto;
import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto.ReportePorEstadoDto;
import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto.ReportePorMesDto;
import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto.ReportePorZonaDto;
import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto.TendenciaZonaDto;
import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto.TiempoAtencionPorZonaDto;
import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto.ZonaRiesgoDto;
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
    List<ReporteEntidad> reportes = reporteRepositorio.findAll();
    List<CasoEntidad> casosResueltos = casoRepositorio.findByEstado(EstadoCaso.RESUELTO);
    List<ReportePorMesDto> reportesPorMes = reportesPorMes(reportes);
    double incrementoEstimado = incrementoEstimado(reportesPorMes);

    return TableroKpiDto.builder()
        .totalReportes(reportes.size())
        .reportesPendientes(reportesPorEstado.getOrDefault(EstadoReporte.PENDIENTE, 0L))
        .reportesEnProceso(reportesPorEstado.getOrDefault(EstadoReporte.EN_PROCESO, 0L))
        .reportesResueltos(reportesPorEstado.getOrDefault(EstadoReporte.RESUELTO, 0L))
        .casosAbiertos(casoRepositorio.countByEstado(EstadoCaso.EN_PROCESO))
        .casosResueltos(casosResueltos.size())
        .promedioHorasResolucion(promedioHorasResolucion(casosResueltos))
        .incrementoEstimadoPorcentaje(incrementoEstimado)
        .recomendacionAutomatica(recomendacionAutomatica(reportes, incrementoEstimado))
        .actividadSemanal(actividadSemanal())
        .reportesPorMes(reportesPorMes)
        .reportesPorCategoria(reportesPorCategoria(reportes))
        .reportesPorEstado(reportesPorEstado(reportesPorEstado))
        .reportesPorZona(reportesPorZona())
        .tiemposPorZona(tiemposPorZona(casosResueltos))
        .zonasCriticas(zonasCriticas())
        .proyeccionMensual(proyeccionMensual(reportes, reportesPorMes, incrementoEstimado))
        .categoriasConCrecimiento(categoriasConCrecimiento(reportes))
        .zonasRiesgo(zonasRiesgo(reportes))
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

  private List<ReportePorMesDto> reportesPorMes(List<ReporteEntidad> reportes) {
    Map<YearMonth, Long> conteoPorMes = reportes.stream()
        .filter(reporte -> reporte.getFechaCreacion() != null)
        .collect(Collectors.groupingBy(
            reporte -> YearMonth.from(reporte.getFechaCreacion()),
            LinkedHashMap::new,
            Collectors.counting()));

    return conteoPorMes.entrySet()
        .stream()
        .sorted(Map.Entry.comparingByKey())
        .map(entry -> ReportePorMesDto.builder()
            .mes(etiquetaMes(entry.getKey()))
            .cantidad(entry.getValue())
            .build())
        .toList();
  }

  private List<ReportePorCategoriaDto> reportesPorCategoria(List<ReporteEntidad> reportes) {
    return reportes.stream()
        .filter(reporte -> reporte.getTipo() != null)
        .collect(Collectors.groupingBy(
            reporte -> reporte.getTipo().getNombre(),
            LinkedHashMap::new,
            Collectors.counting()))
        .entrySet()
        .stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
        .map(entry -> ReportePorCategoriaDto.builder()
            .categoria(entry.getKey())
            .cantidad(entry.getValue())
            .build())
        .toList();
  }

  private List<ReportePorEstadoDto> reportesPorEstado(Map<EstadoReporte, Long> reportesPorEstado) {
    return List.of(EstadoReporte.PENDIENTE, EstadoReporte.EN_PROCESO, EstadoReporte.RESUELTO,
        EstadoReporte.ESCALADO, EstadoReporte.RECHAZADO, EstadoReporte.DUPLICADO)
        .stream()
        .map(estado -> ReportePorEstadoDto.builder()
            .estado(etiquetaEstado(estado))
            .cantidad(reportesPorEstado.getOrDefault(estado, 0L))
            .build())
        .toList();
  }

  private List<ProyeccionMensualDto> proyeccionMensual(
      List<ReporteEntidad> reportes,
      List<ReportePorMesDto> reportesPorMes,
      double incrementoEstimado) {
    long base = reportesPorMes.isEmpty() ? 0L : reportesPorMes.get(reportesPorMes.size() - 1).getCantidad();
    double factor = 1D + (incrementoEstimado / 100D);
    YearMonth inicio = ultimoMesReportado(reportes).plusMonths(1);

    return IntStream.rangeClosed(0, 2)
        .mapToObj(offset -> {
          long estimado = Math.max(0L, Math.round(base * Math.pow(factor, offset + 1)));
          return ProyeccionMensualDto.builder()
              .mes(etiquetaMes(inicio.plusMonths(offset)))
              .estimado(estimado)
              .build();
        })
        .toList();
  }

  private YearMonth ultimoMesReportado(List<ReporteEntidad> reportes) {
    return reportes.stream()
        .filter(reporte -> reporte.getFechaCreacion() != null)
        .map(reporte -> YearMonth.from(reporte.getFechaCreacion()))
        .max(Comparator.naturalOrder())
        .orElse(YearMonth.now());
  }

  private List<CategoriaCrecimientoDto> categoriasConCrecimiento(List<ReporteEntidad> reportes) {
    if (reportes.isEmpty()) {
      return List.of();
    }
    YearMonth ultimoMes = reportes.stream()
        .filter(reporte -> reporte.getFechaCreacion() != null)
        .map(reporte -> YearMonth.from(reporte.getFechaCreacion()))
        .max(Comparator.naturalOrder())
        .orElse(YearMonth.now());
    YearMonth mesPrevio = ultimoMes.minusMonths(1);

    Map<String, List<ReporteEntidad>> porCategoria = reportes.stream()
        .filter(reporte -> reporte.getTipo() != null && reporte.getFechaCreacion() != null)
        .collect(Collectors.groupingBy(reporte -> reporte.getTipo().getNombre()));

    return porCategoria.entrySet()
        .stream()
        .map(entry -> {
          long actual = contarCategoriaEnMes(entry.getValue(), ultimoMes);
          long previo = contarCategoriaEnMes(entry.getValue(), mesPrevio);
          double crecimiento = variacionPorcentual(actual, previo);
          long estimado = Math.max(actual, Math.round(actual * (1D + Math.max(crecimiento, 8D) / 100D)));
          return CategoriaCrecimientoDto.builder()
              .categoria(entry.getKey())
              .baseActual(actual)
              .estimadoSiguienteMes(estimado)
              .crecimientoPorcentual(crecimiento)
              .build();
        })
        .sorted(Comparator
            .comparingDouble(CategoriaCrecimientoDto::getCrecimientoPorcentual).reversed()
            .thenComparing(CategoriaCrecimientoDto::getCategoria))
        .limit(5)
        .toList();
  }

  private List<ZonaRiesgoDto> zonasRiesgo(List<ReporteEntidad> reportes) {
    return reportes.stream()
        .collect(Collectors.groupingBy(
            reporte -> normalizarZona(reporte.getZona()),
            LinkedHashMap::new,
            Collectors.counting()))
        .entrySet()
        .stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
        .limit(5)
        .map(entry -> ZonaRiesgoDto.builder()
            .zona(entry.getKey())
            .reportes(entry.getValue())
            .nivelRiesgo(nivelRiesgo(entry.getValue()))
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

  private double incrementoEstimado(List<ReportePorMesDto> reportesPorMes) {
    if (reportesPorMes.size() < 2) {
      return 0D;
    }
    long penultimo = reportesPorMes.get(reportesPorMes.size() - 2).getCantidad();
    long ultimo = reportesPorMes.get(reportesPorMes.size() - 1).getCantidad();
    return Math.max(0D, variacionPorcentual(ultimo, penultimo));
  }

  private long contarCategoriaEnMes(List<ReporteEntidad> reportes, YearMonth mes) {
    return reportes.stream()
        .filter(reporte -> YearMonth.from(reporte.getFechaCreacion()).equals(mes))
        .count();
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

  private String etiquetaMes(YearMonth mes) {
    return switch (mes.getMonth()) {
      case JANUARY -> "Ene";
      case FEBRUARY -> "Feb";
      case MARCH -> "Mar";
      case APRIL -> "Abr";
      case MAY -> "May";
      case JUNE -> "Jun";
      case JULY -> "Jul";
      case AUGUST -> "Ago";
      case SEPTEMBER -> "Sep";
      case OCTOBER -> "Oct";
      case NOVEMBER -> "Nov";
      case DECEMBER -> "Dic";
    };
  }

  private String etiquetaEstado(EstadoReporte estado) {
    return switch (estado) {
      case PENDIENTE -> "Pendientes";
      case EN_PROCESO -> "En proceso";
      case RESUELTO -> "Resueltos";
      case DUPLICADO -> "Duplicados";
      case RECHAZADO -> "Rechazados";
      case ESCALADO -> "Escalados";
    };
  }

  private String nivelRiesgo(long reportes) {
    if (reportes >= 18L) {
      return "Alto";
    }
    if (reportes >= 10L) {
      return "Medio";
    }
    return "Bajo";
  }

  private String recomendacionAutomatica(List<ReporteEntidad> reportes, double incrementoEstimado) {
    String zona = zonasRiesgo(reportes).stream()
        .findFirst()
        .map(ZonaRiesgoDto::getZona)
        .orElse("las zonas con mayor actividad");
    String categoria = reportesPorCategoria(reportes).stream()
        .findFirst()
        .map(ReportePorCategoriaDto::getCategoria)
        .orElse("incidencias ciudadanas");

    if (incrementoEstimado > 0D) {
      return "Se estima un incremento del " + redondearDosDecimales(incrementoEstimado)
          + "% en los proximos meses. Priorizar cuadrillas en " + zona
          + " y monitorear especialmente " + categoria + ".";
    }
    return "La tendencia se mantiene estable. Mantener monitoreo en " + zona
        + " y revisar preventivamente los reportes de " + categoria + ".";
  }

  private String normalizarZona(String zona) {
    if (zona == null || zona.isBlank()) {
      return "Sin zona";
    }
    return zona.trim();
  }
}
