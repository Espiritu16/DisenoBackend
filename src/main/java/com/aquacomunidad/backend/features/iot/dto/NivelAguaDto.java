package com.aquacomunidad.backend.features.iot.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.aquacomunidad.backend.common.enums.TipoInfraestructura;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NivelAguaDto {
  private Long infraestructuraId;
  private String nombre;
  private String zona;
  private TipoInfraestructura tipo;
  private BigDecimal nivelPorcentaje;
  private BigDecimal bateriaPorcentaje;
  private BigDecimal senalPorcentaje;
  private String estado;
  private LocalDateTime actualizadoEn;
}
