package com.aquacomunidad.backend.features.chatbot.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aquacomunidad.backend.common.response.RespuestaApi;
import com.aquacomunidad.backend.features.chatbot.dto.ChatbotConversacionResumenDto;
import com.aquacomunidad.backend.features.chatbot.dto.ChatbotHistorialDiaDto;
import com.aquacomunidad.backend.features.chatbot.dto.ChatbotRespuestaDto;
import com.aquacomunidad.backend.features.chatbot.dto.ChatbotSolicitudDto;
import com.aquacomunidad.backend.features.chatbot.service.ChatbotServicio;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
public class ChatbotControlador {

  private final ChatbotServicio chatbotServicio;

  @PostMapping("/mensajes")
  public ResponseEntity<RespuestaApi<ChatbotRespuestaDto>> responder(
      @Valid @RequestBody ChatbotSolicitudDto request,
      HttpServletRequest httpRequest) {
    return ResponseEntity.ok(RespuestaApi.ok(
        "Respuesta generada",
        chatbotServicio.responder(request),
        httpRequest.getRequestURI()));
  }

  @GetMapping("/conversaciones")
  public ResponseEntity<RespuestaApi<List<ChatbotConversacionResumenDto>>> listarConversaciones(
      HttpServletRequest httpRequest) {
    return ResponseEntity.ok(RespuestaApi.ok(
        "Conversaciones del chatbot",
        chatbotServicio.listarConversaciones(),
        httpRequest.getRequestURI()));
  }

  @GetMapping("/conversaciones/{fechaConversacion}/mensajes")
  public ResponseEntity<RespuestaApi<ChatbotHistorialDiaDto>> obtenerHistorial(
      @PathVariable LocalDate fechaConversacion,
      HttpServletRequest httpRequest) {
    return ResponseEntity.ok(RespuestaApi.ok(
        "Historial del chatbot",
        chatbotServicio.obtenerHistorial(fechaConversacion),
        httpRequest.getRequestURI()));
  }
}
