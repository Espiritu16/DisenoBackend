package com.aquacomunidad.backend.features.servicio.dto;

import java.time.LocalDateTime;

import com.aquacomunidad.backend.common.enums.EstadoAlertaServicio;
import com.aquacomunidad.backend.common.enums.SeveridadAlertaServicio;
import com.aquacomunidad.backend.common.enums.TipoAlertaServicio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlertaServicioSolicitudDto {
  private Long zonaId;

  @NotNull
  private TipoAlertaServicio tipo;

  @NotBlank
  @Size(max = 160)
  private String titulo;

  @NotBlank
  @Size(max = 800)
  private String descripcion;

  private SeveridadAlertaServicio severidad;
  private EstadoAlertaServicio estado;
  private LocalDateTime iniciaEn;
  private LocalDateTime finalizaEn;
}
