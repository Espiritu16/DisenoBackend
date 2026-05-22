package com.aquacomunidad.backend.features.tablero.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aquacomunidad.backend.common.response.RespuestaApi;
import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto;
import com.aquacomunidad.backend.features.tablero.service.TableroServicio;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class TableroControlador {

  private final TableroServicio dashboardServicio;

  @GetMapping("/kpis")
  public ResponseEntity<RespuestaApi<TableroKpiDto>> getKpis(HttpServletRequest request) {
    return ResponseEntity.ok(
        RespuestaApi.ok("KPIs obtenidos", dashboardServicio.getKpis(), request.getRequestURI()));
  }
}
