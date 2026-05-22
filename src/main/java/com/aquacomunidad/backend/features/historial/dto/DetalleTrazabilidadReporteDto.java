package com.aquacomunidad.backend.features.historial.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DetalleTrazabilidadReporteDto {
  private Long reporteId;
  private Long casoId;
  private List<HistorialCambioDto> historialReporte;
  private List<HistorialCambioDto> historialCaso;
}
