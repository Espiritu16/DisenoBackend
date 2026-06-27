package com.aquacomunidad.backend.features.servicio.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aquacomunidad.backend.common.enums.EstadoAlertaServicio;
import com.aquacomunidad.backend.common.enums.SeveridadAlertaServicio;
import com.aquacomunidad.backend.common.enums.TipoAlertaServicio;
import com.aquacomunidad.backend.exception.ExcepcionApi;
import com.aquacomunidad.backend.features.servicio.dto.AlertaServicioSolicitudDto;
import com.aquacomunidad.backend.features.servicio.entity.AlertaServicioEntidad;
import com.aquacomunidad.backend.features.servicio.entity.ZonaServicioEntidad;
import com.aquacomunidad.backend.features.servicio.repository.AlertaServicioRepositorio;

@ExtendWith(MockitoExtension.class)
class ServicioEstadoServicioImplTest {

  @Mock
  private AlertaServicioRepositorio alertaServicioRepositorio;

  @InjectMocks
  private ServicioEstadoServicioImpl servicio;

  @Test
  void listarAlertasActivasDevuelveAlertasGlobalesYDeLaZona() {
    ZonaServicioEntidad zona = new ZonaServicioEntidad();
    zona.setNombre("San Isidro");

    AlertaServicioEntidad alerta = new AlertaServicioEntidad();
    alerta.setId(8L);
    alerta.setZona(zona);
    alerta.setTipo(TipoAlertaServicio.CORTE_PROGRAMADO);
    alerta.setTitulo("Corte programado");
    alerta.setDescripcion("Corte parcial por mantenimiento.");
    alerta.setSeveridad(SeveridadAlertaServicio.MEDIA);
    alerta.setEstado(EstadoAlertaServicio.PROGRAMADA);
    alerta.setIniciaEn(LocalDateTime.parse("2026-06-22T14:00:00"));
    alerta.setFinalizaEn(LocalDateTime.parse("2026-06-22T16:00:00"));

    when(alertaServicioRepositorio.buscarVigentesPorZona("San Isidro")).thenReturn(List.of(alerta));

    var alertas = servicio.listarAlertasVigentes("San Isidro");

    assertThat(alertas).hasSize(1);
    assertThat(alertas.get(0).getId()).isEqualTo(8L);
    assertThat(alertas.get(0).getZona()).isEqualTo("San Isidro");
    assertThat(alertas.get(0).getTipo()).isEqualTo(TipoAlertaServicio.CORTE_PROGRAMADO);
  }

  @Test
  void crearAlertaRechazaFechaFinalAnteriorAInicio() {
    AlertaServicioSolicitudDto request = new AlertaServicioSolicitudDto();
    request.setTipo(TipoAlertaServicio.CORTE_PROGRAMADO);
    request.setTitulo("Corte programado");
    request.setDescripcion("Corte parcial por mantenimiento.");
    request.setIniciaEn(LocalDateTime.parse("2026-06-22T16:00:00"));
    request.setFinalizaEn(LocalDateTime.parse("2026-06-22T14:00:00"));

    ExcepcionApi ex = assertThrows(ExcepcionApi.class, () -> servicio.crearAlerta(request));

    assertThat(ex.getMessage()).isEqualTo("La fecha de fin no puede ser anterior al inicio");
  }
}
