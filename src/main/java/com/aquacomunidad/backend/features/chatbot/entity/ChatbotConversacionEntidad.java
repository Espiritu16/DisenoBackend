package com.aquacomunidad.backend.features.chatbot.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.aquacomunidad.backend.features.usuario.entity.UsuarioEntidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "chatbot_conversacion",
    uniqueConstraints = @UniqueConstraint(name = "uq_chatbot_conversacion_usuario_fecha", columnNames = {
        "id_usuario",
        "fecha_conversacion"
    }))
public class ChatbotConversacionEntidad {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_conversacion")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_usuario", nullable = false)
  private UsuarioEntidad usuario;

  @Column(name = "fecha_conversacion", nullable = false)
  private LocalDate fechaConversacion;

  @Column(length = 120)
  private String titulo;

  @Column(name = "creado_en", nullable = false)
  private LocalDateTime creadoEn;

  @Column(name = "actualizado_en", nullable = false)
  private LocalDateTime actualizadoEn;

  @PrePersist
  void alCrear() {
    LocalDateTime ahora = LocalDateTime.now();
    creadoEn = ahora;
    actualizadoEn = ahora;
  }

  @PreUpdate
  void alActualizar() {
    actualizadoEn = LocalDateTime.now();
  }
}
