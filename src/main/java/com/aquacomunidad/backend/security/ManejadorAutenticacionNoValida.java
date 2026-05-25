package com.aquacomunidad.backend.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.aquacomunidad.backend.common.response.RespuestaApi;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ManejadorAutenticacionNoValida implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper = JsonMapper.builder()
      .addModule(new JavaTimeModule())
      .build();

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
      throws IOException, ServletException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write(objectMapper.writeValueAsString(
        RespuestaApi.error("No autenticado", null, request.getRequestURI())));
  }
}
