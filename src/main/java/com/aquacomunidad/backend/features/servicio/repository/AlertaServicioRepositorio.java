package com.aquacomunidad.backend.features.servicio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aquacomunidad.backend.features.servicio.entity.AlertaServicioEntidad;

public interface AlertaServicioRepositorio extends JpaRepository<AlertaServicioEntidad, Long> {

  @EntityGraph(attributePaths = {"zona", "creadoPor"})
  @Query("""
      select a
      from AlertaServicioEntidad a
      left join a.zona z
      where a.estado in (
        com.aquacomunidad.backend.common.enums.EstadoAlertaServicio.ACTIVA,
        com.aquacomunidad.backend.common.enums.EstadoAlertaServicio.PROGRAMADA
      )
        and (:zona is null or z is null or lower(z.nombre) = lower(:zona))
      order by a.creadoEn desc
      """)
  List<AlertaServicioEntidad> buscarVigentesPorZona(@Param("zona") String zona);
}
