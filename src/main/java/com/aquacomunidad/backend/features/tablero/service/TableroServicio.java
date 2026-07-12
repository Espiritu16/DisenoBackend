package com.aquacomunidad.backend.features.tablero.service;

import java.time.LocalDate;

import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto;

public interface TableroServicio {
  TableroKpiDto getKpis();

  TableroKpiDto getKpis(LocalDate fechaDesde, LocalDate fechaHasta);
}
