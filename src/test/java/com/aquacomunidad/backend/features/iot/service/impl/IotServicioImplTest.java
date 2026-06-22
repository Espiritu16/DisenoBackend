package com.aquacomunidad.backend.features.iot.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aquacomunidad.backend.common.enums.EstadoAlertaIot;
import com.aquacomunidad.backend.common.enums.SeveridadAlertaIot;
import com.aquacomunidad.backend.common.enums.TipoAlertaIot;
import com.aquacomunidad.backend.common.enums.TipoInfraestructura;
import com.aquacomunidad.backend.features.iot.dto.LecturaIotSolicitudDto;
import com.aquacomunidad.backend.features.iot.entity.AlertaIotEntidad;
import com.aquacomunidad.backend.features.iot.entity.InfraestructuraHidricaEntidad;
import com.aquacomunidad.backend.features.iot.entity.LecturaIotEntidad;
import com.aquacomunidad.backend.features.iot.repository.AlertaIotRepositorio;
import com.aquacomunidad.backend.features.iot.repository.InfraestructuraHidricaRepositorio;
import com.aquacomunidad.backend.features.iot.repository.LecturaIotRepositorio;
import com.aquacomunidad.backend.features.servicio.entity.ZonaServicioEntidad;

@ExtendWith(MockitoExtension.class)
class IotServicioImplTest {

  @Mock
  private InfraestructuraHidricaRepositorio infraestructuraRepositorio;
  @Mock
  private LecturaIotRepositorio lecturaRepositorio;
  @Mock
  private AlertaIotRepositorio alertaIotRepositorio;

  @InjectMocks
  private IotServicioImpl servicio;

  @Test
  void registrarLecturaConNivelBajoGeneraAlertaActiva() {
    ZonaServicioEntidad zona = new ZonaServicioEntidad();
    zona.setNombre("Anexo Norte");

    InfraestructuraHidricaEntidad infraestructura = new InfraestructuraHidricaEntidad();
    infraestructura.setId(3L);
    infraestructura.setNombre("Reservorio Norte");
    infraestructura.setTipo(TipoInfraestructura.RESERVORIO);
    infraestructura.setZona(zona);

    LecturaIotEntidad guardada = new LecturaIotEntidad();
    guardada.setId(9L);
    guardada.setInfraestructura(infraestructura);
    guardada.setNivelPorcentaje(new BigDecimal("15.00"));
    guardada.setLeidoEn(LocalDateTime.parse("2026-06-22T08:00:00"));

    when(infraestructuraRepositorio.findById(3L)).thenReturn(Optional.of(infraestructura));
    when(lecturaRepositorio.save(any(LecturaIotEntidad.class))).thenReturn(guardada);

    LecturaIotSolicitudDto request = new LecturaIotSolicitudDto();
    request.setInfraestructuraId(3L);
    request.setNivelPorcentaje(new BigDecimal("15.00"));
    request.setVolumenLitros(new BigDecimal("3700"));
    request.setBateriaPorcentaje(new BigDecimal("82"));
    request.setSenalPorcentaje(new BigDecimal("75"));
    request.setLeidoEn(LocalDateTime.parse("2026-06-22T08:00:00"));

    var respuesta = servicio.registrarLectura(request);

    assertThat(respuesta.getNivelPorcentaje()).isEqualByComparingTo("15.00");
    ArgumentCaptor<AlertaIotEntidad> alertaCaptor = ArgumentCaptor.forClass(AlertaIotEntidad.class);
    verify(alertaIotRepositorio).save(alertaCaptor.capture());
    assertThat(alertaCaptor.getValue().getTipo()).isEqualTo(TipoAlertaIot.NIVEL_BAJO);
    assertThat(alertaCaptor.getValue().getSeveridad()).isEqualTo(SeveridadAlertaIot.ALTA);
    assertThat(alertaCaptor.getValue().getEstado()).isEqualTo(EstadoAlertaIot.ACTIVA);
  }

  @Test
  void listarNivelesDevuelveUltimaLecturaPorInfraestructura() {
    ZonaServicioEntidad zona = new ZonaServicioEntidad();
    zona.setNombre("Sector Centro");

    InfraestructuraHidricaEntidad infraestructura = new InfraestructuraHidricaEntidad();
    infraestructura.setId(1L);
    infraestructura.setNombre("Tanque Centro");
    infraestructura.setTipo(TipoInfraestructura.TANQUE);
    infraestructura.setZona(zona);

    LecturaIotEntidad lectura = new LecturaIotEntidad();
    lectura.setInfraestructura(infraestructura);
    lectura.setNivelPorcentaje(new BigDecimal("64.25"));
    lectura.setLeidoEn(LocalDateTime.parse("2026-06-22T07:30:00"));

    when(infraestructuraRepositorio.findByActivoTrue()).thenReturn(List.of(infraestructura));
    when(lecturaRepositorio.findTopByInfraestructuraIdOrderByLeidoEnDesc(1L)).thenReturn(Optional.of(lectura));

    var niveles = servicio.listarNiveles();

    assertThat(niveles).hasSize(1);
    assertThat(niveles.get(0).getNombre()).isEqualTo("Tanque Centro");
    assertThat(niveles.get(0).getZona()).isEqualTo("Sector Centro");
    assertThat(niveles.get(0).getEstado()).isEqualTo("NORMAL");
  }
}
