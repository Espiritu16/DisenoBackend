package com.aquacomunidad.backend.features.autenticacion.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aquacomunidad.backend.common.response.RespuestaApi;
import com.aquacomunidad.backend.features.autenticacion.dto.ConfirmarCodigoRecuperacionSolicitudDto;
import com.aquacomunidad.backend.features.autenticacion.dto.ConfirmarCodigoRecuperacionRespuestaDto;
import com.aquacomunidad.backend.features.autenticacion.dto.CerrarSesionSolicitudDto;
import com.aquacomunidad.backend.features.autenticacion.dto.IniciarSesionSolicitudDto;
import com.aquacomunidad.backend.features.autenticacion.dto.IniciarSesionRespuestaDto;
import com.aquacomunidad.backend.features.autenticacion.dto.MensajeSimpleRespuestaDto;
import com.aquacomunidad.backend.features.autenticacion.dto.RefreshTokenSolicitudDto;
import com.aquacomunidad.backend.features.autenticacion.dto.RegistroSolicitudDto;
import com.aquacomunidad.backend.features.autenticacion.dto.RestablecerContrasenaSolicitudDto;
import com.aquacomunidad.backend.features.autenticacion.dto.SolicitarCodigoRecuperacionSolicitudDto;
import com.aquacomunidad.backend.features.autenticacion.service.AutenticacionServicio;
import com.aquacomunidad.backend.features.usuario.dto.UsuarioRespuestaDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AutenticacionControlador {

  private final AutenticacionServicio authServicio;

  @PostMapping("/iniciarSesion")
  public ResponseEntity<RespuestaApi<IniciarSesionRespuestaDto>> iniciarSesion(
      @Valid @RequestBody IniciarSesionSolicitudDto request,
      HttpServletRequest httpRequest) {
    return ResponseEntity.ok(RespuestaApi.ok(
        "IniciarSesion exitoso",
        authServicio.iniciarSesion(request),
        httpRequest.getRequestURI()));
  }

  @PostMapping("/login")
  public ResponseEntity<RespuestaApi<IniciarSesionRespuestaDto>> loginAlias(
      @Valid @RequestBody IniciarSesionSolicitudDto request,
      HttpServletRequest httpRequest) {
    return iniciarSesion(request, httpRequest);
  }

  @PostMapping("/refresh")
  public ResponseEntity<RespuestaApi<IniciarSesionRespuestaDto>> refresh(
      @Valid @RequestBody RefreshTokenSolicitudDto request,
      HttpServletRequest httpRequest) {
    return ResponseEntity.ok(RespuestaApi.ok(
        "Sesion renovada",
        authServicio.refrescarSesion(request),
        httpRequest.getRequestURI()));
  }

  @PostMapping("/cerrarSesion")
  public ResponseEntity<RespuestaApi<MensajeSimpleRespuestaDto>> cerrarSesion(
      @Valid @RequestBody CerrarSesionSolicitudDto request,
      HttpServletRequest httpRequest) {
    return ResponseEntity.ok(RespuestaApi.ok(
        "Sesion cerrada",
        authServicio.cerrarSesion(request),
        httpRequest.getRequestURI()));
  }

  @PostMapping("/registrar")
  public ResponseEntity<RespuestaApi<UsuarioRespuestaDto>> registrar(
      @Valid @RequestBody RegistroSolicitudDto request,
      HttpServletRequest httpRequest) {
    return ResponseEntity.status(HttpStatus.CREATED).body(RespuestaApi.ok(
        "Cuenta creada",
        authServicio.registrar(request),
        httpRequest.getRequestURI()));
  }

  @PostMapping("/register")
  public ResponseEntity<RespuestaApi<UsuarioRespuestaDto>> registerAlias(
      @Valid @RequestBody RegistroSolicitudDto request,
      HttpServletRequest httpRequest) {
    return registrar(request, httpRequest);
  }

  @PostMapping("/recuperacion/solicitar-codigo")
  public ResponseEntity<RespuestaApi<MensajeSimpleRespuestaDto>> solicitarCodigo(
      @Valid @RequestBody SolicitarCodigoRecuperacionSolicitudDto request,
      HttpServletRequest httpRequest) {
    return ResponseEntity.ok(RespuestaApi.ok(
        "Codigo enviado",
        authServicio.solicitarCodigoRecuperacion(request),
        httpRequest.getRequestURI()));
  }

  @PostMapping("/recuperacion/confirmar-codigo")
  public ResponseEntity<RespuestaApi<ConfirmarCodigoRecuperacionRespuestaDto>> confirmarCodigo(
      @Valid @RequestBody ConfirmarCodigoRecuperacionSolicitudDto request,
      HttpServletRequest httpRequest) {
    return ResponseEntity.ok(RespuestaApi.ok(
        "Codigo validado",
        authServicio.confirmarCodigoRecuperacion(request),
        httpRequest.getRequestURI()));
  }

  @PostMapping("/recuperacion/restablecer-contrasena")
  public ResponseEntity<RespuestaApi<MensajeSimpleRespuestaDto>> restablecerContrasena(
      @Valid @RequestBody RestablecerContrasenaSolicitudDto request,
      HttpServletRequest httpRequest) {
    return ResponseEntity.ok(RespuestaApi.ok(
        "Contrasena actualizada",
        authServicio.restablecerContrasena(request),
        httpRequest.getRequestURI()));
  }
}
