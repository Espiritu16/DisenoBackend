package com.aquacomunidad.backend.features.servicio.service;

import java.util.List;

import com.aquacomunidad.backend.features.servicio.dto.AlertaServicioRespuestaDto;
import com.aquacomunidad.backend.features.servicio.dto.AlertaServicioSolicitudDto;
import com.aquacomunidad.backend.features.servicio.dto.ZonaServicioRespuestaDto;

public interface ServicioEstadoServicio {
  List<AlertaServicioRespuestaDto> listarAlertasVigentes(String zona);

  List<ZonaServicioRespuestaDto> listarZonasActivas();

  AlertaServicioRespuestaDto crearAlerta(AlertaServicioSolicitudDto request);
}
