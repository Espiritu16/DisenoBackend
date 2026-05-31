package com.aquacomunidad.backend.features.reporte.dto;

import java.time.LocalDateTime;

import com.aquacomunidad.backend.common.enums.EstadoReporte;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReporteResumenItemDto {
  private Long id;
  private String codigo;
  private String tipo;
  private String zona;
  private EstadoReporte estado;
  private LocalDateTime fechaCreacion;
  private LocalDateTime fechaActualizacion;
}
