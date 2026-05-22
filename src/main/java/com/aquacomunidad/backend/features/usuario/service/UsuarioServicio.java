package com.aquacomunidad.backend.features.usuario.service;

import java.util.List;

import com.aquacomunidad.backend.features.usuario.dto.UsuarioActualizacionRolEstadoSolicitudDto;
import com.aquacomunidad.backend.features.usuario.dto.UsuarioSolicitudDto;
import com.aquacomunidad.backend.features.usuario.dto.UsuarioRespuestaDto;

public interface UsuarioServicio {
  UsuarioRespuestaDto crear(UsuarioSolicitudDto request);

  List<UsuarioRespuestaDto> listarTodos();

  UsuarioRespuestaDto actualizarRolEstado(Long id, UsuarioActualizacionRolEstadoSolicitudDto request);
}
