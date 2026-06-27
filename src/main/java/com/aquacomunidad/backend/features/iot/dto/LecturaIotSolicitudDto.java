package com.aquacomunidad.backend.features.iot.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LecturaIotSolicitudDto {

  @NotNull
  private Long infraestructuraId;

  @NotNull
  @DecimalMin("0.0")
  @DecimalMax("100.0")
  private BigDecimal nivelPorcentaje;

  @DecimalMin("0.0")
  private BigDecimal volumenLitros;

  @DecimalMin("0.0")
  @DecimalMax("100.0")
  private BigDecimal bateriaPorcentaje;

  @DecimalMin("0.0")
  @DecimalMax("100.0")
  private BigDecimal senalPorcentaje;

  private LocalDateTime leidoEn;
}
