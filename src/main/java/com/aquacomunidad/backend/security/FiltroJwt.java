package com.aquacomunidad.backend.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.aquacomunidad.backend.common.enums.RolUsuario;
import com.aquacomunidad.backend.features.usuario.entity.UsuarioEntidad;
import com.aquacomunidad.backend.features.usuario.repository.UsuarioRepositorio;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FiltroJwt extends OncePerRequestFilter {

  private final ServicioJwt servicioJwt;
  private final UsuarioRepositorio usuarioRepositorio;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(7);

    try {
      String tokenType = servicioJwt.extraerTipoToken(token);
      if (!ServicioJwt.TOKEN_TYPE_ACCESS.equals(tokenType)) {
        SecurityContextHolder.clearContext();
        filterChain.doFilter(request, response);
        return;
      }

      String correo = servicioJwt.extraerCorreo(token);
      Long idUsuario = servicioJwt.extraerIdUsuario(token);
      RolUsuario rol = servicioJwt.extraerRol(token);

      if (SecurityContextHolder.getContext().getAuthentication() == null) {
        UsuarioEntidad usuario = usuarioRepositorio.findById(idUsuario)
            .orElse(null);

        if (usuario != null && usuario.getCorreo().equalsIgnoreCase(correo) && usuario.getRol() == rol) {
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
              usuario,
              null,
              List.of(new SimpleGrantedAuthority("ROLE_" + rol.name())));
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
    } catch (JwtException | IllegalArgumentException ignored) {
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }
}
