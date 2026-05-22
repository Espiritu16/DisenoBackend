package com.aquacomunidad.backend.features.tablero.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquacomunidad.backend.common.enums.EstadoCaso;
import com.aquacomunidad.backend.common.enums.EstadoReporte;
import com.aquacomunidad.backend.features.caso.repository.CasoRepositorio;
import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto;
import com.aquacomunidad.backend.features.tablero.service.TableroServicio;
import com.aquacomunidad.backend.features.reporte.repository.ReporteRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TableroServicioImpl implements TableroServicio {

  private final ReporteRepositorio reporteRepositorio;
  private final CasoRepositorio casoRepositorio;

  @Override
  @Transactional(readOnly = true)
  public TableroKpiDto getKpis() {
    return TableroKpiDto.builder()
        .reportesPendientes(reporteRepositorio.findByEstado(EstadoReporte.PENDIENTE).size())
        .reportesEnProceso(reporteRepositorio.findByEstado(EstadoReporte.EN_PROCESO).size())
        .reportesResueltos(reporteRepositorio.findByEstado(EstadoReporte.RESUELTO).size())
        .casosAbiertos(casoRepositorio.findByEstado(EstadoCaso.EN_PROCESO).size())
        .build();
  }
}
