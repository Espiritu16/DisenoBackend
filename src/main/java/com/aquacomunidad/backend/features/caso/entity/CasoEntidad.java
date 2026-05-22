package com.aquacomunidad.backend.features.caso.entity;

import java.time.LocalDateTime;

import com.aquacomunidad.backend.common.enums.EstadoCaso;
import com.aquacomunidad.backend.common.enums.PrioridadCaso;
import com.aquacomunidad.backend.features.reporte.entity.ReporteEntidad;
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
@Table(name = "caso_operativo")
public class CasoEntidad {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_caso")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_reporte_origen")
  private ReporteEntidad reporteOrigen;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_responsable")
  private UsuarioEntidad responsable;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PrioridadCaso prioridad;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EstadoCaso estado;

  @Column(length = 1500)
  private String observaciones;

  @Column(name = "evidencia_cierre", length = 600)
  private String evidenciaCierre;

  @Column(name = "fecha_asignacion", nullable = false)
  private LocalDateTime fechaAsignacion;

  @Column(name = "fecha_cierre")
  private LocalDateTime fechaCierre;

  @PrePersist
  public void prePersist() {
    this.fechaAsignacion = LocalDateTime.now();
    if (this.prioridad == null) {
      this.prioridad = PrioridadCaso.MEDIA;
    }
    if (this.estado == null) {
      this.estado = EstadoCaso.EN_PROCESO;
    }
  }
}
