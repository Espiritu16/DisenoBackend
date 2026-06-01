package com.aquacomunidad.backend.features.reporte.repository;

import com.aquacomunidad.backend.common.enums.EstadoReporte;

public interface ReporteConteoEstado {
  EstadoReporte getEstado();

  long getTotal();
}
