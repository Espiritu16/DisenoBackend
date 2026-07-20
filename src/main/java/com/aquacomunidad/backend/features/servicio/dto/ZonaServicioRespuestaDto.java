package com.aquacomunidad.backend.features.servicio.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ZonaServicioRespuestaDto {
  private Long id;
  private String nombre;
  private String codigo;
}
