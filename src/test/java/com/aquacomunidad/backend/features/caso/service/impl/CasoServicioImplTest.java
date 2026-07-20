package com.aquacomunidad.backend.features.caso.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.aquacomunidad.backend.common.enums.EstadoCaso;
import com.aquacomunidad.backend.common.enums.EstadoReporte;
import com.aquacomunidad.backend.common.enums.EstadoUsuario;
import com.aquacomunidad.backend.common.enums.RolUsuario;
import com.aquacomunidad.backend.exception.ExcepcionApi;
import com.aquacomunidad.backend.features.caso.dto.CasoActualizacionDto;
import com.aquacomunidad.backend.features.caso.dto.CasoRespuestaDto;
import com.aquacomunidad.backend.features.caso.dto.CasoSolicitudDto;
import com.aquacomunidad.backend.features.caso.entity.CasoEntidad;
import com.aquacomunidad.backend.features.caso.mapper.CasoMapeador;
import com.aquacomunidad.backend.features.caso.repository.CasoEvidenciaRepositorio;
import com.aquacomunidad.backend.features.caso.repository.CasoRepositorio;
import com.aquacomunidad.backend.features.historial.service.HistorialEstadoServicio;
import com.aquacomunidad.backend.features.reporte.entity.ReporteEntidad;
import com.aquacomunidad.backend.features.reporte.repository.ReporteRepositorio;
import com.aquacomunidad.backend.features.usuario.entity.UsuarioEntidad;
import com.aquacomunidad.backend.features.usuario.repository.UsuarioRepositorio;

@ExtendWith(MockitoExtension.class)
class CasoServicioImplTest {

  @Mock
  private CasoRepositorio casoRepositorio;
  @Mock
  private ReporteRepositorio reporteRepositorio;
  @Mock
  private UsuarioRepositorio usuarioRepositorio;
  @Mock
  private CasoEvidenciaRepositorio casoEvidenciaRepositorio;
  @Mock
  private CasoMapeador casoMapeador;
  @Mock
  private HistorialEstadoServicio historialEstadoServicio;

  @InjectMocks
  private CasoServicioImpl casoServicio;

  @Test
  void listarTodosComoAdminUsaOrdenDescendentePorFechaAsignacion() {
    setUsuarioEnContexto(usuario(1L, RolUsuario.ADMIN, EstadoUsuario.ACTIVO));
    CasoEntidad caso = new CasoEntidad();
    caso.setId(30L);
    CasoRespuestaDto respuesta = CasoRespuestaDto.builder().id(30L).build();
    when(casoRepositorio.findAllByOrderByFechaAsignacionDesc()).thenReturn(List.of(caso));
    when(casoMapeador.aRespuesta(caso)).thenReturn(respuesta);

    var casos = casoServicio.listarTodos();

    assertEquals(30L, casos.get(0).getId());
  }

  @Test
  void listarTodosComoOperadorUsaOrdenDescendentePorFechaAsignacionDelResponsable() {
    setUsuarioEnContexto(usuario(10L, RolUsuario.OPERADOR, EstadoUsuario.ACTIVO));
    CasoEntidad caso = new CasoEntidad();
    caso.setId(31L);
    CasoRespuestaDto respuesta = CasoRespuestaDto.builder().id(31L).build();
    when(casoRepositorio.findByResponsableIdOrderByFechaAsignacionDesc(10L)).thenReturn(List.of(caso));
    when(casoMapeador.aRespuesta(caso)).thenReturn(respuesta);

    var casos = casoServicio.listarTodos();

    assertEquals(31L, casos.get(0).getId());
  }

  @Test
  void crear_debeRechazarSiOperadorAsignaAOtro() {
    setUsuarioEnContexto(usuario(10L, RolUsuario.OPERADOR, EstadoUsuario.ACTIVO));
    CasoSolicitudDto request = new CasoSolicitudDto();
    request.setReporteId(1L);
    request.setResponsableId(11L);

    ExcepcionApi ex = assertThrows(ExcepcionApi.class, () -> casoServicio.crear(request));
    assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
  }

  @Test
  void crear_debeRechazarResponsableQueNoEsOperador() {
    setUsuarioEnContexto(usuario(1L, RolUsuario.ADMIN, EstadoUsuario.ACTIVO));
    CasoSolicitudDto request = new CasoSolicitudDto();
    request.setReporteId(20L);
    request.setResponsableId(21L);

    ReporteEntidad reporte = new ReporteEntidad();
    reporte.setEstado(EstadoReporte.PENDIENTE);
    when(reporteRepositorio.findById(20L)).thenReturn(Optional.of(reporte));
    when(casoRepositorio.existsByReporteOrigenId(20L)).thenReturn(false);
    when(usuarioRepositorio.findById(21L)).thenReturn(Optional.of(usuario(21L, RolUsuario.CIUDADANO, EstadoUsuario.ACTIVO)));

    ExcepcionApi ex = assertThrows(ExcepcionApi.class, () -> casoServicio.crear(request));
    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
  }

  @Test
  void actualizarEstado_debeRechazarCierreSinEvidencia() {
    setUsuarioEnContexto(usuario(1L, RolUsuario.ADMIN, EstadoUsuario.ACTIVO));

    var caso = mock(com.aquacomunidad.backend.features.caso.entity.CasoEntidad.class);
    var reporte = new ReporteEntidad();
    reporte.setEstado(EstadoReporte.EN_PROCESO);
    when(caso.getEstado()).thenReturn(EstadoCaso.EN_PROCESO);
    when(caso.getReporteOrigen()).thenReturn(reporte);
    when(caso.getEvidenciaCierre()).thenReturn(null);
    when(casoRepositorio.findById(50L)).thenReturn(Optional.of(caso));

    CasoActualizacionDto request = new CasoActualizacionDto();
    request.setEstado(EstadoCaso.RESUELTO);

    ExcepcionApi ex = assertThrows(ExcepcionApi.class, () -> casoServicio.actualizarEstado(50L, request));
    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
  }

  @Test
  void actualizarEstado_debeRechazarOperadorQueNoEsResponsable() {
    setUsuarioEnContexto(usuario(10L, RolUsuario.OPERADOR, EstadoUsuario.ACTIVO));

    var caso = mock(com.aquacomunidad.backend.features.caso.entity.CasoEntidad.class);
    var responsable = usuario(11L, RolUsuario.OPERADOR, EstadoUsuario.ACTIVO);
    when(caso.getResponsable()).thenReturn(responsable);
    when(casoRepositorio.findById(80L)).thenReturn(Optional.of(caso));

    CasoActualizacionDto request = new CasoActualizacionDto();
    request.setEstado(EstadoCaso.EN_PROCESO);

    ExcepcionApi ex = assertThrows(ExcepcionApi.class, () -> casoServicio.actualizarEstado(80L, request));
    assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
  }

  private void setUsuarioEnContexto(UsuarioEntidad usuario) {
    var auth = new UsernamePasswordAuthenticationToken(usuario, null, java.util.List.of());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private UsuarioEntidad usuario(Long id, RolUsuario rol, EstadoUsuario estado) {
    UsuarioEntidad usuario = new UsuarioEntidad();
    usuario.setId(id);
    usuario.setRol(rol);
    usuario.setEstado(estado);
    return usuario;
  }
}
