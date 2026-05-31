package com.aquacomunidad.backend.features.reporte.service.impl;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquacomunidad.backend.common.enums.EstadoReporte;
import com.aquacomunidad.backend.exception.ExcepcionApi;
import com.aquacomunidad.backend.features.caso.entity.CasoEntidad;
import com.aquacomunidad.backend.features.caso.repository.CasoRepositorio;
import com.aquacomunidad.backend.features.historial.dto.DetalleTrazabilidadReporteDto;
import com.aquacomunidad.backend.features.historial.service.HistorialEstadoServicio;
import com.aquacomunidad.backend.features.reporte.entity.CatalogoTipoIncidenciaEntidad;
import com.aquacomunidad.backend.features.reporte.dto.ReporteResumenDto;
import com.aquacomunidad.backend.features.reporte.dto.ReporteResumenItemDto;
import com.aquacomunidad.backend.features.reporte.dto.ReporteSolicitudDto;
import com.aquacomunidad.backend.features.reporte.dto.ReporteRespuestaDto;
import com.aquacomunidad.backend.features.reporte.entity.ReporteEntidad;
import com.aquacomunidad.backend.features.reporte.entity.ReporteImagenEntidad;
import com.aquacomunidad.backend.features.reporte.mapper.ReporteMapeador;
import com.aquacomunidad.backend.features.reporte.repository.CatalogoTipoIncidenciaRepositorio;
import com.aquacomunidad.backend.features.reporte.repository.ReporteImagenRepositorio;
import com.aquacomunidad.backend.features.reporte.repository.ReporteRepositorio;
import com.aquacomunidad.backend.features.reporte.service.ReporteServicio;
import com.aquacomunidad.backend.features.usuario.entity.UsuarioEntidad;
import com.aquacomunidad.backend.features.usuario.repository.UsuarioRepositorio;
import com.aquacomunidad.backend.security.SeguridadContextoUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReporteServicioImpl implements ReporteServicio {

  private final ReporteRepositorio reporteRepositorio;
  private final UsuarioRepositorio usuarioRepositorio;
  private final CasoRepositorio casoRepositorio;
  private final CatalogoTipoIncidenciaRepositorio catalogoTipoIncidenciaRepositorio;
  private final ReporteImagenRepositorio reporteImagenRepositorio;
  private final ReporteMapeador reporteMapeador;
  private final HistorialEstadoServicio historialEstadoServicio;

  @Override
  @Transactional
  public ReporteRespuestaDto crear(ReporteSolicitudDto request) {
    Long usuarioIdAutenticado = SeguridadContextoUtil.idUsuarioAutenticado();
    UsuarioEntidad usuario = usuarioRepositorio.findById(usuarioIdAutenticado)
        .orElseThrow(() -> new ExcepcionApi(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

    CatalogoTipoIncidenciaEntidad tipo = resolverTipo(request.getTipo());
    ReporteEntidad entidad = reporteMapeador.toEntidad(request, usuario, tipo);
    boolean posibleDuplicado = reporteRepositorio.existePosibleDuplicado(
        tipo.getNombre(),
        request.getZona(),
        LocalDateTime.now().minusHours(24));
    entidad.setPosibleDuplicado(posibleDuplicado);

    ReporteEntidad saved = reporteRepositorio.save(entidad);
    guardarImagenes(saved, request);

    historialEstadoServicio.registrarCambioReporte(
        saved,
        null,
        saved.getEstado(),
        "Creacion de reporte",
        usuario);

    return reporteMapeador.aRespuesta(saved);
  }

  private void guardarImagenes(ReporteEntidad reporte, ReporteSolicitudDto request) {
    List<String> urls = request.getFotoUrls() == null || request.getFotoUrls().isEmpty()
        ? List.of(request.getFotoUrl())
        : request.getFotoUrls();
    for (int i = 0; i < urls.size(); i++) {
      String url = urls.get(i);
      if (url == null || url.isBlank()) {
        continue;
      }
      ReporteImagenEntidad imagen = new ReporteImagenEntidad();
      imagen.setReporte(reporte);
      imagen.setUrl(url.trim());
      imagen.setOrden(i);
      reporteImagenRepositorio.save(imagen);
      reporte.getImagenes().add(imagen);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReporteRespuestaDto> listarTodos() {
    return reporteRepositorio.findAll().stream().map(reporteMapeador::aRespuesta).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReporteRespuestaDto> listarPorUsuario(Long usuarioId) {
    return reporteRepositorio.findByUsuarioId(usuarioId).stream().map(reporteMapeador::aRespuesta).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReporteRespuestaDto> listarPorEstado(EstadoReporte estado) {
    return reporteRepositorio.findByEstado(estado).stream().map(reporteMapeador::aRespuesta).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReporteRespuestaDto> listarConFiltros(
      Long usuarioId,
      EstadoReporte estado,
      String tipo,
      String zona,
      LocalDateTime fechaDesde,
      LocalDateTime fechaHasta) {
    String tipoNormalizado = normalizarFiltro(tipo);
    String zonaNormalizada = normalizarFiltro(zona);
    return reporteRepositorio.buscarConFiltros(usuarioId, estado, tipoNormalizado, zonaNormalizada, fechaDesde, fechaHasta)
        .stream()
        .map(reporteMapeador::aRespuesta)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReporteRespuestaDto> listarMisReportes(Long usuarioId) {
    return reporteRepositorio.findByUsuarioId(usuarioId).stream().map(reporteMapeador::aRespuesta).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public ReporteResumenDto obtenerResumenMisReportes(Long usuarioId) {
    List<ReporteEntidad> reportes = reporteRepositorio.findByUsuarioId(usuarioId);
    ReporteEntidad ultimo = ultimoReporte(reportes);

    return ReporteResumenDto.builder()
        .total(reportes.size())
        .pendientes(contarPorEstado(reportes, EstadoReporte.PENDIENTE))
        .enProceso(contarPorEstado(reportes, EstadoReporte.EN_PROCESO))
        .resueltos(contarPorEstado(reportes, EstadoReporte.RESUELTO))
        .duplicados(contarPorEstado(reportes, EstadoReporte.DUPLICADO))
        .rechazados(contarPorEstado(reportes, EstadoReporte.RECHAZADO))
        .escalados(contarPorEstado(reportes, EstadoReporte.ESCALADO))
        .ultimoReporte(ultimo == null ? null : aResumenItem(ultimo))
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public ReporteResumenItemDto obtenerUltimoReporte(Long usuarioId) {
    return reporteRepositorio.findByUsuarioId(usuarioId).stream()
        .max((a, b) -> a.getFechaCreacion().compareTo(b.getFechaCreacion()))
        .map(this::aResumenItem)
        .orElse(null);
  }

  @Override
  @Transactional(readOnly = true)
  public ReporteRespuestaDto obtenerMiReporte(Long reporteId, Long usuarioId) {
    ReporteEntidad reporte = reporteRepositorio.findById(reporteId)
        .orElseThrow(() -> new ExcepcionApi(HttpStatus.NOT_FOUND, "Reporte no encontrado"));
    if (!reporte.getUsuario().getId().equals(usuarioId)) {
      throw new ExcepcionApi(HttpStatus.FORBIDDEN, "No puede ver reportes de otro usuario");
    }
    return reporteMapeador.aRespuesta(reporte);
  }

  @Override
  @Transactional(readOnly = true)
  public DetalleTrazabilidadReporteDto obtenerDetalleTrazabilidad(Long reporteId, Long usuarioId, boolean esCiudadano) {
    ReporteEntidad reporte = reporteRepositorio.findById(reporteId)
        .orElseThrow(() -> new ExcepcionApi(HttpStatus.NOT_FOUND, "Reporte no encontrado"));

    if (esCiudadano && !reporte.getUsuario().getId().equals(usuarioId)) {
      throw new ExcepcionApi(HttpStatus.FORBIDDEN, "No puede ver trazabilidad de reportes de otro usuario");
    }

    CasoEntidad caso = casoRepositorio.findByReporteOrigenId(reporteId).orElse(null);
    Long casoId = caso == null ? null : caso.getId();
    return historialEstadoServicio.obtenerDetalle(reporteId, casoId);
  }

  private CatalogoTipoIncidenciaEntidad resolverTipo(String tipoSolicitado) {
    String tipo = tipoSolicitado == null ? "" : tipoSolicitado.trim();
    return catalogoTipoIncidenciaRepositorio.findByNombreIgnoreCase(tipo)
        .or(() -> catalogoTipoIncidenciaRepositorio.findByCodigoIgnoreCase(tipo))
        .or(() -> catalogoTipoIncidenciaRepositorio.findByCodigoIgnoreCase(codigoPorNombreVisible(tipo)))
        .or(() -> catalogoTipoIncidenciaRepositorio.findByCodigoIgnoreCase("OTRO"))
        .orElseThrow(() -> new ExcepcionApi(HttpStatus.BAD_REQUEST, "Tipo de incidencia no configurado"));
  }

  private String codigoPorNombreVisible(String tipo) {
    String normalizado = tipo.toLowerCase();
    if (normalizado.contains("fuga")) {
      return "FUGA_AGUA";
    }
    if (normalizado.contains("presion") || normalizado.contains("presión")) {
      return "BAJA_PRESION";
    }
    if (normalizado.contains("corte")) {
      return "CORTE_SERVICIO";
    }
    if (normalizado.contains("turbia") || normalizado.contains("calidad") || normalizado.contains("agua")) {
      return "AGUA_TURBIA";
    }
    return "OTRO";
  }

  private String normalizarFiltro(String valor) {
    if (valor == null) {
      return null;
    }
    String limpio = valor.trim();
    return limpio.isEmpty() ? null : limpio;
  }

  private long contarPorEstado(List<ReporteEntidad> reportes, EstadoReporte estado) {
    return reportes.stream()
        .filter(reporte -> reporte.getEstado() == estado)
        .count();
  }

  private ReporteEntidad ultimoReporte(List<ReporteEntidad> reportes) {
    return reportes.stream()
        .max((a, b) -> a.getFechaCreacion().compareTo(b.getFechaCreacion()))
        .orElse(null);
  }

  private ReporteResumenItemDto aResumenItem(ReporteEntidad reporte) {
    return ReporteResumenItemDto.builder()
        .id(reporte.getId())
        .codigo("REP-" + reporte.getId())
        .tipo(reporte.getTipo().getNombre())
        .zona(reporte.getZona())
        .estado(reporte.getEstado())
        .fechaCreacion(reporte.getFechaCreacion())
        .fechaActualizacion(reporte.getFechaActualizacion())
        .build();
  }
}
