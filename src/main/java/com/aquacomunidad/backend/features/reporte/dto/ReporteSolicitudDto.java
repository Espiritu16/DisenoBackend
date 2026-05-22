package com.aquacomunidad.backend.features.reporte.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReporteSolicitudDto {

  @NotNull
  private Long usuarioId;

  @NotBlank
  private String tipo;

  @NotBlank
  private String descripcion;

  @NotBlank
  private String fotoUrl;

  @NotNull
  @DecimalMin("-90.0")
  @DecimalMax("90.0")
  private BigDecimal lat;

  @NotNull
  @DecimalMin("-180.0")
  @DecimalMax("180.0")
  private BigDecimal lng;

  @NotBlank
  private String direccion;

  @NotBlank
  private String zona;
}
