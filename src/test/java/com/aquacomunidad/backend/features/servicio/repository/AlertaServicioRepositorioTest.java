package com.aquacomunidad.backend.features.servicio.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.aquacomunidad.backend.common.enums.EstadoAlertaServicio;
import com.aquacomunidad.backend.common.enums.EstadoUsuario;
import com.aquacomunidad.backend.common.enums.RolUsuario;
import com.aquacomunidad.backend.common.enums.SeveridadAlertaServicio;
import com.aquacomunidad.backend.common.enums.TipoAlertaServicio;
import com.aquacomunidad.backend.features.servicio.entity.AlertaServicioEntidad;
import com.aquacomunidad.backend.features.servicio.entity.ZonaServicioEntidad;
import com.aquacomunidad.backend.features.usuario.entity.UsuarioEntidad;

import jakarta.persistence.EntityManager;

@DataJpaTest
class AlertaServicioRepositorioTest {

  @Autowired
  private AlertaServicioRepositorio repositorio;

  @Autowired
  private EntityManager entityManager;

  @Test
  void buscarVigentesIncluyeAlertasGlobalesSinZona() {
    UsuarioEntidad admin = nuevoAdmin();
    entityManager.persist(admin);

    ZonaServicioEntidad zona = nuevaZona();
    entityManager.persist(zona);

    AlertaServicioEntidad global = nuevaAlerta("Prueba global", admin);
    global.setCreadoEn(LocalDateTime.parse("2026-07-19T19:40:00"));
    global.setActualizadoEn(LocalDateTime.parse("2026-07-19T19:40:00"));
    entityManager.persist(global);

    AlertaServicioEntidad zonal = nuevaAlerta("Prueba zonal", admin);
    zonal.setZona(zona);
    zonal.setCreadoEn(LocalDateTime.parse("2026-07-18T19:40:00"));
    zonal.setActualizadoEn(LocalDateTime.parse("2026-07-18T19:40:00"));
    entityManager.persist(zonal);
    entityManager.flush();

    var alertas = repositorio.buscarVigentesPorZona(null);

    assertThat(alertas)
        .extracting(AlertaServicioEntidad::getTitulo)
        .contains("Prueba global", "Prueba zonal");
  }

  private UsuarioEntidad nuevoAdmin() {
    UsuarioEntidad admin = new UsuarioEntidad();
    admin.setNombre("Admin");
    admin.setCorreo("admin@test.local");
    admin.setPasswordHash("hash");
    admin.setRol(RolUsuario.ADMIN);
    admin.setEstado(EstadoUsuario.ACTIVO);
    return admin;
  }

  private ZonaServicioEntidad nuevaZona() {
    ZonaServicioEntidad zona = new ZonaServicioEntidad();
    zona.setNombre("San Miguel");
    zona.setCodigo("SAN_MIGUEL");
    zona.setActivo(true);
    return zona;
  }

  private AlertaServicioEntidad nuevaAlerta(String titulo, UsuarioEntidad admin) {
    AlertaServicioEntidad alerta = new AlertaServicioEntidad();
    alerta.setCreadoPor(admin);
    alerta.setTipo(TipoAlertaServicio.INFORMATIVA);
    alerta.setTitulo(titulo);
    alerta.setDescripcion("Descripcion de prueba");
    alerta.setSeveridad(SeveridadAlertaServicio.INFO);
    alerta.setEstado(EstadoAlertaServicio.ACTIVA);
    alerta.setIniciaEn(LocalDateTime.parse("2026-07-20T08:00:00"));
    alerta.setFinalizaEn(LocalDateTime.parse("2026-07-20T12:00:00"));
    return alerta;
  }
}
