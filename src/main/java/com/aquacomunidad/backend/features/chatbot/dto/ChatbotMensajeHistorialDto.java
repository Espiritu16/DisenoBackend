package com.aquacomunidad.backend.features.chatbot.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatbotMensajeHistorialDto {
  private Long id;
  private String rol;
  private String contenido;
  private String proveedor;
  private String modelo;
  private boolean iaDisponible;
  private LocalDateTime creadoEn;
}
