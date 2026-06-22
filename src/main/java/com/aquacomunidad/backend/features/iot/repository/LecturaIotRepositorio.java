package com.aquacomunidad.backend.features.iot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aquacomunidad.backend.features.iot.entity.LecturaIotEntidad;

public interface LecturaIotRepositorio extends JpaRepository<LecturaIotEntidad, Long> {

  @EntityGraph(attributePaths = {"infraestructura", "infraestructura.zona"})
  Optional<LecturaIotEntidad> findTopByInfraestructuraIdOrderByLeidoEnDesc(Long infraestructuraId);
}
