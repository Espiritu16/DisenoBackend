package com.aquacomunidad.backend.features.iot.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.aquacomunidad.backend.common.enums.TipoInfraestructura;
import com.aquacomunidad.backend.features.servicio.entity.ZonaServicioEntidad;

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
@Table(name = "infraestructura_hidrica")
public class InfraestructuraHidricaEntidad {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_infraestructura")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_zona")
  private ZonaServicioEntidad zona;

  @Column(nullable = false, length = 140)
  private String nombre;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private TipoInfraestructura tipo;

  @Column(name = "capacidad_litros", precision = 12, scale = 2)
  private BigDecimal capacidadLitros;

  @Column(precision = 10, scale = 7)
  private BigDecimal lat;

  @Column(precision = 10, scale = 7)
  private BigDecimal lng;

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
