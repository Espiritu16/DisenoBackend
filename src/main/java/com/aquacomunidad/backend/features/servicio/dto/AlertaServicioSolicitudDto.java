package com.aquacomunidad.backend.features.servicio.dto;

import java.time.LocalDateTime;

import com.aquacomunidad.backend.common.enums.EstadoAlertaServicio;
import com.aquacomunidad.backend.common.enums.SeveridadAlertaServicio;
import com.aquacomunidad.backend.common.enums.TipoAlertaServicio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlertaServicioSolicitudDto {
  private Long zonaId;

  @NotNull
  private TipoAlertaServicio tipo;

  @NotBlank
  private String titulo;

  @NotBlank
  private String descripcion;

  private SeveridadAlertaServicio severidad;
  private EstadoAlertaServicio estado;
  private LocalDateTime iniciaEn;
  private LocalDateTime finalizaEn;
}
