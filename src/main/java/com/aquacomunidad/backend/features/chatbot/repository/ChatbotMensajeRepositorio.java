package com.aquacomunidad.backend.features.chatbot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquacomunidad.backend.features.chatbot.entity.ChatbotConversacionEntidad;
import com.aquacomunidad.backend.features.chatbot.entity.ChatbotMensajeEntidad;

public interface ChatbotMensajeRepositorio extends JpaRepository<ChatbotMensajeEntidad, Long> {
  List<ChatbotMensajeEntidad> findByConversacionOrderByCreadoEnAsc(ChatbotConversacionEntidad conversacion);

  List<ChatbotMensajeEntidad> findTop8ByConversacionOrderByCreadoEnDesc(ChatbotConversacionEntidad conversacion);

  int countByConversacion(ChatbotConversacionEntidad conversacion);
}
