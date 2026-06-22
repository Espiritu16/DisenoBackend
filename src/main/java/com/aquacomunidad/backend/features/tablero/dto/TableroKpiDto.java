package com.aquacomunidad.backend.features.tablero.dto;

import java.util.List;

import com.aquacomunidad.backend.features.iot.dto.NivelAguaDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TableroKpiDto {
  private long reportesPendientes;
  private long reportesEnProceso;
  private long reportesResueltos;
  private long casosAbiertos;
  private long casosResueltos;
  private double promedioHorasResolucion;
  private List<ActividadSemanalDto> actividadSemanal;
  private List<ReportePorZonaDto> reportesPorZona;
  private List<TiempoAtencionPorZonaDto> tiemposPorZona;
  private List<TendenciaZonaDto> zonasCriticas;
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
}
