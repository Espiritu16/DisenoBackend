package com.aquacomunidad.backend.features.historial.entity;

import java.time.LocalDateTime;

import com.aquacomunidad.backend.common.enums.EstadoCaso;
import com.aquacomunidad.backend.features.caso.entity.CasoEntidad;
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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "historial_estado_caso")
public class HistorialEstadoCasoEntidad {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_historial")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_caso")
  private CasoEntidad caso;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado_anterior", length = 20)
  private EstadoCaso estadoAnterior;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado_nuevo", nullable = false, length = 20)
  private EstadoCaso estadoNuevo;

  @Column(name = "observacion", length = 800)
  private String observacion;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cambiado_por")
  private UsuarioEntidad cambiadoPor;

  @Column(name = "fecha_cambio", nullable = false)
  private LocalDateTime fechaCambio;

  @PrePersist
  public void prePersist() {
    if (this.fechaCambio == null) {
      this.fechaCambio = LocalDateTime.now();
    }
  }
}
