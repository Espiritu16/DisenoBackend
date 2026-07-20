package com.aquacomunidad.backend.features.servicio.dto;

import java.time.LocalDateTime;

import com.aquacomunidad.backend.common.enums.EstadoAlertaServicio;
import com.aquacomunidad.backend.common.enums.SeveridadAlertaServicio;
import com.aquacomunidad.backend.common.enums.TipoAlertaServicio;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AlertaServicioRespuestaDto {
  private Long id;
  private TipoAlertaServicio tipo;
  private String titulo;
  private String descripcion;
  private SeveridadAlertaServicio severidad;
  private EstadoAlertaServicio estado;
  private String zona;
  private LocalDateTime iniciaEn;
  private LocalDateTime finalizaEn;
  private LocalDateTime creadoEn;
}
