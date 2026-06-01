package com.aquacomunidad.backend.features.reporte.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aquacomunidad.backend.common.enums.EstadoReporte;
import com.aquacomunidad.backend.exception.ExcepcionApi;
import com.aquacomunidad.backend.features.reporte.dto.ReporteRespuestaDto;
import com.aquacomunidad.backend.features.caso.repository.CasoRepositorio;
import com.aquacomunidad.backend.features.historial.service.HistorialEstadoServicio;
import com.aquacomunidad.backend.features.reporte.entity.CatalogoTipoIncidenciaEntidad;
import com.aquacomunidad.backend.features.reporte.entity.ReporteEntidad;
import com.aquacomunidad.backend.features.reporte.mapper.ReporteMapeador;
import com.aquacomunidad.backend.features.reporte.repository.CatalogoTipoIncidenciaRepositorio;
import com.aquacomunidad.backend.features.reporte.repository.ReporteConteoEstado;
import com.aquacomunidad.backend.features.reporte.repository.ReporteImagenRepositorio;
import com.aquacomunidad.backend.features.reporte.repository.ReporteRepositorio;
import com.aquacomunidad.backend.features.usuario.entity.UsuarioEntidad;
import com.aquacomunidad.backend.features.usuario.repository.UsuarioRepositorio;

@ExtendWith(MockitoExtension.class)
class ReporteServicioImplTest {

  @Mock
  private ReporteRepositorio reporteRepositorio;
  @Mock
  private UsuarioRepositorio usuarioRepositorio;
  @Mock
  private CasoRepositorio casoRepositorio;
  @Mock
  private CatalogoTipoIncidenciaRepositorio catalogoTipoIncidenciaRepositorio;
  @Mock
  private ReporteImagenRepositorio reporteImagenRepositorio;
  @Mock
  private ReporteMapeador reporteMapeador;
  @Mock
  private HistorialEstadoServicio historialEstadoServicio;

  @InjectMocks
  private ReporteServicioImpl reporteServicio;

  @Test
  void obtenerResumenMisReportesCuentaEstadosYDevuelveUltimoReporte() {
    when(reporteRepositorio.contarPorEstadoDeUsuario(10L)).thenReturn(List.of(
        conteo(EstadoReporte.PENDIENTE, 1),
        conteo(EstadoReporte.EN_PROCESO, 1),
        conteo(EstadoReporte.RESUELTO, 1),
        conteo(EstadoReporte.RECHAZADO, 1)));
    when(reporteRepositorio.findTopByUsuarioIdOrderByFechaCreacionDesc(10L))
        .thenReturn(Optional.of(reporte(3L, EstadoReporte.RESUELTO, LocalDateTime.parse("2026-05-31T08:00:00"))));

    var resumen = reporteServicio.obtenerResumenMisReportes(10L);

    assertThat(resumen.getTotal()).isEqualTo(4);
    assertThat(resumen.getPendientes()).isEqualTo(1);
    assertThat(resumen.getEnProceso()).isEqualTo(1);
    assertThat(resumen.getResueltos()).isEqualTo(1);
    assertThat(resumen.getRechazados()).isEqualTo(1);
    assertThat(resumen.getUltimoReporte().getId()).isEqualTo(3L);
    assertThat(resumen.getUltimoReporte().getCodigo()).isEqualTo("REP-3");
    assertThat(resumen.getUltimoReporte().getEstado()).isEqualTo(EstadoReporte.RESUELTO);
  }

  @Test
  void obtenerUltimoReporteDevuelveElMasRecienteDelUsuario() {
    when(reporteRepositorio.findTopByUsuarioIdOrderByFechaCreacionDesc(10L))
        .thenReturn(Optional.of(reporte(2L, EstadoReporte.EN_PROCESO, LocalDateTime.parse("2026-05-31T09:00:00"))));

    var ultimo = reporteServicio.obtenerUltimoReporte(10L);

    assertThat(ultimo.getId()).isEqualTo(2L);
    assertThat(ultimo.getCodigo()).isEqualTo("REP-2");
    assertThat(ultimo.getEstado()).isEqualTo(EstadoReporte.EN_PROCESO);
  }

  @Test
  void obtenerMiReporteRechazaReportesDeOtroUsuario() {
    when(reporteRepositorio.findByIdAndUsuarioId(7L, 10L)).thenReturn(Optional.empty());

    assertThrows(ExcepcionApi.class, () -> reporteServicio.obtenerMiReporte(7L, 10L));
  }

  @Test
  void obtenerMiReporteDevuelveDetallePropio() {
    ReporteEntidad reporte = reporte(8L, EstadoReporte.RESUELTO, LocalDateTime.parse("2026-05-31T09:00:00"));
    ReporteRespuestaDto respuesta = ReporteRespuestaDto.builder()
        .id(8L)
        .usuarioId(10L)
        .tipo("Fuga de agua")
        .zona("Cercado de Lima")
        .estado(EstadoReporte.RESUELTO)
        .fechaCreacion(reporte.getFechaCreacion())
        .fechaActualizacion(reporte.getFechaActualizacion())
        .build();
    when(reporteRepositorio.findByIdAndUsuarioId(8L, 10L)).thenReturn(Optional.of(reporte));
    when(reporteMapeador.aRespuesta(reporte)).thenReturn(respuesta);

    var detalle = reporteServicio.obtenerMiReporte(8L, 10L);

    assertThat(detalle.getId()).isEqualTo(8L);
    assertThat(detalle.getEstado()).isEqualTo(EstadoReporte.RESUELTO);
  }

  private ReporteEntidad reporte(Long id, EstadoReporte estado, LocalDateTime fechaCreacion) {
    UsuarioEntidad usuario = new UsuarioEntidad();
    usuario.setId(10L);

    CatalogoTipoIncidenciaEntidad tipo = new CatalogoTipoIncidenciaEntidad();
    tipo.setNombre("Fuga de agua");

    ReporteEntidad reporte = new ReporteEntidad();
    reporte.setId(id);
    reporte.setUsuario(usuario);
    reporte.setTipo(tipo);
    reporte.setZona("Cercado de Lima");
    reporte.setEstado(estado);
    reporte.setFechaCreacion(fechaCreacion);
    reporte.setFechaActualizacion(fechaCreacion);
    return reporte;
  }

  private ReporteConteoEstado conteo(EstadoReporte estado, long total) {
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
}
