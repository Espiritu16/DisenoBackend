package com.aquacomunidad.backend.features.caso.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquacomunidad.backend.features.caso.entity.CasoEvidenciaEntidad;

public interface CasoEvidenciaRepositorio extends JpaRepository<CasoEvidenciaEntidad, Long> {
}
