package com.aquacomunidad.backend.features.autenticacion.entity;

import java.time.LocalDateTime;

import com.aquacomunidad.backend.features.usuario.entity.UsuarioEntidad;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "refresh_token")
public class RefreshTokenEntidad {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_refresh_token")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_usuario")
  private UsuarioEntidad usuario;

  @Column(name = "token_id", nullable = false, unique = true, length = 64)
  private String tokenId;

  @Column(name = "session_id", nullable = false, length = 64)
  private String sessionId;

  @Column(name = "hash_token", nullable = false, length = 128)
  private String hashToken;

  @Column(name = "expira_en", nullable = false)
  private LocalDateTime expiraEn;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EstadoRefreshToken estado;

  @Column(name = "reemplazado_por_token_id", length = 64)
  private String reemplazadoPorTokenId;

  @Column(name = "revocado_en")
  private LocalDateTime revocadoEn;

  @Column(name = "creado_en", nullable = false)
  private LocalDateTime creadoEn;

  @Column(name = "actualizado_en", nullable = false)
  private LocalDateTime actualizadoEn;

  @PrePersist
  public void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    this.creadoEn = now;
    this.actualizadoEn = now;
    if (this.estado == null) {
      this.estado = EstadoRefreshToken.ACTIVO;
    }
  }

  @PreUpdate
  public void preUpdate() {
    this.actualizadoEn = LocalDateTime.now();
  }
}
