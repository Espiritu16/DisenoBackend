package com.aquacomunidad.backend.features.servicio.service;

import java.util.List;

import com.aquacomunidad.backend.features.servicio.dto.AlertaServicioRespuestaDto;
import com.aquacomunidad.backend.features.servicio.dto.AlertaServicioSolicitudDto;

public interface ServicioEstadoServicio {
  List<AlertaServicioRespuestaDto> listarAlertasVigentes(String zona);

  AlertaServicioRespuestaDto crearAlerta(AlertaServicioSolicitudDto request);
}
