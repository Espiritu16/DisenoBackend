package com.aquacomunidad.backend.features.usuario.dto;

import com.aquacomunidad.backend.common.enums.RolUsuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioSolicitudDto {

  @NotBlank
  private String nombre;

  @NotBlank
  @Email
  private String correo;

  @NotBlank
  private String password;

  @NotNull
  private RolUsuario rol;
}
