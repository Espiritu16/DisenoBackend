package com.aquacomunidad.backend.features.usuario.dto;

import com.aquacomunidad.backend.common.enums.EstadoUsuario;
import com.aquacomunidad.backend.common.enums.RolUsuario;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioActualizacionRolEstadoSolicitudDto {

  @NotNull
  private RolUsuario rol;

  @NotNull
  private EstadoUsuario estado;
}
