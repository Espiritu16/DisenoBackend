package com.aquacomunidad.backend.features.iot.entity;

import java.time.LocalDateTime;

import com.aquacomunidad.backend.common.enums.EstadoAlertaIot;
import com.aquacomunidad.backend.common.enums.SeveridadAlertaIot;
import com.aquacomunidad.backend.common.enums.TipoAlertaIot;
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
@Table(name = "alerta_iot")
public class AlertaIotEntidad {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_alerta_iot")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_infraestructura")
  private InfraestructuraHidricaEntidad infraestructura;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_lectura")
  private LecturaIotEntidad lectura;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private TipoAlertaIot tipo;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private SeveridadAlertaIot severidad;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EstadoAlertaIot estado;

  @Column(nullable = false, length = 500)
  private String mensaje;

  @Column(name = "creada_en", nullable = false)
  private LocalDateTime creadaEn;

  @Column(name = "atendida_en")
  private LocalDateTime atendidaEn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "atendida_por")
  private UsuarioEntidad atendidaPor;

  @PrePersist
  void prePersist() {
    if (estado == null) {
      estado = EstadoAlertaIot.ACTIVA;
    }
    if (creadaEn == null) {
      creadaEn = LocalDateTime.now();
    }
  }
}
