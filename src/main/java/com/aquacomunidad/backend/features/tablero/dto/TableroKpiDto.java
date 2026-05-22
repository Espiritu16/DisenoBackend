package com.aquacomunidad.backend.features.tablero.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TableroKpiDto {
  private long reportesPendientes;
  private long reportesEnProceso;
  private long reportesResueltos;
  private long casosAbiertos;
}
