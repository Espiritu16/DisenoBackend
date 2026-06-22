package com.aquacomunidad.backend.features.tablero.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto;

class ExportacionPdfServicioImplTest {

  private final ExportacionPdfServicioImpl service = new ExportacionPdfServicioImpl();

  @Test
  void generarReporteDashboardDebeRetornarPdfValido() {
    TableroKpiDto kpis = TableroKpiDto.builder()
        .reportesPendientes(2)
        .reportesEnProceso(3)
        .reportesResueltos(5)
        .casosAbiertos(1)
        .casosResueltos(4)
        .promedioHorasResolucion(12.5)
        .actividadSemanal(List.of())
        .reportesPorZona(List.of())
        .tiemposPorZona(List.of())
        .zonasCriticas(List.of())
        .nivelesAgua(List.of())
        .build();

    byte[] pdf = service.generarReporteDashboard(kpis);

    assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    assertThat(new String(pdf, StandardCharsets.ISO_8859_1)).contains("AquaComunidad");
    assertThat(pdf).endsWith("%%EOF".getBytes(StandardCharsets.US_ASCII));
  }
}
