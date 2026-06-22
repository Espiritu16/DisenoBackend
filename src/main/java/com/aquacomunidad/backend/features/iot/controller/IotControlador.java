package com.aquacomunidad.backend.features.iot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aquacomunidad.backend.common.response.RespuestaApi;
import com.aquacomunidad.backend.features.iot.dto.LecturaIotSolicitudDto;
import com.aquacomunidad.backend.features.iot.dto.NivelAguaDto;
import com.aquacomunidad.backend.features.iot.service.IotServicio;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/iot")
@RequiredArgsConstructor
public class IotControlador {

  private final IotServicio iotServicio;

  @GetMapping("/niveles")
  public ResponseEntity<RespuestaApi<List<NivelAguaDto>>> listarNiveles(HttpServletRequest request) {
    return ResponseEntity.ok(RespuestaApi.ok("Niveles IoT listados", iotServicio.listarNiveles(), request.getRequestURI()));
  }

  @PostMapping("/lecturas")
  public ResponseEntity<RespuestaApi<NivelAguaDto>> registrarLectura(
      @Valid @RequestBody LecturaIotSolicitudDto body,
      HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(RespuestaApi.ok("Lectura IoT registrada", iotServicio.registrarLectura(body), request.getRequestURI()));
  }
}
