package com.aquacomunidad.backend.features.servicio.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.aquacomunidad.backend.common.enums.TipoAlertaServicio;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class AlertaServicioSolicitudDtoTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void rechazaTituloYDescripcionConLongitudExcesiva() {
    AlertaServicioSolicitudDto request = new AlertaServicioSolicitudDto();
    request.setTipo(TipoAlertaServicio.INFORMATIVA);
    request.setTitulo("a".repeat(161));
    request.setDescripcion("b".repeat(801));

    var violations = validator.validate(request);

    assertThat(violations)
        .extracting(violation -> violation.getPropertyPath().toString())
        .contains("titulo", "descripcion");
  }
}
