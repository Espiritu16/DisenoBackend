package com.aquacomunidad.backend.features.usuario.service.impl;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquacomunidad.backend.exception.ExcepcionApi;
import com.aquacomunidad.backend.features.usuario.dto.UsuarioActualizacionRolEstadoSolicitudDto;
import com.aquacomunidad.backend.features.usuario.dto.UsuarioSolicitudDto;
import com.aquacomunidad.backend.features.usuario.dto.UsuarioRespuestaDto;
import com.aquacomunidad.backend.features.usuario.entity.UsuarioEntidad;
import com.aquacomunidad.backend.features.usuario.mapper.UsuarioMapeador;
import com.aquacomunidad.backend.features.usuario.repository.UsuarioRepositorio;
import com.aquacomunidad.backend.features.usuario.service.UsuarioServicio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioServicioImpl implements UsuarioServicio {

  private final UsuarioRepositorio usuarioRepositorio;
  private final UsuarioMapeador usuarioMapeador;

  @Override
  @Transactional
  public UsuarioRespuestaDto crear(UsuarioSolicitudDto request) {
    String correo = normalizarCorreo(request.getCorreo());
    if (usuarioRepositorio.existsByCorreoIgnoreCase(correo)) {
      throw new ExcepcionApi(HttpStatus.CONFLICT, "El correo ya esta registrado");
    }
    request.setCorreo(correo);
    request.setNombre(request.getNombre().trim());
    return usuarioMapeador.aRespuesta(usuarioRepositorio.save(usuarioMapeador.toEntidad(request)));
  }

  @Override
  @Transactional(readOnly = true)
  public List<UsuarioRespuestaDto> listarTodos() {
    return usuarioRepositorio.findAll().stream().map(usuarioMapeador::aRespuesta).toList();
  }

  @Override
  @Transactional
  public UsuarioRespuestaDto actualizarRolEstado(Long id, UsuarioActualizacionRolEstadoSolicitudDto request) {
    UsuarioEntidad usuario = usuarioRepositorio.findById(id)
        .orElseThrow(() -> new ExcepcionApi(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    usuario.setRol(request.getRol());
    usuario.setEstado(request.getEstado());
    return usuarioMapeador.aRespuesta(usuarioRepositorio.save(usuario));
  }

  private String normalizarCorreo(String correo) {
    return correo == null ? "" : correo.trim().toLowerCase();
  }
}
