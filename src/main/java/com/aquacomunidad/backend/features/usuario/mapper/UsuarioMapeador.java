package com.aquacomunidad.backend.features.usuario.mapper;

import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.aquacomunidad.backend.common.enums.EstadoUsuario;
import com.aquacomunidad.backend.features.usuario.dto.UsuarioSolicitudDto;
import com.aquacomunidad.backend.features.usuario.dto.UsuarioRespuestaDto;
import com.aquacomunidad.backend.features.usuario.entity.UsuarioEntidad;

@Component
public class UsuarioMapeador {
  private final PasswordEncoder passwordEncoder;

  public UsuarioMapeador(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  public UsuarioEntidad toEntidad(UsuarioSolicitudDto dto) {
    UsuarioEntidad entity = new UsuarioEntidad();
    entity.setNombre(dto.getNombre());
    entity.setCorreo(dto.getCorreo());
    entity.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
    entity.setRol(dto.getRol());
    entity.setEstado(EstadoUsuario.ACTIVO);
    return entity;
  }

  public UsuarioRespuestaDto aRespuesta(UsuarioEntidad entity) {
    return UsuarioRespuestaDto.builder()
        .id(entity.getId())
        .nombre(entity.getNombre())
        .correo(entity.getCorreo())
        .rol(entity.getRol())
        .estado(entity.getEstado())
        .build();
  }
}
