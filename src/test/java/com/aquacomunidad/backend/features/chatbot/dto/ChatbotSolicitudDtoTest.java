package com.aquacomunidad.backend.features.chatbot.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class ChatbotSolicitudDtoTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void permiteHistorialConRespuestaDeIaMayorASetecientosCaracteres() {
    String respuestaLarga = "a".repeat(1200);
    ChatbotSolicitudDto solicitud = new ChatbotSolicitudDto(
        "Para que sirve el mapa?",
        List.of(new ChatbotMensajeDto("assistant", respuestaLarga)));

    var errores = validator.validate(solicitud);

    assertThat(errores).isEmpty();
  }

  @Test
  void mantieneLimiteDeSetecientosCaracteresParaMensajeActualDelUsuario() {
    ChatbotSolicitudDto solicitud = new ChatbotSolicitudDto("a".repeat(701), null);

    var errores = validator.validate(solicitud);

    assertThat(errores)
        .anyMatch(error -> error.getMessage().equals("El mensaje no debe superar 700 caracteres"));
  }
}
