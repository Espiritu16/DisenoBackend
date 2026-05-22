package com.aquacomunidad.backend.features.caso.dto;

import com.aquacomunidad.backend.common.enums.EstadoCaso;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CasoActualizacionDto {

  @NotNull
  private EstadoCaso estado;

  private String observaciones;
  private String evidenciaCierre;
}
