package com.aquacomunidad.backend.features.tablero.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aquacomunidad.backend.common.enums.EstadoCaso;
import com.aquacomunidad.backend.common.enums.EstadoReporte;
import com.aquacomunidad.backend.features.caso.entity.CasoEntidad;
import com.aquacomunidad.backend.features.caso.repository.CasoRepositorio;
import com.aquacomunidad.backend.features.iot.dto.NivelAguaDto;
import com.aquacomunidad.backend.features.iot.service.IotServicio;
import com.aquacomunidad.backend.features.reporte.entity.CatalogoTipoIncidenciaEntidad;
import com.aquacomunidad.backend.features.reporte.entity.ReporteEntidad;
import com.aquacomunidad.backend.features.reporte.repository.ReporteRepositorio;
import com.aquacomunidad.backend.features.usuario.entity.UsuarioEntidad;

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
    when(reporteRepositorio.findAll()).thenReturn(reportesDemo());
    when(casoRepositorio.findAll()).thenReturn(List.of(
        casoResuelto("Sector 4", "2026-09-02T08:00:00", 10),
        casoResuelto("Sector 4", "2026-09-03T08:00:00", 20),
        casoResuelto("Centro", "2026-09-04T08:00:00", 5),
        casoEnProceso("Ate", "2026-09-05T08:00:00")));
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
    assertThat(kpis.getReportesPendientes()).isEqualTo(2);
    assertThat(kpis.getReportesEnProceso()).isEqualTo(1);
    assertThat(kpis.getReportesResueltos()).isEqualTo(3);
    assertThat(kpis.getTotalCiudadanosReportantes()).isEqualTo(3);
    assertThat(kpis.getCasosAbiertos()).isEqualTo(1);
    assertThat(kpis.getPromedioHorasResolucion()).isEqualTo(11.67);
    assertThat(kpis.getReportesPorMes()).extracting("mes").contains("Jul", "Ago", "Sep");
    assertThat(kpis.getUsuariosReportantesPorMes()).extracting("mes").contains("Jul", "Ago", "Sep");
    assertThat(kpis.getUsuariosReportantesPorMes()).extracting("cantidad").contains(1L, 2L);
    assertThat(kpis.getReportesPorCategoria()).first().extracting("categoria").isEqualTo("Fuga de agua");
    assertThat(kpis.getReportesPorEstado()).extracting("estado").contains("Pendientes", "Resueltos");
    assertThat(kpis.getProyeccionMensual()).hasSize(3);
    assertThat(kpis.getProyeccionMensual()).extracting("mes").containsExactly("Oct", "Nov", "Dic");
    assertThat(kpis.getRecomendacionAutomatica()).contains("Priorizar cuadrillas");
    assertThat(kpis.getTiemposPorZona()).extracting("zona").contains("Sector 4", "Centro");
    assertThat(kpis.getZonasCriticas()).first().extracting("zona").isEqualTo("Centro");
    assertThat(kpis.getNivelesAgua()).hasSize(1);
  }

  @Test
  void getKpisFiltraPorRangoDeFechas() {
    when(reporteRepositorio.findAll()).thenReturn(reportesDemo());
    when(casoRepositorio.findAll()).thenReturn(List.of(
        casoResuelto("Sector 4", "2026-09-02T08:00:00", 10),
        casoResuelto("Centro", "2026-08-03T08:00:00", 6)));
    when(iotServicio.listarNiveles()).thenReturn(List.of());

    var servicio = new TableroServicioImpl(reporteRepositorio, casoRepositorio, iotServicio);

    var kpis = servicio.getKpis(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-09-30"));

    assertThat(kpis.getTotalReportes()).isEqualTo(3);
    assertThat(kpis.getTotalCiudadanosReportantes()).isEqualTo(2);
    assertThat(kpis.getReportesResueltos()).isEqualTo(3);
    assertThat(kpis.getReportesPorMes()).extracting("mes").containsExactly("Sep");
    assertThat(kpis.getUsuariosReportantesPorMes()).extracting("mes", "cantidad").containsExactly(
        org.assertj.core.groups.Tuple.tuple("Sep", 2L));
    assertThat(kpis.getReportesPorCategoria()).extracting("categoria").contains("Fuga de agua", "Agua turbia");
    assertThat(kpis.getTiemposPorZona()).extracting("zona").containsExactly("Sector 4");
    assertThat(kpis.getActividadSemanal()).extracting("dia").contains("Hoy");
  }

  @Test
  void getKpisProyectaTresMesesConCrecimientoPreventivoDesdeUltimosTresMeses() {
    when(reporteRepositorio.findAll()).thenReturn(reportesConVariacionMensual());
    when(casoRepositorio.findAll()).thenReturn(List.of());
    when(iotServicio.listarNiveles()).thenReturn(List.of());

    var servicio = new TableroServicioImpl(reporteRepositorio, casoRepositorio, iotServicio);

    var kpis = servicio.getKpis();

    assertThat(kpis.getReportesPorMes()).extracting("mes").containsExactly("Jun", "Jul", "Ago", "Sep");
    assertThat(kpis.getProyeccionMensual()).extracting("mes").containsExactly("Oct", "Nov", "Dic");
    assertThat(kpis.getProyeccionMensual()).extracting("estimado").containsExactly(34L, 38L, 42L);
  }

  private List<ReporteEntidad> reportesDemo() {
    return List.of(
        reporte(1L, "Sector 4", "Fuga de agua", EstadoReporte.PENDIENTE, LocalDateTime.parse("2026-07-02T08:00:00")),
        reporte(1L, "Sector 4", "Fuga de agua", EstadoReporte.EN_PROCESO, LocalDateTime.parse("2026-08-02T08:00:00")),
        reporte(2L, "Sector 4", "Baja presión", EstadoReporte.PENDIENTE, LocalDateTime.parse("2026-08-03T08:00:00")),
        reporte(2L, "Centro", "Agua turbia", EstadoReporte.RESUELTO, LocalDateTime.parse("2026-09-02T08:00:00")),
        reporte(3L, "Centro", "Fuga de agua", EstadoReporte.RESUELTO, LocalDateTime.parse("2026-09-03T08:00:00")),
        reporte(3L, "Centro", "Fuga de agua", EstadoReporte.RESUELTO, LocalDateTime.parse("2026-09-04T08:00:00")));
  }

  private List<ReporteEntidad> reportesConVariacionMensual() {
    return java.util.stream.Stream.of(
        reportesDelMes(1L, "Ate", "Agua turbia", EstadoReporte.RESUELTO, "2026-06", 2),
        reportesDelMes(20L, "Ate", "Agua turbia", EstadoReporte.RESUELTO, "2026-07", 59),
        reportesDelMes(90L, "Callao", "Fuga de agua", EstadoReporte.EN_PROCESO, "2026-08", 31),
        reportesDelMes(140L, "Comas", "Baja presión", EstadoReporte.PENDIENTE, "2026-09", 30))
        .flatMap(List::stream)
        .toList();
  }

  private List<ReporteEntidad> reportesDelMes(
      long usuarioInicial,
      String zona,
      String tipoNombre,
      EstadoReporte estado,
      String mes,
      int cantidad) {
    return java.util.stream.IntStream.range(0, cantidad)
        .mapToObj(index -> reporte(
            usuarioInicial + index,
            zona,
            tipoNombre,
            estado,
            LocalDateTime.parse(mes + "-" + String.format("%02d", (index % 28) + 1) + "T08:00:00")))
        .toList();
  }

  private CasoEntidad casoResuelto(String zona, String fechaReporte, int horas) {
    ReporteEntidad reporte = new ReporteEntidad();
    reporte.setZona(zona);
    reporte.setFechaCreacion(LocalDateTime.parse(fechaReporte));
    CasoEntidad caso = new CasoEntidad();
    caso.setReporteOrigen(reporte);
    caso.setEstado(EstadoCaso.RESUELTO);
    caso.setFechaAsignacion(LocalDateTime.parse("2026-06-20T08:00:00"));
    caso.setFechaCierre(caso.getFechaAsignacion().plusHours(horas));
    return caso;
  }

  private CasoEntidad casoEnProceso(String zona, String fechaReporte) {
    ReporteEntidad reporte = new ReporteEntidad();
    reporte.setZona(zona);
    reporte.setFechaCreacion(LocalDateTime.parse(fechaReporte));
    CasoEntidad caso = new CasoEntidad();
    caso.setReporteOrigen(reporte);
    caso.setEstado(EstadoCaso.EN_PROCESO);
    return caso;
  }

  private ReporteEntidad reporte(Long usuarioId, String zona, String tipoNombre, EstadoReporte estado, LocalDateTime fecha) {
    CatalogoTipoIncidenciaEntidad tipo = new CatalogoTipoIncidenciaEntidad();
    tipo.setNombre(tipoNombre);
    UsuarioEntidad usuario = new UsuarioEntidad();
    usuario.setId(usuarioId);
    ReporteEntidad reporte = new ReporteEntidad();
    reporte.setUsuario(usuario);
    reporte.setZona(zona);
    reporte.setTipo(tipo);
    reporte.setEstado(estado);
    reporte.setFechaCreacion(fecha);
    return reporte;
  }
}
