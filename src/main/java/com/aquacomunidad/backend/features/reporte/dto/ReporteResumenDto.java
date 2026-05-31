package com.aquacomunidad.backend.features.reporte.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReporteResumenDto {
  private long total;
  private long pendientes;
  private long enProceso;
  private long resueltos;
  private long duplicados;
  private long rechazados;
  private long escalados;
  private ReporteResumenItemDto ultimoReporte;
}
