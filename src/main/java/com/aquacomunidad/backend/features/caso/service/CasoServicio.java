package com.aquacomunidad.backend.features.caso.service;

import java.util.List;

import com.aquacomunidad.backend.common.enums.EstadoCaso;
import com.aquacomunidad.backend.features.caso.dto.CasoSolicitudDto;
import com.aquacomunidad.backend.features.caso.dto.CasoRespuestaDto;
import com.aquacomunidad.backend.features.caso.dto.CasoActualizacionDto;

public interface CasoServicio {
  CasoRespuestaDto crear(CasoSolicitudDto request);

  CasoRespuestaDto actualizarEstado(Long id, CasoActualizacionDto request);

  List<CasoRespuestaDto> listarTodos();

  List<CasoRespuestaDto> listarPorResponsable(Long responsableId);

  List<CasoRespuestaDto> listarPorEstado(EstadoCaso estado);
}
