package com.aquacomunidad.backend.features.servicio.entity;

import java.time.LocalDateTime;

import com.aquacomunidad.backend.common.enums.EstadoAlertaServicio;
import com.aquacomunidad.backend.common.enums.SeveridadAlertaServicio;
import com.aquacomunidad.backend.common.enums.TipoAlertaServicio;
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
@Table(name = "alerta_servicio")
public class AlertaServicioEntidad {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_alerta")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_zona")
  private ZonaServicioEntidad zona;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private TipoAlertaServicio tipo;

  @Column(nullable = false, length = 160)
  private String titulo;

  @Column(nullable = false, length = 800)
  private String descripcion;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private SeveridadAlertaServicio severidad;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EstadoAlertaServicio estado;

  @Column(name = "inicia_en")
  private LocalDateTime iniciaEn;

  @Column(name = "finaliza_en")
  private LocalDateTime finalizaEn;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "creado_por")
  private UsuarioEntidad creadoPor;

  @Column(name = "creado_en", nullable = false)
  private LocalDateTime creadoEn;

  @Column(name = "actualizado_en", nullable = false)
  private LocalDateTime actualizadoEn;

  @PrePersist
  void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    if (estado == null) {
      estado = EstadoAlertaServicio.ACTIVA;
    }
    if (severidad == null) {
      severidad = SeveridadAlertaServicio.INFO;
    }
    creadoEn = now;
    actualizadoEn = now;
  }

  @PreUpdate
  void preUpdate() {
    actualizadoEn = LocalDateTime.now();
  }
}
