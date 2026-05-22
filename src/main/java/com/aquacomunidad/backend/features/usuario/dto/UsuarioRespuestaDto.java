package com.aquacomunidad.backend.features.usuario.dto;

import com.aquacomunidad.backend.common.enums.EstadoUsuario;
import com.aquacomunidad.backend.common.enums.RolUsuario;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UsuarioRespuestaDto {
  private Long id;
  private String nombre;
  private String correo;
  private RolUsuario rol;
  private EstadoUsuario estado;
}
