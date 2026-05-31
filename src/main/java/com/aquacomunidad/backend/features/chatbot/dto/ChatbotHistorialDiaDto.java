package com.aquacomunidad.backend.features.chatbot.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatbotHistorialDiaDto {
  private Long conversacionId;
  private LocalDate fechaConversacion;
  private List<ChatbotMensajeHistorialDto> mensajes;
}
