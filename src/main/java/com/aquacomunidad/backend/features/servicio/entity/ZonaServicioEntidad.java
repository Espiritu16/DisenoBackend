package com.aquacomunidad.backend.features.servicio.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "zona_servicio")
public class ZonaServicioEntidad {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_zona")
  private Long id;

  @Column(nullable = false, unique = true, length = 120)
  private String nombre;

  @Column(nullable = false, unique = true, length = 60)
  private String codigo;

  @Column(nullable = false)
  private Boolean activo;

  @Column(name = "creado_en", nullable = false)
  private LocalDateTime creadoEn;

  @PrePersist
  void prePersist() {
    if (activo == null) {
      activo = true;
    }
    if (creadoEn == null) {
      creadoEn = LocalDateTime.now();
    }
  }
}
