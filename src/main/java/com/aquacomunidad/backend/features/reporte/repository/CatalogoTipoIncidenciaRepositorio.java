package com.aquacomunidad.backend.features.reporte.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquacomunidad.backend.features.reporte.entity.CatalogoTipoIncidenciaEntidad;

public interface CatalogoTipoIncidenciaRepositorio extends JpaRepository<CatalogoTipoIncidenciaEntidad, Short> {
  Optional<CatalogoTipoIncidenciaEntidad> findByCodigoIgnoreCaseAndActivoTrue(String codigo);

  Optional<CatalogoTipoIncidenciaEntidad> findByNombreIgnoreCaseAndActivoTrue(String nombre);
}
