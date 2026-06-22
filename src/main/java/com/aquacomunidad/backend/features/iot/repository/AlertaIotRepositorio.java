package com.aquacomunidad.backend.features.iot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquacomunidad.backend.common.enums.EstadoAlertaIot;
import com.aquacomunidad.backend.features.iot.entity.AlertaIotEntidad;

public interface AlertaIotRepositorio extends JpaRepository<AlertaIotEntidad, Long> {
  long countByEstado(EstadoAlertaIot estado);

  List<AlertaIotEntidad> findByEstadoOrderByCreadaEnDesc(EstadoAlertaIot estado);
}
