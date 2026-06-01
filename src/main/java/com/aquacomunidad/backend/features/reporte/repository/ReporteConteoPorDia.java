package com.aquacomunidad.backend.features.reporte.repository;

import java.time.LocalDate;

public interface ReporteConteoPorDia {
  LocalDate getFecha();

  long getTotal();
}
