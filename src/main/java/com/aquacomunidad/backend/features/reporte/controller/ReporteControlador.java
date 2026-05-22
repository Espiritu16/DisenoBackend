package com.aquacomunidad.backend.features.reporte.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aquacomunidad.backend.common.enums.EstadoReporte;
import com.aquacomunidad.backend.common.response.RespuestaApi;
import com.aquacomunidad.backend.common.enums.RolUsuario;
import com.aquacomunidad.backend.features.historial.dto.DetalleTrazabilidadReporteDto;
import com.aquacomunidad.backend.features.reporte.dto.ReporteSolicitudDto;
import com.aquacomunidad.backend.features.reporte.dto.ReporteRespuestaDto;
import com.aquacomunidad.backend.features.reporte.service.ReporteServicio;
import com.aquacomunidad.backend.security.SeguridadContextoUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class ReporteControlador {

  private final ReporteServicio reporteServicio;

  @PostMapping
  public ResponseEntity<RespuestaApi<ReporteRespuestaDto>> crear(
      @Valid @RequestBody ReporteSolicitudDto request,
      HttpServletRequest httpRequest) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(RespuestaApi.ok("Reporte creado", reporteServicio.crear(request), httpRequest.getRequestURI()));
  }

  @GetMapping
  public ResponseEntity<RespuestaApi<List<ReporteRespuestaDto>>> listarTodos(
      @RequestParam(required = false) Long usuarioId,
      @RequestParam(required = false) EstadoReporte estado,
      HttpServletRequest httpRequest) {
    List<ReporteRespuestaDto> data;
    if (usuarioId != null) {
      data = reporteServicio.listarPorUsuario(usuarioId);
    } else if (estado != null) {
      data = reporteServicio.listarPorEstado(estado);
    } else {
      data = reporteServicio.listarTodos();
    }
    return ResponseEntity.ok(RespuestaApi.ok("Reportes listados", data, httpRequest.getRequestURI()));
  }

  @GetMapping("/mis-reportes")
  public ResponseEntity<RespuestaApi<List<ReporteRespuestaDto>>> listarMisReportes(HttpServletRequest httpRequest) {
    Long usuarioId = SeguridadContextoUtil.idUsuarioAutenticado();
    return ResponseEntity.ok(RespuestaApi.ok(
        "Mis reportes listados",
        reporteServicio.listarMisReportes(usuarioId),
        httpRequest.getRequestURI()));
  }

  @GetMapping("/{id}/trazabilidad")
  public ResponseEntity<RespuestaApi<DetalleTrazabilidadReporteDto>> obtenerTrazabilidad(
      @PathVariable Long id,
      HttpServletRequest httpRequest) {
    var usuario = SeguridadContextoUtil.usuarioAutenticado();
    boolean esCiudadano = usuario.getRol() == RolUsuario.CIUDADANO;
    return ResponseEntity.ok(RespuestaApi.ok(
        "Trazabilidad obtenida",
        reporteServicio.obtenerDetalleTrazabilidad(id, usuario.getId(), esCiudadano),
        httpRequest.getRequestURI()));
  }
}
