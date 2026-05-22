package com.aquacomunidad.backend.features.autenticacion.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquacomunidad.backend.common.enums.EstadoUsuario;
import com.aquacomunidad.backend.common.enums.RolUsuario;
import com.aquacomunidad.backend.exception.ExcepcionApi;
import com.aquacomunidad.backend.features.autenticacion.dto.CerrarSesionSolicitudDto;
import com.aquacomunidad.backend.features.autenticacion.dto.ConfirmarCodigoRecuperacionRespuestaDto;
import com.aquacomunidad.backend.features.autenticacion.dto.ConfirmarCodigoRecuperacionSolicitudDto;
import com.aquacomunidad.backend.features.autenticacion.dto.IniciarSesionRespuestaDto;
import com.aquacomunidad.backend.features.autenticacion.dto.IniciarSesionSolicitudDto;
import com.aquacomunidad.backend.features.autenticacion.dto.MensajeSimpleRespuestaDto;
import com.aquacomunidad.backend.features.autenticacion.dto.RefreshTokenSolicitudDto;
import com.aquacomunidad.backend.features.autenticacion.dto.RegistroSolicitudDto;
import com.aquacomunidad.backend.features.autenticacion.dto.RestablecerContrasenaSolicitudDto;
import com.aquacomunidad.backend.features.autenticacion.dto.SolicitarCodigoRecuperacionSolicitudDto;
import com.aquacomunidad.backend.features.autenticacion.entity.EstadoRefreshToken;
import com.aquacomunidad.backend.features.autenticacion.entity.EstadoTokenRecuperacion;
import com.aquacomunidad.backend.features.autenticacion.entity.RefreshTokenEntidad;
import com.aquacomunidad.backend.features.autenticacion.entity.TokenRecuperacionContrasenaEntidad;
import com.aquacomunidad.backend.features.autenticacion.repository.RefreshTokenRepositorio;
import com.aquacomunidad.backend.features.autenticacion.repository.TokenRecuperacionContrasenaRepositorio;
import com.aquacomunidad.backend.features.autenticacion.service.AutenticacionServicio;
import com.aquacomunidad.backend.features.autenticacion.support.GeneradorCodigoRecuperacion;
import com.aquacomunidad.backend.features.usuario.dto.UsuarioRespuestaDto;
import com.aquacomunidad.backend.features.usuario.entity.UsuarioEntidad;
import com.aquacomunidad.backend.features.usuario.repository.UsuarioRepositorio;
import com.aquacomunidad.backend.security.ServicioJwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AutenticacionServicioImpl implements AutenticacionServicio {

  private static final int MINUTOS_EXPIRACION_CODIGO = 15;
  private static final int MINUTOS_EXPIRACION_TOKEN_RESTABLECIMIENTO = 10;
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
  private static final ZoneId ZONA_APP = ZoneId.of("America/Lima");

  private final UsuarioRepositorio usuarioRepositorio;
  private final TokenRecuperacionContrasenaRepositorio tokenRecuperacionRepositorio;
  private final RefreshTokenRepositorio refreshTokenRepositorio;
  private final GeneradorCodigoRecuperacion generadorCodigoRecuperacion;
  private final PasswordEncoder passwordEncoder;
  private final JavaMailSender mailSender;
  private final ServicioJwt servicioJwt;

  @Override
  @Transactional
  public IniciarSesionRespuestaDto iniciarSesion(IniciarSesionSolicitudDto request) {
    UsuarioEntidad user = usuarioRepositorio.findByCorreoIgnoreCase(request.getCorreo())
        .orElseThrow(() -> new ExcepcionApi(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new ExcepcionApi(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
    }

    if (user.getEstado() != EstadoUsuario.ACTIVO) {
      throw new ExcepcionApi(HttpStatus.FORBIDDEN, "Cuenta inactiva");
    }

    return emitirSesion(user, UUID.randomUUID().toString());
  }

  @Override
  @Transactional
  public IniciarSesionRespuestaDto refrescarSesion(RefreshTokenSolicitudDto request) {
    Claims claims;
    try {
      claims = servicioJwt.extraerClaims(request.getRefreshToken());
    } catch (JwtException | IllegalArgumentException ex) {
      throw new ExcepcionApi(HttpStatus.UNAUTHORIZED, "Refresh token invalido");
    }

    String tokenType = String.valueOf(claims.get("tokenType"));
    if (!ServicioJwt.TOKEN_TYPE_REFRESH.equals(tokenType)) {
      throw new ExcepcionApi(HttpStatus.UNAUTHORIZED, "Token invalido para refresh");
    }

    String tokenId = claims.getId();
    String sessionId = String.valueOf(claims.get("sessionId"));
    if (tokenId == null || sessionId == null || "null".equals(sessionId)) {
      throw new ExcepcionApi(HttpStatus.UNAUTHORIZED, "Refresh token invalido");
    }

    RefreshTokenEntidad tokenPersistido = refreshTokenRepositorio.findByTokenId(tokenId)
        .orElseThrow(() -> new ExcepcionApi(HttpStatus.UNAUTHORIZED, "Refresh token invalido"));

    if (!MessageDigest.isEqual(
        tokenPersistido.getHashToken().getBytes(StandardCharsets.UTF_8),
        hashToken(request.getRefreshToken()).getBytes(StandardCharsets.UTF_8))) {
      revocarSesionCompleta(sessionId);
      throw new ExcepcionApi(HttpStatus.UNAUTHORIZED, "Se detecto uso indebido de token. Inicia sesion nuevamente");
    }

    if (tokenPersistido.getEstado() != EstadoRefreshToken.ACTIVO || tokenPersistido.getRevocadoEn() != null) {
      revocarSesionCompleta(sessionId);
      throw new ExcepcionApi(HttpStatus.UNAUTHORIZED, "Refresh token reutilizado o revocado");
    }

    if (LocalDateTime.now().isAfter(tokenPersistido.getExpiraEn())) {
      tokenPersistido.setEstado(EstadoRefreshToken.REVOCADO);
      tokenPersistido.setRevocadoEn(LocalDateTime.now());
      refreshTokenRepositorio.save(tokenPersistido);
      throw new ExcepcionApi(HttpStatus.UNAUTHORIZED, "Refresh token expirado");
    }

    UsuarioEntidad user = tokenPersistido.getUsuario();
    if (user.getEstado() != EstadoUsuario.ACTIVO) {
      throw new ExcepcionApi(HttpStatus.FORBIDDEN, "Cuenta inactiva");
    }

    IniciarSesionRespuestaDto sesionNueva = emitirSesion(user, sessionId);
    String nuevoTokenId = servicioJwt.extraerTokenId(sesionNueva.getRefreshToken());

    tokenPersistido.setEstado(EstadoRefreshToken.ROTADO);
    tokenPersistido.setRevocadoEn(LocalDateTime.now());
    tokenPersistido.setReemplazadoPorTokenId(nuevoTokenId);
    refreshTokenRepositorio.save(tokenPersistido);

    return sesionNueva;
  }

  @Override
  @Transactional
  public MensajeSimpleRespuestaDto cerrarSesion(CerrarSesionSolicitudDto request) {
    String sessionId;
    try {
      String tokenType = servicioJwt.extraerTipoToken(request.getRefreshToken());
      if (!ServicioJwt.TOKEN_TYPE_REFRESH.equals(tokenType)) {
        throw new ExcepcionApi(HttpStatus.UNAUTHORIZED, "Token invalido para cierre de sesion");
      }
      sessionId = servicioJwt.extraerSessionId(request.getRefreshToken());
    } catch (JwtException | IllegalArgumentException ex) {
      throw new ExcepcionApi(HttpStatus.UNAUTHORIZED, "Refresh token invalido");
    }

    revocarSesionCompleta(sessionId);

    return MensajeSimpleRespuestaDto.builder()
        .mensaje("Sesion cerrada correctamente")
        .build();
  }

  @Override
  @Transactional
  public UsuarioRespuestaDto registrar(RegistroSolicitudDto request) {
    if (usuarioRepositorio.existsByCorreoIgnoreCase(request.getCorreo())) {
      throw new ExcepcionApi(HttpStatus.CONFLICT, "El correo ya esta registrado");
    }

    UsuarioEntidad user = new UsuarioEntidad();
    user.setNombre(request.getNombre());
    user.setCorreo(request.getCorreo());
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setRol(RolUsuario.CIUDADANO);
    user.setEstado(EstadoUsuario.ACTIVO);

    UsuarioEntidad saved = usuarioRepositorio.save(user);

    return UsuarioRespuestaDto.builder()
        .id(saved.getId())
        .nombre(saved.getNombre())
        .correo(saved.getCorreo())
        .rol(saved.getRol())
        .estado(saved.getEstado())
        .build();
  }

  @Override
  @Transactional
  public MensajeSimpleRespuestaDto solicitarCodigoRecuperacion(SolicitarCodigoRecuperacionSolicitudDto request) {
    UsuarioEntidad user = usuarioRepositorio.findByCorreoIgnoreCase(request.getCorreo())
        .orElseThrow(() -> new ExcepcionApi(HttpStatus.NOT_FOUND, "No existe una cuenta con ese correo"));

    invalidarTokensActivos(user.getId());

    String codigo = generadorCodigoRecuperacion.generarCodigoSeisDigitos();

    TokenRecuperacionContrasenaEntidad token = new TokenRecuperacionContrasenaEntidad();
    token.setUsuario(user);
    token.setHashCodigo(passwordEncoder.encode(codigo));
    token.setExpiraEn(LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION_CODIGO));
    token.setEstado(EstadoTokenRecuperacion.PENDIENTE);
    token.setIntentos(0);
    token.setMaxIntentos(5);
    tokenRecuperacionRepositorio.save(token);

    enviarCorreoCodigo(user.getCorreo(), codigo);

    return MensajeSimpleRespuestaDto.builder()
        .mensaje("Se envio un codigo de recuperacion al correo registrado")
        .build();
  }

  @Override
  @Transactional
  public ConfirmarCodigoRecuperacionRespuestaDto confirmarCodigoRecuperacion(ConfirmarCodigoRecuperacionSolicitudDto request) {
    UsuarioEntidad user = usuarioRepositorio.findByCorreoIgnoreCase(request.getCorreo())
        .orElseThrow(() -> new ExcepcionApi(HttpStatus.NOT_FOUND, "No existe una cuenta con ese correo"));

    TokenRecuperacionContrasenaEntidad token = tokenRecuperacionRepositorio
        .findTopByUsuarioIdAndEstadoOrderByCreadoEnDesc(user.getId(), EstadoTokenRecuperacion.PENDIENTE)
        .orElseThrow(() -> new ExcepcionApi(HttpStatus.BAD_REQUEST, "No hay un codigo pendiente para validar"));

    validarNoExpirado(token);

    if (!passwordEncoder.matches(request.getCodigo(), token.getHashCodigo())) {
      token.setIntentos(token.getIntentos() + 1);
      if (token.getIntentos() >= token.getMaxIntentos()) {
        token.setEstado(EstadoTokenRecuperacion.CANCELADO);
      }
      tokenRecuperacionRepositorio.save(token);
      throw new ExcepcionApi(HttpStatus.BAD_REQUEST, "Codigo invalido");
    }

    String tokenPlanoRestablecimiento = UUID.randomUUID().toString();
    token.setHashTokenRestablecimiento(passwordEncoder.encode(tokenPlanoRestablecimiento));
    token.setExpiraTokenRestablecimientoEn(LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION_TOKEN_RESTABLECIMIENTO));
    token.setCodigoConfirmadoEn(LocalDateTime.now());
    token.setEstado(EstadoTokenRecuperacion.CODIGO_CONFIRMADO);
    tokenRecuperacionRepositorio.save(token);

    return ConfirmarCodigoRecuperacionRespuestaDto.builder()
        .tokenRecuperacion(tokenPlanoRestablecimiento)
        .expiraEn(token.getExpiraTokenRestablecimientoEn().format(DATE_TIME_FORMATTER))
        .build();
  }

  @Override
  @Transactional
  public MensajeSimpleRespuestaDto restablecerContrasena(RestablecerContrasenaSolicitudDto request) {
    if (!request.getNuevaContrasena().equals(request.getConfirmarContrasena())) {
      throw new ExcepcionApi(HttpStatus.BAD_REQUEST, "La confirmacion de contrasena no coincide");
    }

    UsuarioEntidad user = usuarioRepositorio.findByCorreoIgnoreCase(request.getCorreo())
        .orElseThrow(() -> new ExcepcionApi(HttpStatus.NOT_FOUND, "No existe una cuenta con ese correo"));

    TokenRecuperacionContrasenaEntidad token = tokenRecuperacionRepositorio
        .findTopByUsuarioIdAndEstadoOrderByCreadoEnDesc(user.getId(), EstadoTokenRecuperacion.CODIGO_CONFIRMADO)
        .orElseThrow(() -> new ExcepcionApi(HttpStatus.BAD_REQUEST, "No hay un token de recuperacion confirmado"));

    if (token.getExpiraTokenRestablecimientoEn() == null
        || LocalDateTime.now().isAfter(token.getExpiraTokenRestablecimientoEn())) {
      token.setEstado(EstadoTokenRecuperacion.EXPIRADO);
      tokenRecuperacionRepositorio.save(token);
      throw new ExcepcionApi(HttpStatus.BAD_REQUEST, "El token de recuperacion expiro");
    }

    if (!passwordEncoder.matches(request.getTokenRecuperacion(), token.getHashTokenRestablecimiento())) {
      throw new ExcepcionApi(HttpStatus.BAD_REQUEST, "Token de recuperacion invalido");
    }

    user.setPasswordHash(passwordEncoder.encode(request.getNuevaContrasena()));
    usuarioRepositorio.save(user);

    token.setEstado(EstadoTokenRecuperacion.USADO);
    token.setUsadoEn(LocalDateTime.now());
    tokenRecuperacionRepositorio.save(token);

    return MensajeSimpleRespuestaDto.builder()
        .mensaje("Contrasena restablecida correctamente")
        .build();
  }

  private IniciarSesionRespuestaDto emitirSesion(UsuarioEntidad user, String sessionId) {
    String accessToken = servicioJwt.generarAccessToken(user, sessionId);
    String refreshTokenId = UUID.randomUUID().toString();
    String refreshToken = servicioJwt.generarRefreshToken(user, sessionId, refreshTokenId);

    RefreshTokenEntidad refresh = new RefreshTokenEntidad();
    refresh.setUsuario(user);
    refresh.setTokenId(refreshTokenId);
    refresh.setSessionId(sessionId);
    refresh.setHashToken(hashToken(refreshToken));
    refresh.setExpiraEn(convertirALocalDateTime(servicioJwt.calcularExpiracionRefreshDesdeAhora()));
    refresh.setEstado(EstadoRefreshToken.ACTIVO);
    refreshTokenRepositorio.save(refresh);

    return IniciarSesionRespuestaDto.builder()
        .token(accessToken)
        .refreshToken(refreshToken)
        .userId(user.getId())
        .correo(user.getCorreo())
        .rol(user.getRol())
        .build();
  }

  private void revocarSesionCompleta(String sessionId) {
    List<RefreshTokenEntidad> tokensSesion = refreshTokenRepositorio.findBySessionId(sessionId);
    for (RefreshTokenEntidad token : tokensSesion) {
      if (token.getEstado() == EstadoRefreshToken.ACTIVO) {
        token.setEstado(EstadoRefreshToken.REVOCADO);
      }
      if (token.getRevocadoEn() == null) {
        token.setRevocadoEn(LocalDateTime.now());
      }
    }
    if (!tokensSesion.isEmpty()) {
      refreshTokenRepositorio.saveAll(tokensSesion);
    }
  }

  private String hashToken(String tokenPlano) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(tokenPlano.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(hash.length * 2);
      for (byte b : hash) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("No se pudo hashear el refresh token", ex);
    }
  }

  private LocalDateTime convertirALocalDateTime(Instant instant) {
    return LocalDateTime.ofInstant(instant, ZONA_APP);
  }

  private void enviarCorreoCodigo(String destino, String codigo) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(destino);
    message.setSubject("Codigo de recuperacion - AquaComunidad");
    message.setText("Tu codigo de recuperacion es: " + codigo
        + "\n\nEste codigo vence en " + MINUTOS_EXPIRACION_CODIGO + " minutos.");
    mailSender.send(message);
  }

  private void invalidarTokensActivos(Long usuarioId) {
    List<EstadoTokenRecuperacion> estadosActivos = List.of(
        EstadoTokenRecuperacion.PENDIENTE,
        EstadoTokenRecuperacion.CODIGO_CONFIRMADO);

    List<TokenRecuperacionContrasenaEntidad> tokensActivos = tokenRecuperacionRepositorio
        .findByUsuarioIdAndEstadoIn(usuarioId, estadosActivos);

    for (TokenRecuperacionContrasenaEntidad token : tokensActivos) {
      token.setEstado(EstadoTokenRecuperacion.CANCELADO);
    }

    if (!tokensActivos.isEmpty()) {
      tokenRecuperacionRepositorio.saveAll(tokensActivos);
    }
  }

  private void validarNoExpirado(TokenRecuperacionContrasenaEntidad token) {
    if (LocalDateTime.now().isAfter(token.getExpiraEn())) {
      token.setEstado(EstadoTokenRecuperacion.EXPIRADO);
      tokenRecuperacionRepositorio.save(token);
      throw new ExcepcionApi(HttpStatus.BAD_REQUEST, "El codigo de recuperacion expiro");
    }
  }
}
