package com.aquacomunidad.backend.features.servicio.service.impl;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquacomunidad.backend.common.enums.EstadoAlertaServicio;
import com.aquacomunidad.backend.common.enums.SeveridadAlertaServicio;
import com.aquacomunidad.backend.exception.ExcepcionApi;
import com.aquacomunidad.backend.features.servicio.dto.AlertaServicioRespuestaDto;
import com.aquacomunidad.backend.features.servicio.dto.AlertaServicioSolicitudDto;
import com.aquacomunidad.backend.features.servicio.entity.AlertaServicioEntidad;
import com.aquacomunidad.backend.features.servicio.entity.ZonaServicioEntidad;
import com.aquacomunidad.backend.features.servicio.repository.AlertaServicioRepositorio;
import com.aquacomunidad.backend.features.servicio.repository.ZonaServicioRepositorio;
import com.aquacomunidad.backend.features.servicio.service.ServicioEstadoServicio;
import com.aquacomunidad.backend.security.SeguridadContextoUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServicioEstadoServicioImpl implements ServicioEstadoServicio {

  private final AlertaServicioRepositorio alertaServicioRepositorio;
  private final ZonaServicioRepositorio zonaServicioRepositorio;

  @Override
  @Transactional(readOnly = true)
  public List<AlertaServicioRespuestaDto> listarAlertasVigentes(String zona) {
    String filtroZona = zona == null || zona.isBlank() ? null : zona.trim();
    return alertaServicioRepositorio.buscarVigentesPorZona(filtroZona).stream()
        .map(this::aRespuesta)
        .toList();
  }

  @Override
  @Transactional
  public AlertaServicioRespuestaDto crearAlerta(AlertaServicioSolicitudDto request) {
    AlertaServicioEntidad alerta = new AlertaServicioEntidad();
    alerta.setZona(resolverZona(request.getZonaId()));
    alerta.setTipo(request.getTipo());
    alerta.setTitulo(request.getTitulo().trim());
    alerta.setDescripcion(request.getDescripcion().trim());
    alerta.setSeveridad(request.getSeveridad() == null ? SeveridadAlertaServicio.INFO : request.getSeveridad());
    alerta.setEstado(request.getEstado() == null ? EstadoAlertaServicio.ACTIVA : request.getEstado());
    alerta.setIniciaEn(request.getIniciaEn());
    alerta.setFinalizaEn(request.getFinalizaEn());
    alerta.setCreadoPor(SeguridadContextoUtil.usuarioAutenticado());
    return aRespuesta(alertaServicioRepositorio.save(alerta));
  }

  private ZonaServicioEntidad resolverZona(Long zonaId) {
    if (zonaId == null) {
      return null;
    }
    return zonaServicioRepositorio.findById(zonaId)
        .orElseThrow(() -> new ExcepcionApi(HttpStatus.NOT_FOUND, "Zona no encontrada"));
  }

  private AlertaServicioRespuestaDto aRespuesta(AlertaServicioEntidad alerta) {
    return AlertaServicioRespuestaDto.builder()
        .id(alerta.getId())
        .tipo(alerta.getTipo())
        .titulo(alerta.getTitulo())
        .descripcion(alerta.getDescripcion())
        .severidad(alerta.getSeveridad())
        .estado(alerta.getEstado())
        .zona(alerta.getZona() == null ? null : alerta.getZona().getNombre())
        .iniciaEn(alerta.getIniciaEn())
        .finalizaEn(alerta.getFinalizaEn())
        .build();
  }
}
