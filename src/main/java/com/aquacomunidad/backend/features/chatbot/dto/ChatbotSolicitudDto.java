package com.aquacomunidad.backend.features.chatbot.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatbotSolicitudDto(
    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 700, message = "El mensaje no debe superar 700 caracteres")
    String mensaje,

    @Size(max = 8, message = "El historial no debe superar 8 mensajes")
    List<@Valid ChatbotMensajeDto> historial,

    LocalDate fechaConversacion) {

  public ChatbotSolicitudDto(String mensaje, List<@Valid ChatbotMensajeDto> historial) {
    this(mensaje, historial, null);
  }
}
