package com.aquacomunidad.backend.features.iot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aquacomunidad.backend.features.iot.entity.InfraestructuraHidricaEntidad;

public interface InfraestructuraHidricaRepositorio extends JpaRepository<InfraestructuraHidricaEntidad, Long> {

  @EntityGraph(attributePaths = {"zona"})
  List<InfraestructuraHidricaEntidad> findByActivoTrue();
}
