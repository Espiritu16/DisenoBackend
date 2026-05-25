package com.aquacomunidad.backend.features.upload.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArchivoSubidoRespuestaDto {
  private List<ArchivoSubidoItemDto> archivos;
}
