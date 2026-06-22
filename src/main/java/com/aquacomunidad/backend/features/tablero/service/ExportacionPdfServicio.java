package com.aquacomunidad.backend.features.tablero.service;

import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto;

public interface ExportacionPdfServicio {
  byte[] generarReporteDashboard(TableroKpiDto kpis);
}
