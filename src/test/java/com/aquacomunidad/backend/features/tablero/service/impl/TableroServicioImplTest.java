package com.aquacomunidad.backend.features.tablero.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.aquacomunidad.backend.common.enums.EstadoCaso;
import com.aquacomunidad.backend.common.enums.EstadoReporte;
import com.aquacomunidad.backend.features.caso.entity.CasoEntidad;
import com.aquacomunidad.backend.features.caso.repository.CasoRepositorio;
import com.aquacomunidad.backend.features.iot.dto.NivelAguaDto;
import com.aquacomunidad.backend.features.iot.service.IotServicio;
import com.aquacomunidad.backend.features.reporte.entity.CatalogoTipoIncidenciaEntidad;
import com.aquacomunidad.backend.features.reporte.entity.ReporteEntidad;
import com.aquacomunidad.backend.features.reporte.repository.ReporteConteoEstado;
import com.aquacomunidad.backend.features.reporte.repository.ReporteConteoPorZona;
import com.aquacomunidad.backend.features.reporte.repository.ReporteRepositorio;

@ExtendWith(MockitoExtension.class)
class TableroServicioImplTest {

  @Mock
  private ReporteRepositorio reporteRepositorio;
  @Mock
  private CasoRepositorio casoRepositorio;
  @Mock
  private IotServicio iotServicio;

  @Test
  void getKpisIncluyeIndicadoresConsolidadosYZonasCriticas() {
    when(reporteRepositorio.contarPorEstadoGlobal()).thenReturn(List.of(
        conteoEstado(EstadoReporte.PENDIENTE, 2),
        conteoEstado(EstadoReporte.EN_PROCESO, 3),
        conteoEstado(EstadoReporte.RESUELTO, 5)));
    when(casoRepositorio.countByEstado(EstadoCaso.EN_PROCESO)).thenReturn(4L);
    when(casoRepositorio.findByEstado(EstadoCaso.RESUELTO)).thenReturn(List.of(
        casoResuelto("Sector 4", 10),
        casoResuelto("Sector 4", 20),
        casoResuelto("Centro", 5)));
    when(reporteRepositorio.contarZonas(org.mockito.ArgumentMatchers.any(Pageable.class))).thenReturn(List.of(
        conteoZona("Sector 4", 8),
        conteoZona("Centro", 2)));
    when(reporteRepositorio.findAll()).thenReturn(List.of(
        reporte("Sector 4", "Fuga de agua", LocalDateTime.parse("2026-07-02T08:00:00")),
        reporte("Sector 4", "Fuga de agua", LocalDateTime.parse("2026-08-02T08:00:00")),
        reporte("Sector 4", "Baja presión", LocalDateTime.parse("2026-08-03T08:00:00")),
        reporte("Centro", "Agua turbia", LocalDateTime.parse("2026-09-02T08:00:00")),
        reporte("Centro", "Fuga de agua", LocalDateTime.parse("2026-09-03T08:00:00")),
        reporte("Centro", "Fuga de agua", LocalDateTime.parse("2026-09-04T08:00:00"))));
    when(reporteRepositorio.findByFechaCreacionBetweenOrderByFechaCreacionAsc(
        org.mockito.ArgumentMatchers.any(LocalDateTime.class),
        org.mockito.ArgumentMatchers.any(LocalDateTime.class))).thenReturn(List.of(
            reporte("Sector 4", "Fuga de agua", LocalDateTime.now().minusDays(4)),
            reporte("Sector 4", "Fuga de agua", LocalDateTime.now().minusDays(2)),
            reporte("Centro", "Agua turbia", LocalDateTime.now().minusDays(20))));
    when(iotServicio.listarNiveles()).thenReturn(List.of(NivelAguaDto.builder()
        .infraestructuraId(1L)
        .nombre("Reservorio Norte")
        .zona("Sector 4")
        .estado("BAJO")
        .build()));

    var servicio = new TableroServicioImpl(reporteRepositorio, casoRepositorio, iotServicio);

    var kpis = servicio.getKpis();

    assertThat(kpis.getCasosResueltos()).isEqualTo(3);
    assertThat(kpis.getTotalReportes()).isEqualTo(6);
    assertThat(kpis.getPromedioHorasResolucion()).isEqualTo(11.67);
    assertThat(kpis.getReportesPorMes()).extracting("mes").contains("Jul", "Ago", "Sep");
    assertThat(kpis.getReportesPorCategoria()).first().extracting("categoria").isEqualTo("Fuga de agua");
    assertThat(kpis.getReportesPorEstado()).extracting("estado").contains("Pendientes", "Resueltos");
    assertThat(kpis.getProyeccionMensual()).hasSize(3);
    assertThat(kpis.getProyeccionMensual()).extracting("mes").containsExactly("Oct", "Nov", "Dic");
    assertThat(kpis.getRecomendacionAutomatica()).contains("Priorizar cuadrillas");
    assertThat(kpis.getTiemposPorZona()).extracting("zona").contains("Sector 4", "Centro");
    assertThat(kpis.getZonasCriticas()).first().extracting("zona").isEqualTo("Sector 4");
    assertThat(kpis.getNivelesAgua()).hasSize(1);
  }

  private ReporteConteoEstado conteoEstado(EstadoReporte estado, long total) {
    return new ReporteConteoEstado() {
      @Override
      public EstadoReporte getEstado() {
        return estado;
      }

      @Override
      public long getTotal() {
        return total;
      }
    };
  }

  private ReporteConteoPorZona conteoZona(String zona, long total) {
    return new ReporteConteoPorZona() {
      @Override
      public String getZona() {
        return zona;
      }

      @Override
      public long getTotal() {
        return total;
      }
    };
  }

  private CasoEntidad casoResuelto(String zona, int horas) {
    ReporteEntidad reporte = new ReporteEntidad();
    reporte.setZona(zona);
    CasoEntidad caso = new CasoEntidad();
    caso.setReporteOrigen(reporte);
    caso.setEstado(EstadoCaso.RESUELTO);
    caso.setFechaAsignacion(LocalDateTime.parse("2026-06-20T08:00:00"));
    caso.setFechaCierre(caso.getFechaAsignacion().plusHours(horas));
    return caso;
  }

  private ReporteEntidad reporte(String zona, String tipoNombre, LocalDateTime fecha) {
    CatalogoTipoIncidenciaEntidad tipo = new CatalogoTipoIncidenciaEntidad();
    tipo.setNombre(tipoNombre);
    ReporteEntidad reporte = new ReporteEntidad();
    reporte.setZona(zona);
    reporte.setTipo(tipo);
    reporte.setFechaCreacion(fecha);
    return reporte;
  }
}
