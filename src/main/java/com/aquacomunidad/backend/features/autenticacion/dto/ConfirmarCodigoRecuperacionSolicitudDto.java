package com.aquacomunidad.backend.features.autenticacion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmarCodigoRecuperacionSolicitudDto {

  @NotBlank
  @Email
  private String correo;

  @NotBlank
  @Pattern(regexp = "\\\\d{6}", message = "El codigo debe tener 6 digitos")
  private String codigo;
}
