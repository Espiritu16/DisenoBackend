package com.aquacomunidad.backend.features.autenticacion.dto;

import com.aquacomunidad.backend.common.enums.RolUsuario;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IniciarSesionRespuestaDto {
  private String token;
  private String refreshToken;
  private Long userId;
  private String nombre;
  private String correo;
  private RolUsuario rol;
}
