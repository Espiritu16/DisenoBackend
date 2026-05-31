package com.aquacomunidad.backend.features.chatbot.service;

import java.time.LocalDate;
import java.util.List;

import com.aquacomunidad.backend.features.chatbot.dto.ChatbotConversacionResumenDto;
import com.aquacomunidad.backend.features.chatbot.dto.ChatbotHistorialDiaDto;
import com.aquacomunidad.backend.features.chatbot.dto.ChatbotRespuestaDto;
import com.aquacomunidad.backend.features.chatbot.dto.ChatbotSolicitudDto;

public interface ChatbotServicio {
  ChatbotRespuestaDto responder(ChatbotSolicitudDto solicitud);

  List<ChatbotConversacionResumenDto> listarConversaciones();

  ChatbotHistorialDiaDto obtenerHistorial(LocalDate fechaConversacion);
}
