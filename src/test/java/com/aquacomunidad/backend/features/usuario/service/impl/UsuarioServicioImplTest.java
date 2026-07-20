package com.aquacomunidad.backend.features.usuario.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aquacomunidad.backend.features.usuario.dto.UsuarioRespuestaDto;
import com.aquacomunidad.backend.features.usuario.entity.UsuarioEntidad;
import com.aquacomunidad.backend.features.usuario.mapper.UsuarioMapeador;
import com.aquacomunidad.backend.features.usuario.repository.UsuarioRepositorio;

@ExtendWith(MockitoExtension.class)
class UsuarioServicioImplTest {

  @Mock
  private UsuarioRepositorio usuarioRepositorio;
  @Mock
  private UsuarioMapeador usuarioMapeador;

  @InjectMocks
  private UsuarioServicioImpl usuarioServicio;

  @Test
  void listarTodosUsaOrdenDescendentePorId() {
    UsuarioEntidad usuario = new UsuarioEntidad();
    usuario.setId(20L);
    UsuarioRespuestaDto respuesta = UsuarioRespuestaDto.builder().id(20L).build();
    when(usuarioRepositorio.findAllByOrderByIdDesc()).thenReturn(List.of(usuario));
    when(usuarioMapeador.aRespuesta(usuario)).thenReturn(respuesta);

    var usuarios = usuarioServicio.listarTodos();

    assertThat(usuarios).extracting(UsuarioRespuestaDto::getId).containsExactly(20L);
  }
}
