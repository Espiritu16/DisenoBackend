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
import com.aquacomunidad.backend.features.chatbot.support.ContextoChatbotAquaComunidad;
import com.aquacomunidad.backend.features.reporte.dto.ReporteResumenDto;
import com.aquacomunidad.backend.features.reporte.dto.ReporteResumenItemDto;
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
  void explicaCuandoUnReporteYaEstaTerminadoSinDarRespuestaGenerica() {
    ChatbotServicioImpl servicio = servicio("", Mockito.mock(ReporteServicio.class));

    var respuesta = servicio.responder(new ChatbotSolicitudDto("Como se que mi reporte ya esta terminado?", null));

    assertThat(respuesta.iaDisponible()).isFalse();
    assertThat(respuesta.proveedor()).isEqualTo("local");
    assertThat(respuesta.respuesta())
        .contains("Resuelto")
        .contains("Pendiente")
        .contains("En proceso");
  }

  @Test
  void explicaQueEsAquaComunidadDesdeElContextoDelSistema() {
    ChatbotServicioImpl servicio = servicio("", Mockito.mock(ReporteServicio.class));

    var respuesta = servicio.responder(new ChatbotSolicitudDto("Que es AquaComunidad?", null));

    assertThat(respuesta.iaDisponible()).isFalse();
    assertThat(respuesta.proveedor()).isEqualTo("local");
    assertThat(respuesta.respuesta())
        .contains("plataforma ciudadana")
        .contains("incidencias de agua")
        .contains("trazabilidad");
  }

  @Test
  void respondeCanalesDeContactoDesdeElContextoDelSistema() {
    ChatbotServicioImpl servicio = servicio("", Mockito.mock(ReporteServicio.class));

    var respuesta = servicio.responder(new ChatbotSolicitudDto("Cual es el telefono y WhatsApp de soporte?", null));

    assertThat(respuesta.iaDisponible()).isFalse();
    assertThat(respuesta.proveedor()).isEqualTo("local");
    assertThat(respuesta.respuesta())
        .contains("+51 999 000 111")
        .contains("+51 944 555 221")
        .contains("lunes a sabado")
        .contains("atencion 24/7");
  }

  @Test
  void noRespondeConsultasConceptualesDeIncidenciasComoFallbackGenerico() {
    ChatbotServicioImpl servicio = servicio("", Mockito.mock(ReporteServicio.class));

    var respuesta = servicio.responder(new ChatbotSolicitudDto("Que es una fuga?", null));

    assertThat(respuesta.iaDisponible()).isFalse();
    assertThat(respuesta.proveedor()).isEqualTo("local");
    assertThat(respuesta.respuesta())
        .contains("requiere el asistente con IA")
        .doesNotContain("Para hacer un reporte entra a Reportar");
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
  void respondeSaludosConMensajeCiudadanoSinRutasInternas() {
    ChatbotServicioImpl servicio = servicio("", Mockito.mock(ReporteServicio.class));

    var respuesta = servicio.responder(new ChatbotSolicitudDto("hola", null));

    assertThat(respuesta.iaDisponible()).isFalse();
    assertThat(respuesta.respuesta())
        .contains("Puedo ayudarte")
        .doesNotContain("/inicio")
        .doesNotContain("/reportar")
        .doesNotContain("/mis-reportes")
        .doesNotContain("?auth=");
  }

  @Test
  void instruccionesDeIaPidenNoMostrarRutasInternas() {
    String instrucciones = new ContextoChatbotAquaComunidad().instruccionesSistema();

    assertThat(instrucciones)
        .contains("No muestres rutas")
        .contains("nombres entendibles")
        .doesNotContain("/inicio")
        .doesNotContain("/reportar")
        .doesNotContain("/mis-reportes")
        .doesNotContain("?auth=")
        .doesNotContain("- Reportar: /reportar")
        .doesNotContain("- Registro: /inicio?auth=registro");
  }

  @Test
  void resumeReportesDelUsuarioAutenticadoSinExponerSqlAIa() {
    ReporteServicio reporteServicio = Mockito.mock(ReporteServicio.class);
    when(reporteServicio.obtenerResumenMisReportes(10L)).thenReturn(resumen(2, 1, 0, 1));
    when(reporteServicio.listarMisReportesRecientes(10L, 5)).thenReturn(List.of(
        resumenItem(13L, "Fuga", "Ate", EstadoReporte.PENDIENTE),
        resumenItem(14L, "Baja presion", "San Miguel", EstadoReporte.RESUELTO)));
    ChatbotServicioImpl servicio = servicio("sk-test", reporteServicio);
    autenticarCiudadano(10L);

    var respuesta = servicio.responder(new ChatbotSolicitudDto("Que reportes tengo?", null));

    assertThat(respuesta.iaDisponible()).isFalse();
    assertThat(respuesta.proveedor()).isEqualTo("local");
    assertThat(respuesta.respuesta()).contains("REP-13", "Pendiente", "REP-14", "Resuelto");
  }

  @Test
  void respondeCantidadDeReportesPorEstadoDesdeDatosDelUsuario() {
    ReporteServicio reporteServicio = Mockito.mock(ReporteServicio.class);
    when(reporteServicio.obtenerResumenMisReportes(10L)).thenReturn(resumen(4, 2, 1, 1));
    ChatbotServicioImpl servicio = servicio("sk-test", reporteServicio);
    autenticarCiudadano(10L);

    var respuesta = servicio.responder(new ChatbotSolicitudDto("Me puedes decir la cantidad de reportes pendientes?", null));

    assertThat(respuesta.iaDisponible()).isFalse();
    assertThat(respuesta.proveedor()).isEqualTo("local");
    assertThat(respuesta.respuesta()).contains("Tienes 4 reportes", "2 pendientes", "1 en proceso", "1 resuelto");
  }

  @Test
  void respondeSiUltimoReporteYaEstaAcabado() {
    ReporteServicio reporteServicio = Mockito.mock(ReporteServicio.class);
    when(reporteServicio.obtenerResumenMisReportes(10L)).thenReturn(resumen(2, 1, 0, 1));
    when(reporteServicio.obtenerUltimoReporte(10L)).thenReturn(
        resumenItem(21L, "Baja presion", "San Miguel", EstadoReporte.RESUELTO, LocalDateTime.parse("2026-05-31T09:00:00")));
    ChatbotServicioImpl servicio = servicio("sk-test", reporteServicio);
    autenticarCiudadano(10L);

    var respuesta = servicio.responder(new ChatbotSolicitudDto("Mi ultimo reporte ya esta acabado?", null));

    assertThat(respuesta.iaDisponible()).isFalse();
    assertThat(respuesta.proveedor()).isEqualTo("local");
    assertThat(respuesta.respuesta()).contains("REP-21", "Baja presion", "San Miguel", "ya esta resuelto");
    assertThat(respuesta.respuesta()).contains("terminado");
  }

  @Test
  void respondeEstadoDeReportePorCodigo() {
    ReporteServicio reporteServicio = Mockito.mock(ReporteServicio.class);
    when(reporteServicio.obtenerResumenMisReportes(10L)).thenReturn(resumen(2, 1, 0, 1));
    when(reporteServicio.obtenerMiReporte(21L, 10L)).thenReturn(
        reporte(21L, "Baja presion", "San Miguel", EstadoReporte.RESUELTO, LocalDateTime.parse("2026-05-31T09:00:00")));
    ChatbotServicioImpl servicio = servicio("sk-test", reporteServicio);
    autenticarCiudadano(10L);

    var respuesta = servicio.responder(new ChatbotSolicitudDto("Como va mi REP-21?", null));

    assertThat(respuesta.iaDisponible()).isFalse();
    assertThat(respuesta.proveedor()).isEqualTo("local");
    assertThat(respuesta.respuesta()).contains("REP-21", "Resuelto", "Baja presion", "San Miguel");
    assertThat(respuesta.respuesta()).contains("atendido y cerrado");
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
    return reporte(id, tipo, zona, estado, LocalDateTime.now());
  }

  private ReporteRespuestaDto reporte(Long id, String tipo, String zona, EstadoReporte estado, LocalDateTime fechaCreacion) {
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
        .fechaCreacion(fechaCreacion)
        .fechaActualizacion(fechaCreacion)
        .fotoUrls(List.of())
        .build();
  }

  private ReporteResumenDto resumen(long total, long pendientes, long enProceso, long resueltos) {
    return ReporteResumenDto.builder()
        .total(total)
        .pendientes(pendientes)
        .enProceso(enProceso)
        .resueltos(resueltos)
        .duplicados(0)
        .rechazados(0)
        .escalados(0)
        .build();
  }

  private ReporteResumenItemDto resumenItem(Long id, String tipo, String zona, EstadoReporte estado) {
    return resumenItem(id, tipo, zona, estado, LocalDateTime.now());
  }

  private ReporteResumenItemDto resumenItem(
      Long id,
      String tipo,
      String zona,
      EstadoReporte estado,
      LocalDateTime fechaCreacion) {
    return ReporteResumenItemDto.builder()
        .id(id)
        .codigo("REP-" + id)
        .tipo(tipo)
        .zona(zona)
        .estado(estado)
        .fechaCreacion(fechaCreacion)
        .fechaActualizacion(fechaCreacion)
        .build();
  }

  private ChatbotServicioImpl servicio(String apiKey, ReporteServicio reporteServicio) {
    return new ChatbotServicioImpl(
        apiKey,
        "gpt-5-mini",
        reporteServicio,
        Mockito.mock(ChatbotConversacionRepositorio.class),
        Mockito.mock(ChatbotMensajeRepositorio.class),
        new ContextoChatbotAquaComunidad());
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
        mensajeRepositorio,
        new ContextoChatbotAquaComunidad());
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
