package com.aquacomunidad.backend.features.historial.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HistorialCambioDto {
  private String tipo;
  private String estadoAnterior;
  private String estadoNuevo;
  private String observacion;
  private Long cambiadoPor;
  private LocalDateTime fechaCambio;
}
