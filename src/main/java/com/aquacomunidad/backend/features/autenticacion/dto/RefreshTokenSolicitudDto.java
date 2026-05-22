package com.aquacomunidad.backend.features.autenticacion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenSolicitudDto {

  @NotBlank(message = "El refresh token es obligatorio")
  private String refreshToken;
}
