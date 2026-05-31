package com.aquacomunidad.backend.features.tablero.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TableroKpiDto {
  private long reportesPendientes;
  private long reportesEnProceso;
  private long reportesResueltos;
  private long casosAbiertos;
  private List<ActividadSemanalDto> actividadSemanal;
  private List<ReportePorZonaDto> reportesPorZona;

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
}
