package com.aquacomunidad.backend.features.iot.entity;

import java.math.BigDecimal;
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
@Table(name = "lectura_iot")
public class LecturaIotEntidad {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_lectura")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_infraestructura")
  private InfraestructuraHidricaEntidad infraestructura;

  @Column(name = "nivel_porcentaje", nullable = false, precision = 5, scale = 2)
  private BigDecimal nivelPorcentaje;

  @Column(name = "volumen_litros", precision = 12, scale = 2)
  private BigDecimal volumenLitros;

  @Column(name = "bateria_porcentaje", precision = 5, scale = 2)
  private BigDecimal bateriaPorcentaje;

  @Column(name = "senal_porcentaje", precision = 5, scale = 2)
  private BigDecimal senalPorcentaje;

  @Column(name = "leido_en", nullable = false)
  private LocalDateTime leidoEn;

  @Column(name = "recibido_en", nullable = false)
  private LocalDateTime recibidoEn;

  @PrePersist
  void prePersist() {
    if (recibidoEn == null) {
      recibidoEn = LocalDateTime.now();
    }
    if (leidoEn == null) {
      leidoEn = recibidoEn;
    }
  }
}
