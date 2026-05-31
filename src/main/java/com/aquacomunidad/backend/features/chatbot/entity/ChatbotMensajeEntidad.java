package com.aquacomunidad.backend.features.chatbot.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "chatbot_mensaje")
public class ChatbotMensajeEntidad {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_mensaje")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_conversacion", nullable = false)
  private ChatbotConversacionEntidad conversacion;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private RolMensajeChatbot rol;

  @Column(nullable = false, length = 2500)
  private String contenido;

  @Column(length = 40)
  private String proveedor;

  @Column(length = 80)
  private String modelo;

  @Column(name = "ia_disponible", nullable = false)
  private boolean iaDisponible;

  @Column(name = "creado_en", nullable = false)
  private LocalDateTime creadoEn;

  @PrePersist
  void alCrear() {
    creadoEn = LocalDateTime.now();
  }
}
