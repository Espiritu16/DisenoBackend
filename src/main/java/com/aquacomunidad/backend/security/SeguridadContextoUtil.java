package com.aquacomunidad.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.aquacomunidad.backend.exception.ExcepcionApi;
import com.aquacomunidad.backend.features.usuario.entity.UsuarioEntidad;

import org.springframework.http.HttpStatus;

public final class SeguridadContextoUtil {

  private SeguridadContextoUtil() {
  }

  public static UsuarioEntidad usuarioAutenticado() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioEntidad usuario)) {
      throw new ExcepcionApi(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
    }
    return usuario;
  }

  public static Long idUsuarioAutenticado() {
    return usuarioAutenticado().getId();
  }
}
