package com.aquacomunidad.backend.features.caso.dto;

import com.aquacomunidad.backend.common.enums.PrioridadCaso;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CasoSolicitudDto {

  @NotNull
  private Long reporteId;

  @NotNull
  private Long responsableId;

  private PrioridadCaso prioridad;
}
