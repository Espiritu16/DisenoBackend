package com.aquacomunidad.backend.features.reporte.entity;

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
@Table(name = "catalogo_tipo_incidencia")
public class CatalogoTipoIncidenciaEntidad {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_tipo")
  private Short id;

  @Column(nullable = false, unique = true, length = 50)
  private String codigo;

  @Column(nullable = false, length = 100)
  private String nombre;

  @Column(nullable = false)
  private Boolean activo;

  @Column(name = "creado_en", nullable = false)
  private LocalDateTime creadoEn;

  @PrePersist
  public void prePersist() {
    if (activo == null) {
      activo = true;
    }
    if (creadoEn == null) {
      creadoEn = LocalDateTime.now();
    }
  }
}
