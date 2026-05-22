package com.aquacomunidad.backend.features.reporte.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.aquacomunidad.backend.common.enums.EstadoReporte;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReporteRespuestaDto {
  private Long id;
  private Long usuarioId;
  private String tipo;
  private String descripcion;
  private String fotoUrl;
  private BigDecimal lat;
  private BigDecimal lng;
  private String direccion;
  private String zona;
  private Boolean posibleDuplicado;
  private EstadoReporte estado;
  private LocalDateTime fechaCreacion;
  private LocalDateTime fechaActualizacion;
}
