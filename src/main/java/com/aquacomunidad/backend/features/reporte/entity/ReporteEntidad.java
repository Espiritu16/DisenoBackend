package com.aquacomunidad.backend.features.reporte.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.aquacomunidad.backend.common.enums.EstadoReporte;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reporte")
public class ReporteEntidad {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_reporte")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_usuario")
  private UsuarioEntidad usuario;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_tipo")
  private CatalogoTipoIncidenciaEntidad tipo;

  @Column(nullable = false, length = 1200)
  private String descripcion;

  @Column(name = "foto_url", nullable = false, length = 600)
  private String fotoUrl;

  @Column(nullable = false, precision = 10, scale = 7)
  private BigDecimal lat;

  @Column(nullable = false, precision = 10, scale = 7)
  private BigDecimal lng;

  @Column(nullable = false, length = 300)
  private String direccion;

  @Column(nullable = false, length = 120)
  private String zona;

  @Column(name = "posible_duplicado", nullable = false)
  private Boolean posibleDuplicado;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EstadoReporte estado;

  @Column(name = "fecha_creacion", nullable = false)
  private LocalDateTime fechaCreacion;

  @Column(name = "fecha_actualizacion", nullable = false)
  private LocalDateTime fechaActualizacion;

  @OneToMany(mappedBy = "reporte")
  private List<ReporteImagenEntidad> imagenes = new ArrayList<>();

  @PrePersist
  public void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    this.fechaCreacion = now;
    this.fechaActualizacion = now;
    if (this.estado == null) {
      this.estado = EstadoReporte.PENDIENTE;
    }
    if (this.posibleDuplicado == null) {
      this.posibleDuplicado = false;
    }
  }

  @PreUpdate
  public void preUpdate() {
    this.fechaActualizacion = LocalDateTime.now();
  }
}
