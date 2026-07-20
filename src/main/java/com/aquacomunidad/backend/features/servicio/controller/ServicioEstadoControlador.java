package com.aquacomunidad.backend.features.servicio.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aquacomunidad.backend.common.response.RespuestaApi;
import com.aquacomunidad.backend.features.servicio.dto.AlertaServicioRespuestaDto;
import com.aquacomunidad.backend.features.servicio.dto.AlertaServicioSolicitudDto;
import com.aquacomunidad.backend.features.servicio.dto.ZonaServicioRespuestaDto;
import com.aquacomunidad.backend.features.servicio.service.ServicioEstadoServicio;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/estado-servicio")
@RequiredArgsConstructor
public class ServicioEstadoControlador {

  private final ServicioEstadoServicio servicioEstadoServicio;

  @GetMapping("/alertas")
  public ResponseEntity<RespuestaApi<List<AlertaServicioRespuestaDto>>> listarAlertas(
      @RequestParam(required = false) String zona,
      HttpServletRequest request) {
    return ResponseEntity.ok(RespuestaApi.ok(
        "Alertas del servicio listadas",
        servicioEstadoServicio.listarAlertasVigentes(zona),
        request.getRequestURI()));
  }

  @GetMapping("/zonas")
  public ResponseEntity<RespuestaApi<List<ZonaServicioRespuestaDto>>> listarZonas(HttpServletRequest request) {
    return ResponseEntity.ok(RespuestaApi.ok(
        "Zonas del servicio listadas",
        servicioEstadoServicio.listarZonasActivas(),
        request.getRequestURI()));
  }

  @PostMapping("/alertas")
  public ResponseEntity<RespuestaApi<AlertaServicioRespuestaDto>> crearAlerta(
      @Valid @RequestBody AlertaServicioSolicitudDto body,
      HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(RespuestaApi.ok(
        "Alerta del servicio creada",
        servicioEstadoServicio.crearAlerta(body),
        request.getRequestURI()));
  }
}
