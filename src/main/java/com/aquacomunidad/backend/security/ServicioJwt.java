package com.aquacomunidad.backend.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aquacomunidad.backend.common.enums.RolUsuario;
import com.aquacomunidad.backend.features.usuario.entity.UsuarioEntidad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class ServicioJwt {

  public static final String TOKEN_TYPE_ACCESS = "ACCESS";
  public static final String TOKEN_TYPE_REFRESH = "REFRESH";

  private final SecretKey secretKey;
  private final long accessExpiracionSegundos;
  private final long refreshExpiracionSegundos;

  public ServicioJwt(
      @Value("${app.jwt.secreto}") String secreto,
      @Value("${app.jwt.expiracion-segundos}") long accessExpiracionSegundos,
      @Value("${app.jwt.refresh-expiracion-segundos}") long refreshExpiracionSegundos) {
    this.secretKey = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    this.accessExpiracionSegundos = accessExpiracionSegundos;
    this.refreshExpiracionSegundos = refreshExpiracionSegundos;
  }

  public String generarAccessToken(UsuarioEntidad usuario, String sessionId) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(usuario.getCorreo())
        .claim("idUsuario", usuario.getId())
        .claim("rol", usuario.getRol().name())
        .claim("tokenType", TOKEN_TYPE_ACCESS)
        .claim("sessionId", sessionId)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(accessExpiracionSegundos)))
        .signWith(secretKey)
        .compact();
  }

  public String generarRefreshToken(UsuarioEntidad usuario, String sessionId, String tokenId) {
    Instant now = Instant.now();
    return Jwts.builder()
        .id(tokenId)
        .subject(usuario.getCorreo())
        .claim("idUsuario", usuario.getId())
        .claim("rol", usuario.getRol().name())
        .claim("tokenType", TOKEN_TYPE_REFRESH)
        .claim("sessionId", sessionId)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(refreshExpiracionSegundos)))
        .signWith(secretKey)
        .compact();
  }

  public Instant calcularExpiracionRefreshDesdeAhora() {
    return Instant.now().plusSeconds(refreshExpiracionSegundos);
  }

  public Claims extraerClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public String extraerCorreo(String token) {
    return extraerClaims(token).getSubject();
  }

  public Long extraerIdUsuario(String token) {
    Object id = extraerClaims(token).get("idUsuario");
    if (id instanceof Integer integerId) {
      return integerId.longValue();
    }
    if (id instanceof Long longId) {
      return longId;
    }
    return Long.valueOf(String.valueOf(id));
  }

  public RolUsuario extraerRol(String token) {
    return RolUsuario.valueOf(String.valueOf(extraerClaims(token).get("rol")));
  }

  public String extraerTipoToken(String token) {
    return String.valueOf(extraerClaims(token).get("tokenType"));
  }

  public String extraerSessionId(String token) {
    return String.valueOf(extraerClaims(token).get("sessionId"));
  }

  public String extraerTokenId(String token) {
    return extraerClaims(token).getId();
  }
}
