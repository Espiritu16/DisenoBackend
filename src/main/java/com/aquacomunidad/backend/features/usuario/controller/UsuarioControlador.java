package com.aquacomunidad.backend.features.usuario.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.aquacomunidad.backend.common.response.RespuestaApi;
import com.aquacomunidad.backend.features.usuario.dto.UsuarioActualizacionRolEstadoSolicitudDto;
import com.aquacomunidad.backend.features.usuario.dto.UsuarioSolicitudDto;
import com.aquacomunidad.backend.features.usuario.dto.UsuarioRespuestaDto;
import com.aquacomunidad.backend.features.usuario.service.UsuarioServicio;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioControlador {

  private final UsuarioServicio usuarioServicio;

  @PostMapping
  public ResponseEntity<RespuestaApi<UsuarioRespuestaDto>> crear(
      @Valid @RequestBody UsuarioSolicitudDto request,
      HttpServletRequest httpRequest) {
    UsuarioRespuestaDto data = usuarioServicio.crear(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(RespuestaApi.ok("Usuario creado", data, httpRequest.getRequestURI()));
  }

  @GetMapping
  public ResponseEntity<RespuestaApi<List<UsuarioRespuestaDto>>> listarTodos(HttpServletRequest httpRequest) {
    return ResponseEntity.ok(
        RespuestaApi.ok("Usuarios listados", usuarioServicio.listarTodos(), httpRequest.getRequestURI()));
  }

  @PatchMapping("/{id}/rol-estado")
  public ResponseEntity<RespuestaApi<UsuarioRespuestaDto>> actualizarRolEstado(
      @PathVariable Long id,
      @Valid @RequestBody UsuarioActualizacionRolEstadoSolicitudDto request,
      HttpServletRequest httpRequest) {
    return ResponseEntity.ok(RespuestaApi.ok(
        "Rol y estado de usuario actualizado",
        usuarioServicio.actualizarRolEstado(id, request),
        httpRequest.getRequestURI()));
  }
}
