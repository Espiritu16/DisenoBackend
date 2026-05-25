package com.aquacomunidad.backend.features.upload.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aquacomunidad.backend.common.response.RespuestaApi;
import com.aquacomunidad.backend.features.upload.dto.ArchivoSubidoRespuestaDto;
import com.aquacomunidad.backend.features.upload.service.ArchivoServicio;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
public class ArchivoControlador {

  private final ArchivoServicio archivoServicio;

  @PostMapping("/reportes")
  public ResponseEntity<RespuestaApi<ArchivoSubidoRespuestaDto>> subirFotoReporte(
      @RequestParam(value = "file", required = false) MultipartFile file,
      @RequestParam(value = "files", required = false) List<MultipartFile> files,
      HttpServletRequest request) {
    return ResponseEntity.ok(RespuestaApi.ok(
        "Archivo de reporte subido",
        guardar(file, files, "reportes"),
        request.getRequestURI()));
  }

  @PostMapping("/casos")
  public ResponseEntity<RespuestaApi<ArchivoSubidoRespuestaDto>> subirEvidenciaCaso(
      @RequestParam(value = "file", required = false) MultipartFile file,
      @RequestParam(value = "files", required = false) List<MultipartFile> files,
      HttpServletRequest request) {
    return ResponseEntity.ok(RespuestaApi.ok(
        "Archivo de caso subido",
        guardar(file, files, "casos"),
        request.getRequestURI()));
  }

  private ArchivoSubidoRespuestaDto guardar(MultipartFile file, List<MultipartFile> files, String carpeta) {
    if (files != null && !files.isEmpty()) {
      return archivoServicio.guardarTodos(files, carpeta);
    }
    return archivoServicio.guardar(file, carpeta);
  }
}
