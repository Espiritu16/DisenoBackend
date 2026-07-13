package com.aquacomunidad.backend.features.tablero.dto;

import java.util.List;

import com.aquacomunidad.backend.features.iot.dto.NivelAguaDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TableroKpiDto {
  private long totalReportes;
  private long reportesPendientes;
  private long reportesEnProceso;
  private long reportesResueltos;
  private long totalCiudadanosReportantes;
  private long casosAbiertos;
  private long casosResueltos;
  private double promedioHorasResolucion;
  private double incrementoEstimadoPorcentaje;
  private String recomendacionAutomatica;
  private List<ActividadSemanalDto> actividadSemanal;
  private List<ReportePorMesDto> reportesPorMes;
  private List<UsuarioReportantePorMesDto> usuariosReportantesPorMes;
  private List<ReportePorCategoriaDto> reportesPorCategoria;
  private List<ReportePorEstadoDto> reportesPorEstado;
  private List<ReportePorZonaDto> reportesPorZona;
  private List<TiempoAtencionPorZonaDto> tiemposPorZona;
  private List<TendenciaZonaDto> zonasCriticas;
  private List<ProyeccionMensualDto> proyeccionMensual;
  private List<CategoriaCrecimientoDto> categoriasConCrecimiento;
  private List<ZonaRiesgoDto> zonasRiesgo;
  private List<NivelAguaDto> nivelesAgua;

  @Getter
  @Builder
  public static class ActividadSemanalDto {
    private String dia;
    private long valor;
  }

  @Getter
  @Builder
  public static class ReportePorZonaDto {
    private String nombre;
    private long cantidad;
  }

  @Getter
  @Builder
  public static class ReportePorMesDto {
    private String mes;
    private long cantidad;
  }

  @Getter
  @Builder
  public static class UsuarioReportantePorMesDto {
    private String mes;
    private long cantidad;
  }

  @Getter
  @Builder
  public static class ReportePorCategoriaDto {
    private String categoria;
    private long cantidad;
  }

  @Getter
  @Builder
  public static class ReportePorEstadoDto {
    private String estado;
    private long cantidad;
  }

  @Getter
  @Builder
  public static class TiempoAtencionPorZonaDto {
    private String zona;
    private double promedioHoras;
    private long casosResueltos;
  }

  @Getter
  @Builder
  public static class TendenciaZonaDto {
    private String zona;
    private long reportesUltimos30Dias;
    private long reportes30DiasPrevios;
    private double variacionPorcentual;
  }

  @Getter
  @Builder
  public static class ProyeccionMensualDto {
    private String mes;
    private long estimado;
  }

  @Getter
  @Builder
  public static class CategoriaCrecimientoDto {
    private String categoria;
    private long baseActual;
    private long estimadoSiguienteMes;
    private double crecimientoPorcentual;
  }

  @Getter
  @Builder
  public static class ZonaRiesgoDto {
    private String zona;
    private long reportes;
    private String nivelRiesgo;
  }
}
