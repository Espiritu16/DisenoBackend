package com.aquacomunidad.backend.features.chatbot.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.aquacomunidad.backend.common.enums.EstadoReporte;
import com.aquacomunidad.backend.common.enums.EstadoUsuario;
import com.aquacomunidad.backend.common.enums.RolUsuario;
import com.aquacomunidad.backend.features.chatbot.repository.ChatbotConversacionRepositorio;
import com.aquacomunidad.backend.features.chatbot.repository.ChatbotMensajeRepositorio;
import com.aquacomunidad.backend.features.chatbot.dto.ChatbotSolicitudDto;
import com.aquacomunidad.backend.features.chatbot.entity.ChatbotConversacionEntidad;
import com.aquacomunidad.backend.features.chatbot.entity.ChatbotMensajeEntidad;
import com.aquacomunidad.backend.features.chatbot.entity.RolMensajeChatbot;
import com.aquacomunidad.backend.features.reporte.dto.ReporteRespuestaDto;
import com.aquacomunidad.backend.features.reporte.service.ReporteServicio;
import com.aquacomunidad.backend.features.usuario.entity.UsuarioEntidad;

class ChatbotServicioImplTest {

  @AfterEach
  void limpiarContexto() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void respondeConFallbackCuandoNoHayApiKey() {
    ChatbotServicioImpl servicio = servicio("", Mockito.mock(ReporteServicio.class));

    var respuesta = servicio.responder(new ChatbotSolicitudDto("Como puedo hacer un reporte de fuga?", null));

    assertThat(respuesta.iaDisponible()).isFalse();
    assertThat(respuesta.proveedor()).isEqualTo("local");
    assertThat(respuesta.respuesta()).contains("Reportar");
  }

  @Test
  void bloqueaPreguntasFueraDeAquaComunidadSinUsarIa() {
    ChatbotServicioImpl servicio = servicio("sk-test", Mockito.mock(ReporteServicio.class));

    var respuesta = servicio.responder(new ChatbotSolicitudDto("Cuanto es 3+3?", null));

    assertThat(respuesta.iaDisponible()).isFalse();
    assertThat(respuesta.proveedor()).isEqualTo("local");
    assertThat(respuesta.respuesta()).contains("AquaComunidad");
  }

  @Test
  void resumeReportesDelUsuarioAutenticadoSinExponerSqlAIa() {
    ReporteServicio reporteServicio = Mockito.mock(ReporteServicio.class);
    when(reporteServicio.listarMisReportes(10L)).thenReturn(List.of(
        reporte(13L, "Fuga", "Ate", EstadoReporte.PENDIENTE),
        reporte(14L, "Baja presion", "San Miguel", EstadoReporte.RESUELTO)));
    ChatbotServicioImpl servicio = servicio("sk-test", reporteServicio);
    autenticarCiudadano(10L);

    var respuesta = servicio.responder(new ChatbotSolicitudDto("Que reportes tengo?", null));

    assertThat(respuesta.iaDisponible()).isFalse();
    assertThat(respuesta.proveedor()).isEqualTo("local");
    assertThat(respuesta.respuesta()).contains("REP-13", "Pendiente", "REP-14", "Resuelto");
  }

  @Test
  void persisteMensajeUsuarioYRespuestaCuandoHaySesion() {
    ChatbotConversacionRepositorio conversacionRepositorio = Mockito.mock(ChatbotConversacionRepositorio.class);
    ChatbotMensajeRepositorio mensajeRepositorio = Mockito.mock(ChatbotMensajeRepositorio.class);
    ChatbotConversacionEntidad conversacion = new ChatbotConversacionEntidad();
    conversacion.setId(20L);
    conversacion.setFechaConversacion(java.time.LocalDate.now());
    when(conversacionRepositorio.findByUsuarioIdAndFechaConversacion(any(), any())).thenReturn(Optional.of(conversacion));
    when(mensajeRepositorio.findTop8ByConversacionOrderByCreadoEnDesc(conversacion)).thenReturn(List.of());
    ChatbotServicioImpl servicio = servicio("",
        Mockito.mock(ReporteServicio.class),
        conversacionRepositorio,
        mensajeRepositorio);
    autenticarCiudadano(10L);

    var respuesta = servicio.responder(new ChatbotSolicitudDto("Como puedo hacer un reporte?", null));

    ArgumentCaptor<ChatbotMensajeEntidad> captor = ArgumentCaptor.forClass(ChatbotMensajeEntidad.class);
    Mockito.verify(mensajeRepositorio, Mockito.times(2)).save(captor.capture());
    assertThat(respuesta.persistido()).isTrue();
    assertThat(respuesta.conversacionId()).isEqualTo(20L);
    assertThat(captor.getAllValues())
        .extracting(ChatbotMensajeEntidad::getRol)
        .containsExactly(RolMensajeChatbot.USUARIO, RolMensajeChatbot.ASISTENTE);
  }

  @Test
  void muestraAccionDeRegistroCuandoElUsuarioQuiereCrearCuenta() {
    ChatbotServicioImpl servicio = servicio("", Mockito.mock(ReporteServicio.class));

    var respuesta = servicio.responder(new ChatbotSolicitudDto("Quiero registrarme", null));

    assertThat(respuesta.acciones()).hasSize(1);
    assertThat(respuesta.acciones().get(0).etiqueta()).isEqualTo("Registrarse");
    assertThat(respuesta.acciones().get(0).ruta()).isEqualTo("/inicio?auth=registro");
  }

  @Test
  void muestraAccionDeMisReportesCuandoElUsuarioConsultaSeguimiento() {
    ChatbotServicioImpl servicio = servicio("", Mockito.mock(ReporteServicio.class));

    var respuesta = servicio.responder(new ChatbotSolicitudDto("Quiero ver el seguimiento de mis reportes", null));

    assertThat(respuesta.acciones()).hasSize(1);
    assertThat(respuesta.acciones().get(0).etiqueta()).isEqualTo("Ver Mis Reportes");
    assertThat(respuesta.acciones().get(0).ruta()).isEqualTo("/mis-reportes");
  }

  private ReporteRespuestaDto reporte(Long id, String tipo, String zona, EstadoReporte estado) {
    return ReporteRespuestaDto.builder()
        .id(id)
        .usuarioId(10L)
        .tipo(tipo)
        .zona(zona)
        .estado(estado)
        .direccion("Direccion " + zona)
        .descripcion("Descripcion")
        .lat(BigDecimal.ZERO)
        .lng(BigDecimal.ZERO)
        .posibleDuplicado(false)
        .fechaCreacion(LocalDateTime.now())
        .fechaActualizacion(LocalDateTime.now())
        .fotoUrls(List.of())
        .build();
  }

  private ChatbotServicioImpl servicio(String apiKey, ReporteServicio reporteServicio) {
    return new ChatbotServicioImpl(
        apiKey,
        "gpt-5-mini",
        reporteServicio,
        Mockito.mock(ChatbotConversacionRepositorio.class),
        Mockito.mock(ChatbotMensajeRepositorio.class));
  }

  private ChatbotServicioImpl servicio(
      String apiKey,
      ReporteServicio reporteServicio,
      ChatbotConversacionRepositorio conversacionRepositorio,
      ChatbotMensajeRepositorio mensajeRepositorio) {
    return new ChatbotServicioImpl(
        apiKey,
        "gpt-5-mini",
        reporteServicio,
        conversacionRepositorio,
        mensajeRepositorio);
  }

  private void autenticarCiudadano(Long id) {
    UsuarioEntidad usuario = new UsuarioEntidad();
    usuario.setId(id);
    usuario.setNombre("Ciudadano Prueba");
    usuario.setCorreo("ciudadano@test.local");
    usuario.setRol(RolUsuario.CIUDADANO);
    usuario.setEstado(EstadoUsuario.ACTIVO);
    var auth = new UsernamePasswordAuthenticationToken(usuario, null, List.of());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }
}
