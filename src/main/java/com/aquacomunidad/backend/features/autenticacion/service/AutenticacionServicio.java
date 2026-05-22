package com.aquacomunidad.backend.features.autenticacion.service;

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
import com.aquacomunidad.backend.features.usuario.dto.UsuarioRespuestaDto;

public interface AutenticacionServicio {

  IniciarSesionRespuestaDto iniciarSesion(IniciarSesionSolicitudDto request);
  IniciarSesionRespuestaDto refrescarSesion(RefreshTokenSolicitudDto request);
  MensajeSimpleRespuestaDto cerrarSesion(CerrarSesionSolicitudDto request);

  UsuarioRespuestaDto registrar(RegistroSolicitudDto request);

  MensajeSimpleRespuestaDto solicitarCodigoRecuperacion(SolicitarCodigoRecuperacionSolicitudDto request);

  ConfirmarCodigoRecuperacionRespuestaDto confirmarCodigoRecuperacion(ConfirmarCodigoRecuperacionSolicitudDto request);

  MensajeSimpleRespuestaDto restablecerContrasena(RestablecerContrasenaSolicitudDto request);
}
