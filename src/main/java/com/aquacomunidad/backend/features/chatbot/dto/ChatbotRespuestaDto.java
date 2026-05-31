package com.aquacomunidad.backend.features.chatbot.dto;

import java.time.LocalDate;
import java.util.List;

public record ChatbotRespuestaDto(
    String respuesta,
    String proveedor,
    String modelo,
    boolean iaDisponible,
    Long conversacionId,
    LocalDate fechaConversacion,
    boolean persistido,
    List<ChatbotAccionDto> acciones) {

  public ChatbotRespuestaDto(String respuesta, String proveedor, String modelo, boolean iaDisponible) {
    this(respuesta, proveedor, modelo, iaDisponible, null, null, false, List.of());
  }

  public ChatbotRespuestaDto(
      String respuesta,
      String proveedor,
      String modelo,
      boolean iaDisponible,
      Long conversacionId,
      LocalDate fechaConversacion,
      boolean persistido) {
    this(respuesta, proveedor, modelo, iaDisponible, conversacionId, fechaConversacion, persistido, List.of());
  }
}
