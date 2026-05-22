package com.aquacomunidad.backend.features.caso.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aquacomunidad.backend.common.enums.EstadoCaso;
import com.aquacomunidad.backend.common.response.RespuestaApi;
import com.aquacomunidad.backend.features.caso.dto.CasoSolicitudDto;
import com.aquacomunidad.backend.features.caso.dto.CasoRespuestaDto;
import com.aquacomunidad.backend.features.caso.dto.CasoActualizacionDto;
import com.aquacomunidad.backend.features.caso.service.CasoServicio;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/casos")
@RequiredArgsConstructor
public class CasoControlador {

  private final CasoServicio casoServicio;

  @PostMapping
  public ResponseEntity<RespuestaApi<CasoRespuestaDto>> crear(
      @Valid @RequestBody CasoSolicitudDto request,
      HttpServletRequest httpRequest) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(RespuestaApi.ok("Caso creado", casoServicio.crear(request), httpRequest.getRequestURI()));
  }

  @PatchMapping("/{id}/estado")
  public ResponseEntity<RespuestaApi<CasoRespuestaDto>> actualizarEstado(
      @PathVariable Long id,
      @Valid @RequestBody CasoActualizacionDto request,
      HttpServletRequest httpRequest) {
    return ResponseEntity.ok(RespuestaApi.ok(
        "Estado de caso actualizado",
        casoServicio.actualizarEstado(id, request),
        httpRequest.getRequestURI()));
  }

  @GetMapping
  public ResponseEntity<RespuestaApi<List<CasoRespuestaDto>>> listarTodos(
      @RequestParam(required = false) Long responsableId,
      @RequestParam(required = false) EstadoCaso estado,
      HttpServletRequest httpRequest) {
    List<CasoRespuestaDto> data;
    if (responsableId != null) {
      data = casoServicio.listarPorResponsable(responsableId);
    } else if (estado != null) {
      data = casoServicio.listarPorEstado(estado);
    } else {
      data = casoServicio.listarTodos();
    }
    return ResponseEntity.ok(RespuestaApi.ok("Casos listados", data, httpRequest.getRequestURI()));
  }
}
