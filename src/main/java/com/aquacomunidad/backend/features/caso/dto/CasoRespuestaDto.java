package com.aquacomunidad.backend.features.caso.dto;

import java.time.LocalDateTime;

import com.aquacomunidad.backend.common.enums.EstadoCaso;
import com.aquacomunidad.backend.common.enums.PrioridadCaso;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CasoRespuestaDto {
  private Long id;
  private Long reporteId;
  private Long responsableId;
  private PrioridadCaso prioridad;
  private EstadoCaso estado;
  private String observaciones;
  private String evidenciaCierre;
  private LocalDateTime fechaAsignacion;
  private LocalDateTime fechaCierre;
}
