package com.aquacomunidad.backend.features.chatbot.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquacomunidad.backend.features.chatbot.entity.ChatbotConversacionEntidad;

public interface ChatbotConversacionRepositorio extends JpaRepository<ChatbotConversacionEntidad, Long> {
  Optional<ChatbotConversacionEntidad> findByUsuarioIdAndFechaConversacion(Long usuarioId, LocalDate fechaConversacion);

  List<ChatbotConversacionEntidad> findTop30ByUsuarioIdOrderByFechaConversacionDesc(Long usuarioId);
}
