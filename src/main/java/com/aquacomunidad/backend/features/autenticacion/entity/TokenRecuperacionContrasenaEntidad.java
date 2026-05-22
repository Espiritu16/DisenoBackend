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
@Table(name = "token_recuperacion_contrasena")
public class TokenRecuperacionContrasenaEntidad {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_token")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_usuario")
  private UsuarioEntidad usuario;

  @Column(name = "hash_codigo", nullable = false, length = 255)
  private String hashCodigo;

  @Column(name = "hash_token_restablecimiento", length = 255)
  private String hashTokenRestablecimiento;

  @Column(name = "expira_en", nullable = false)
  private LocalDateTime expiraEn;

  @Column(name = "expira_token_restablecimiento_en")
  private LocalDateTime expiraTokenRestablecimientoEn;

  @Column(name = "usado_en")
  private LocalDateTime usadoEn;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EstadoTokenRecuperacion estado;

  @Column(nullable = false)
  private Integer intentos;

  @Column(name = "max_intentos", nullable = false)
  private Integer maxIntentos;

  @Column(name = "codigo_confirmado_en")
  private LocalDateTime codigoConfirmadoEn;

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
      this.estado = EstadoTokenRecuperacion.PENDIENTE;
    }
    if (this.intentos == null) {
      this.intentos = 0;
    }
    if (this.maxIntentos == null) {
      this.maxIntentos = 5;
    }
  }

  @PreUpdate
  public void preUpdate() {
    this.actualizadoEn = LocalDateTime.now();
  }
}
