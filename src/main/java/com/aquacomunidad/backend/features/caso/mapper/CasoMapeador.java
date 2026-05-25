package com.aquacomunidad.backend.features.caso.mapper;

import org.springframework.stereotype.Component;

import com.aquacomunidad.backend.features.caso.dto.CasoRespuestaDto;
import com.aquacomunidad.backend.features.caso.entity.CasoEntidad;
import com.aquacomunidad.backend.features.caso.entity.CasoEvidenciaEntidad;

@Component
public class CasoMapeador {

  public CasoRespuestaDto aRespuesta(CasoEntidad entity) {
    return CasoRespuestaDto.builder()
        .id(entity.getId())
        .reporteId(entity.getReporteOrigen().getId())
        .responsableId(entity.getResponsable().getId())
        .prioridad(entity.getPrioridad())
        .estado(entity.getEstado())
        .observaciones(entity.getObservaciones())
        .evidenciaCierre(entity.getEvidenciaCierre())
        .evidenciaCierreUrls(entity.getEvidencias().stream().map(CasoEvidenciaEntidad::getUrl).toList())
        .fechaAsignacion(entity.getFechaAsignacion())
        .fechaCierre(entity.getFechaCierre())
        .build();
  }
}
