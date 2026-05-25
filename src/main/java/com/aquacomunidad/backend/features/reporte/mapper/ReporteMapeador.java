package com.aquacomunidad.backend.features.reporte.mapper;

import org.springframework.stereotype.Component;

import com.aquacomunidad.backend.features.reporte.dto.ReporteSolicitudDto;
import com.aquacomunidad.backend.features.reporte.dto.ReporteRespuestaDto;
import com.aquacomunidad.backend.features.reporte.entity.CatalogoTipoIncidenciaEntidad;
import com.aquacomunidad.backend.features.reporte.entity.ReporteEntidad;
import com.aquacomunidad.backend.features.reporte.entity.ReporteImagenEntidad;
import com.aquacomunidad.backend.features.usuario.entity.UsuarioEntidad;

@Component
public class ReporteMapeador {

  public ReporteEntidad toEntidad(ReporteSolicitudDto dto, UsuarioEntidad usuario, CatalogoTipoIncidenciaEntidad tipo) {
    ReporteEntidad entity = new ReporteEntidad();
    entity.setUsuario(usuario);
    entity.setTipo(tipo);
    entity.setDescripcion(dto.getDescripcion());
    entity.setFotoUrl(dto.getFotoUrl());
    entity.setLat(dto.getLat());
    entity.setLng(dto.getLng());
    entity.setDireccion(dto.getDireccion());
    entity.setZona(dto.getZona());
    entity.setPosibleDuplicado(false);
    return entity;
  }

  public ReporteRespuestaDto aRespuesta(ReporteEntidad entity) {
    return ReporteRespuestaDto.builder()
        .id(entity.getId())
        .usuarioId(entity.getUsuario().getId())
        .tipo(entity.getTipo().getNombre())
        .descripcion(entity.getDescripcion())
        .fotoUrl(entity.getFotoUrl())
        .fotoUrls(entity.getImagenes().stream().map(ReporteImagenEntidad::getUrl).toList())
        .lat(entity.getLat())
        .lng(entity.getLng())
        .direccion(entity.getDireccion())
        .zona(entity.getZona())
        .posibleDuplicado(entity.getPosibleDuplicado())
        .estado(entity.getEstado())
        .fechaCreacion(entity.getFechaCreacion())
        .fechaActualizacion(entity.getFechaActualizacion())
        .build();
  }
}
