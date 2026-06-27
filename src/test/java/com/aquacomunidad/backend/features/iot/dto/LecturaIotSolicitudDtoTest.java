package com.aquacomunidad.backend.features.iot.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class LecturaIotSolicitudDtoTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void rechazaPorcentajesFueraDeRangoYVolumenNegativo() {
    LecturaIotSolicitudDto request = new LecturaIotSolicitudDto();
    request.setInfraestructuraId(1L);
    request.setNivelPorcentaje(new BigDecimal("50.00"));
    request.setBateriaPorcentaje(new BigDecimal("101.00"));
    request.setSenalPorcentaje(new BigDecimal("-1.00"));
    request.setVolumenLitros(new BigDecimal("-10.00"));

    var violations = validator.validate(request);

    assertThat(violations)
        .extracting(violation -> violation.getPropertyPath().toString())
        .contains("bateriaPorcentaje", "senalPorcentaje", "volumenLitros");
  }
}
