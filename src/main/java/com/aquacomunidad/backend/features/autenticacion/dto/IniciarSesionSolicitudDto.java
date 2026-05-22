package com.aquacomunidad.backend.features.autenticacion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IniciarSesionSolicitudDto {

  @NotBlank
  @Email
  private String correo;

  @NotBlank
  private String password;
}
