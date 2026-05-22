package com.aquacomunidad.backend.features.autenticacion.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConfirmarCodigoRecuperacionRespuestaDto {
  private String tokenRecuperacion;
  private String expiraEn;
}
