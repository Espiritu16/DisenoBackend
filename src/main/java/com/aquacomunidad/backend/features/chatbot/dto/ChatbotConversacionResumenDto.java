package com.aquacomunidad.backend.features.chatbot.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatbotConversacionResumenDto {
  private Long id;
  private LocalDate fechaConversacion;
  private String titulo;
  private int totalMensajes;
  private LocalDateTime actualizadoEn;
}
