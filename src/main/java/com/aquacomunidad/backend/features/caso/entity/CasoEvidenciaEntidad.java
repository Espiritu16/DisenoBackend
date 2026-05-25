package com.aquacomunidad.backend.features.caso.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "caso_evidencia")
public class CasoEvidenciaEntidad {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_caso_evidencia")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_caso")
  private CasoEntidad caso;

  @Column(nullable = false, length = 800)
  private String url;

  @Column(name = "public_id", length = 200)
  private String publicId;

  @Column(nullable = false)
  private Integer orden;

  @Column(name = "fecha_creacion", nullable = false)
  private LocalDateTime fechaCreacion;

  @PrePersist
  public void prePersist() {
    if (fechaCreacion == null) {
      fechaCreacion = LocalDateTime.now();
    }
    if (orden == null) {
      orden = 0;
    }
  }
}
