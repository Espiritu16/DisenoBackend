package com.aquacomunidad.backend.features.chatbot.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.aquacomunidad.backend.exception.ExcepcionApi;
import com.aquacomunidad.backend.features.chatbot.dto.ChatbotAccionDto;
import com.aquacomunidad.backend.features.chatbot.dto.ChatbotConversacionResumenDto;
import com.aquacomunidad.backend.features.chatbot.dto.ChatbotHistorialDiaDto;
import com.aquacomunidad.backend.features.chatbot.dto.ChatbotMensajeDto;
import com.aquacomunidad.backend.features.chatbot.dto.ChatbotMensajeHistorialDto;
import com.aquacomunidad.backend.features.chatbot.dto.ChatbotRespuestaDto;
import com.aquacomunidad.backend.features.chatbot.dto.ChatbotSolicitudDto;
import com.aquacomunidad.backend.features.chatbot.entity.ChatbotConversacionEntidad;
import com.aquacomunidad.backend.features.chatbot.entity.ChatbotMensajeEntidad;
import com.aquacomunidad.backend.features.chatbot.entity.RolMensajeChatbot;
import com.aquacomunidad.backend.features.chatbot.repository.ChatbotConversacionRepositorio;
import com.aquacomunidad.backend.features.chatbot.repository.ChatbotMensajeRepositorio;
import com.aquacomunidad.backend.features.chatbot.service.ChatbotServicio;
import com.aquacomunidad.backend.features.chatbot.support.ContextoChatbotAquaComunidad;
import com.aquacomunidad.backend.features.reporte.dto.ReporteResumenDto;
import com.aquacomunidad.backend.features.reporte.dto.ReporteResumenItemDto;
import com.aquacomunidad.backend.features.reporte.dto.ReporteRespuestaDto;
import com.aquacomunidad.backend.features.reporte.service.ReporteServicio;
import com.aquacomunidad.backend.features.usuario.entity.UsuarioEntidad;
import com.aquacomunidad.backend.security.SeguridadContextoUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class ChatbotServicioImpl implements ChatbotServicio {

  private static final String OPENAI_RESPONSES_URL = "https://api.openai.com/v1/responses";
  private static final int MAX_HISTORIAL_ENVIADO = 8;
  private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Lima");

  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;
  private final String apiKey;
  private final String modelo;
  private final ReporteServicio reporteServicio;
  private final ChatbotConversacionRepositorio conversacionRepositorio;
  private final ChatbotMensajeRepositorio mensajeRepositorio;
  private final ContextoChatbotAquaComunidad contextoChatbot;

  public ChatbotServicioImpl(
      @Value("${app.openai.api-key:}") String apiKey,
      @Value("${app.openai.model:gpt-5-mini}") String modelo,
      ReporteServicio reporteServicio,
      ChatbotConversacionRepositorio conversacionRepositorio,
      ChatbotMensajeRepositorio mensajeRepositorio,
      ContextoChatbotAquaComunidad contextoChatbot) {
    this.objectMapper = new ObjectMapper();
    this.apiKey = apiKey;
    this.modelo = modelo;
    this.reporteServicio = reporteServicio;
    this.conversacionRepositorio = conversacionRepositorio;
    this.mensajeRepositorio = mensajeRepositorio;
    this.contextoChatbot = contextoChatbot;
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(8))
        .build();
  }

  @Override
  @Transactional
  public ChatbotRespuestaDto responder(ChatbotSolicitudDto solicitud) {
    String mensaje = solicitud.mensaje().trim();
    UsuarioEntidad usuario = usuarioAutenticadoSeguro();
    ChatbotConversacionEntidad conversacion = usuario == null
        ? null
        : obtenerOCrearConversacion(usuario, resolverFechaConversacion(solicitud.fechaConversacion()));
    List<ChatbotMensajeDto> historial = conversacion == null
        ? solicitud.historial()
        : historialReciente(conversacion);
    ChatbotSolicitudDto solicitudConHistorial = new ChatbotSolicitudDto(mensaje, historial,
        conversacion == null ? solicitud.fechaConversacion() : conversacion.getFechaConversacion());

    if (conversacion != null) {
      guardarMensaje(conversacion, RolMensajeChatbot.USUARIO, mensaje, null, null, false);
    }

    ChatbotRespuestaDto respuesta = generarRespuesta(solicitudConHistorial);
    if (conversacion == null) {
      return respuesta;
    }

    guardarMensaje(
        conversacion,
        RolMensajeChatbot.ASISTENTE,
        respuesta.respuesta(),
        respuesta.proveedor(),
        respuesta.modelo(),
        respuesta.iaDisponible());
    conversacion.setActualizadoEn(LocalDateTime.now());
    conversacionRepositorio.save(conversacion);
    return new ChatbotRespuestaDto(
        respuesta.respuesta(),
        respuesta.proveedor(),
        respuesta.modelo(),
        respuesta.iaDisponible(),
        conversacion.getId(),
        conversacion.getFechaConversacion(),
        true,
        respuesta.acciones());
  }

  @Override
  @Transactional(readOnly = true)
  public List<ChatbotConversacionResumenDto> listarConversaciones() {
    UsuarioEntidad usuario = SeguridadContextoUtil.usuarioAutenticado();
    return conversacionRepositorio.findTop30ByUsuarioIdOrderByFechaConversacionDesc(usuario.getId())
        .stream()
        .map(conversacion -> ChatbotConversacionResumenDto.builder()
            .id(conversacion.getId())
            .fechaConversacion(conversacion.getFechaConversacion())
            .titulo(conversacion.getTitulo())
            .totalMensajes(mensajeRepositorio.countByConversacion(conversacion))
            .actualizadoEn(conversacion.getActualizadoEn())
            .build())
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public ChatbotHistorialDiaDto obtenerHistorial(LocalDate fechaConversacion) {
    UsuarioEntidad usuario = SeguridadContextoUtil.usuarioAutenticado();
    LocalDate fecha = resolverFechaConversacion(fechaConversacion);
    ChatbotConversacionEntidad conversacion = conversacionRepositorio
        .findByUsuarioIdAndFechaConversacion(usuario.getId(), fecha)
        .orElse(null);
    if (conversacion == null) {
      return ChatbotHistorialDiaDto.builder()
          .fechaConversacion(fecha)
          .mensajes(List.of())
          .build();
    }

    return ChatbotHistorialDiaDto.builder()
        .conversacionId(conversacion.getId())
        .fechaConversacion(conversacion.getFechaConversacion())
        .mensajes(mensajeRepositorio.findByConversacionOrderByCreadoEnAsc(conversacion)
            .stream()
            .map(this::aMensajeHistorial)
            .toList())
        .build();
  }

  private ChatbotRespuestaDto generarRespuesta(ChatbotSolicitudDto solicitud) {
    String mensaje = solicitud.mensaje().trim();
    if (esPreguntaFueraDeAlcance(mensaje)) {
      return respuestaFueraDeAlcance();
    }
    if (esConsultaMisReportes(mensaje)) {
      return responderMisReportes(mensaje);
    }
    if (contextoChatbot.esConsultaConceptualParaIa(mensaje) && !StringUtils.hasText(apiKey)) {
      return respuestaIaNoDisponibleParaConsultaConceptual();
    }
    if (!StringUtils.hasText(apiKey)) {
      return respuestaLocal(mensaje);
    }

    try {
      String respuesta = consultarOpenAi(solicitud);
      return new ChatbotRespuestaDto(
          respuesta,
          "openai",
          modelo,
          true,
          null,
          null,
          false,
          accionesParaRespuesta(mensaje, respuesta));
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new ExcepcionApi(HttpStatus.BAD_GATEWAY, "No se pudo completar la consulta al asistente");
    } catch (IOException ex) {
      throw new ExcepcionApi(HttpStatus.BAD_GATEWAY, "No se pudo conectar con el asistente");
    }
  }

  private ChatbotConversacionEntidad obtenerOCrearConversacion(UsuarioEntidad usuario, LocalDate fechaConversacion) {
    return conversacionRepositorio.findByUsuarioIdAndFechaConversacion(usuario.getId(), fechaConversacion)
        .orElseGet(() -> {
          ChatbotConversacionEntidad conversacion = new ChatbotConversacionEntidad();
          conversacion.setUsuario(usuario);
          conversacion.setFechaConversacion(fechaConversacion);
          conversacion.setTitulo("Chat del " + fechaConversacion);
          return conversacionRepositorio.save(conversacion);
        });
  }

  private LocalDate resolverFechaConversacion(LocalDate fechaConversacion) {
    LocalDate hoy = LocalDate.now(ZONA_HORARIA);
    if (fechaConversacion == null) {
      return hoy;
    }
    if (fechaConversacion.isAfter(hoy)) {
      throw new ExcepcionApi(HttpStatus.BAD_REQUEST, "La fecha de conversacion no puede ser futura");
    }
    return fechaConversacion;
  }

  private List<ChatbotMensajeDto> historialReciente(ChatbotConversacionEntidad conversacion) {
    List<ChatbotMensajeEntidad> mensajes = new ArrayList<>(
        mensajeRepositorio.findTop8ByConversacionOrderByCreadoEnDesc(conversacion));
    Collections.reverse(mensajes);
    return mensajes.stream()
        .map(mensaje -> new ChatbotMensajeDto(
            mensaje.getRol() == RolMensajeChatbot.ASISTENTE ? "assistant" : "user",
            mensaje.getContenido()))
        .toList();
  }

  private void guardarMensaje(
      ChatbotConversacionEntidad conversacion,
      RolMensajeChatbot rol,
      String contenido,
      String proveedor,
      String modeloUsado,
      boolean iaDisponible) {
    ChatbotMensajeEntidad mensaje = new ChatbotMensajeEntidad();
    mensaje.setConversacion(conversacion);
    mensaje.setRol(rol);
    mensaje.setContenido(contenido);
    mensaje.setProveedor(proveedor);
    mensaje.setModelo(modeloUsado);
    mensaje.setIaDisponible(iaDisponible);
    mensajeRepositorio.save(mensaje);
  }

  private ChatbotMensajeHistorialDto aMensajeHistorial(ChatbotMensajeEntidad mensaje) {
    return ChatbotMensajeHistorialDto.builder()
        .id(mensaje.getId())
        .rol(mensaje.getRol() == RolMensajeChatbot.ASISTENTE ? "assistant" : "user")
        .contenido(mensaje.getContenido())
        .proveedor(mensaje.getProveedor())
        .modelo(mensaje.getModelo())
        .iaDisponible(mensaje.isIaDisponible())
        .creadoEn(mensaje.getCreadoEn())
        .build();
  }

  private String consultarOpenAi(ChatbotSolicitudDto solicitud) throws IOException, InterruptedException {
    ObjectNode body = objectMapper.createObjectNode();
    body.put("model", modelo);
    body.put("instructions", contextoChatbot.instruccionesSistema());
    body.put("max_output_tokens", 900);
    body.put("input", construirInput(solicitud));
    ObjectNode reasoning = objectMapper.createObjectNode();
    reasoning.put("effort", "minimal");
    body.set("reasoning", reasoning);
    ObjectNode text = objectMapper.createObjectNode();
    text.put("verbosity", "low");
    body.set("text", text);

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(OPENAI_RESPONSES_URL))
        .timeout(Duration.ofSeconds(20))
        .header("Authorization", "Bearer " + apiKey)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      throw new ExcepcionApi(HttpStatus.BAD_GATEWAY, "El asistente no pudo responder en este momento");
    }

    return extraerTextoRespuesta(response.body());
  }

  private List<ChatbotMensajeDto> ultimosMensajes(List<ChatbotMensajeDto> historial) {
    if (historial == null || historial.isEmpty()) {
      return List.of();
    }
    int desde = Math.max(0, historial.size() - MAX_HISTORIAL_ENVIADO);
    return new ArrayList<>(historial.subList(desde, historial.size()));
  }

  private String construirInput(ChatbotSolicitudDto solicitud) {
    StringBuilder builder = new StringBuilder();
    List<ChatbotMensajeDto> historial = ultimosMensajes(solicitud.historial());
    if (!historial.isEmpty()) {
      builder.append("Historial reciente:\n");
      for (ChatbotMensajeDto mensaje : historial) {
        String autor = "assistant".equals(mensaje.rol()) ? "Asistente" : "Usuario";
        builder.append(autor).append(": ").append(mensaje.contenido().trim()).append('\n');
      }
      builder.append('\n');
    }
    builder.append("Consulta actual del usuario: ").append(solicitud.mensaje().trim());
    return builder.toString();
  }

  private String extraerTextoRespuesta(String responseBody) throws IOException {
    JsonNode root = objectMapper.readTree(responseBody);
    JsonNode outputText = root.path("output_text");
    if (outputText.isTextual() && StringUtils.hasText(outputText.asText())) {
      return outputText.asText().trim();
    }

    for (JsonNode output : root.path("output")) {
      for (JsonNode content : output.path("content")) {
        JsonNode text = content.path("text");
        if (text.isTextual() && StringUtils.hasText(text.asText())) {
          return text.asText().trim();
        }
      }
    }

    return "Puedo ayudarte con reportes, registro y seguimiento. Intenta reformular tu consulta en una frase corta.";
  }

  private ChatbotRespuestaDto respuestaLocal(String mensaje) {
    String normalizado = mensaje == null ? "" : mensaje.toLowerCase();
    String respuesta;
    if (contextoChatbot.esConsultaSobreEstados(normalizado)) {
      respuesta = contextoChatbot.respuestaGuiaEstadosReporte();
    } else if (contextoChatbot.esConsultaSobreContacto(normalizado)) {
      respuesta = contextoChatbot.respuestaContacto();
    } else if (contextoChatbot.esConsultaSobreSistema(normalizado)) {
      respuesta = contextoChatbot.respuestaSistema();
    } else if (normalizado.contains("reporte") || normalizado.contains("incidencia") || normalizado.contains("fuga")) {
      respuesta = "Para hacer un reporte entra a Reportar, selecciona tu distrito, marca el punto en el mapa, elige el tipo de incidencia, agrega una descripcion y adjunta fotos si las tienes. Luego revisa el resumen y envia el reporte.";
    } else if (normalizado.contains("registr")) {
      respuesta = "Para registrarte presiona Registrarse en la cabecera, coloca tu nombre, correo y una contrasena segura. Despues podras iniciar sesion para enviar reportes y hacer seguimiento.";
    } else if (normalizado.contains("seguimiento") || normalizado.contains("estado") || normalizado.contains("consulta")) {
      respuesta = "Para consultar el avance entra a Mis Reportes e ingresa tu numero de consulta. Ahi veras el estado, historial y resolucion cuando corresponda.";
    } else {
      respuesta = "Puedo orientarte sobre como reportar incidencias de agua, registrarte, consultar tus reportes o contactar soporte de AquaComunidad.";
    }
    return new ChatbotRespuestaDto(respuesta, "local", "fallback", false, null, null, false, accionesParaRespuesta(mensaje, respuesta));
  }

  private ChatbotRespuestaDto respuestaFueraDeAlcance() {
    return new ChatbotRespuestaDto(
        "Solo puedo ayudarte con AquaComunidad: reportar incidencias, registrarte, consultar tus reportes, seguimiento, contacto y uso de la plataforma.",
        "local",
        "scope-filter",
        false);
  }

  private ChatbotRespuestaDto respuestaIaNoDisponibleParaConsultaConceptual() {
    return new ChatbotRespuestaDto(
        contextoChatbot.respuestaIaNoDisponibleParaConsultaConceptual(),
        "local",
        "ia-required",
        false);
  }

  private ChatbotRespuestaDto responderMisReportes(String mensaje) {
    Long usuarioId = usuarioIdAutenticadoSeguro();
    if (usuarioId == null) {
      return new ChatbotRespuestaDto(
          "Para ver tus reportes personales debes iniciar sesion. Tambien puedes entrar a Mis Reportes y consultar con tu numero de reporte.",
          "local",
          "reportes-service",
          false,
          null,
          null,
          false,
          List.of(new ChatbotAccionDto("Ver Mis Reportes", "/mis-reportes")));
    }

    ReporteResumenDto resumen = reporteServicio.obtenerResumenMisReportes(usuarioId);
    if (resumen.getTotal() == 0) {
      return new ChatbotRespuestaDto(
          "No tienes reportes registrados por ahora. Para crear uno, entra a Reportar y completa ubicacion, tipo de incidencia, descripcion y evidencia.",
          "local",
          "reportes-service",
          false);
    }

    Long reporteId = extraerReporteId(mensaje);
    if (reporteId != null) {
      return responderReportePorCodigo(usuarioId, reporteId);
    }
    if (esConsultaUltimoReporte(mensaje)) {
      return responderUltimoReporte(reporteServicio.obtenerUltimoReporte(usuarioId));
    }
    if (esConsultaCantidadReportes(mensaje)) {
      return responderResumenReportes(resumen);
    }

    StringBuilder respuesta = new StringBuilder("Estos son tus reportes recientes:\n");
    reporteServicio.listarMisReportesRecientes(usuarioId, 5).stream()
        .forEach(reporte -> respuesta
            .append("- REP-")
            .append(reporte.getId())
            .append(": ")
            .append(reporte.getTipo())
            .append(" en ")
            .append(reporte.getZona())
            .append(" - ")
            .append(contextoChatbot.etiquetaEstadoReporte(reporte.getEstado().name()))
            .append(".\n"));
    respuesta.append("Para revisar la trazabilidad completa, entra a Mis Reportes.");
    return new ChatbotRespuestaDto(
        respuesta.toString(),
        "local",
        "reportes-service",
        false,
        null,
        null,
        false,
        List.of(new ChatbotAccionDto("Ver Mis Reportes", "/mis-reportes")));
  }

  private ChatbotRespuestaDto responderResumenReportes(ReporteResumenDto resumen) {
    StringBuilder respuesta = new StringBuilder()
        .append("Tienes ")
        .append(resumen.getTotal())
        .append(" ")
        .append(pluralizar(resumen.getTotal(), "reporte", "reportes"))
        .append(": ")
        .append(fragmentoCantidad(resumen.getPendientes(), "pendiente", "pendientes"))
        .append(", ")
        .append(fragmentoCantidad(resumen.getEnProceso(), "en proceso", "en proceso"))
        .append(" y ")
        .append(fragmentoCantidad(resumen.getResueltos(), "resuelto", "resueltos"));
    if (resumen.getDuplicados() > 0 || resumen.getRechazados() > 0 || resumen.getEscalados() > 0) {
      respuesta
          .append(". Tambien tienes ")
          .append(fragmentoCantidad(resumen.getDuplicados(), "duplicado", "duplicados"))
          .append(", ")
          .append(fragmentoCantidad(resumen.getRechazados(), "rechazado", "rechazados"))
          .append(" y ")
          .append(fragmentoCantidad(resumen.getEscalados(), "escalado", "escalados"));
    }
    respuesta.append(".");

    return new ChatbotRespuestaDto(
        respuesta.toString(),
        "local",
        "reportes-service",
        false,
        null,
        null,
        false,
        List.of(new ChatbotAccionDto("Ver Mis Reportes", "/mis-reportes")));
  }

  private ChatbotRespuestaDto responderUltimoReporte(ReporteResumenItemDto ultimo) {
    String estado = contextoChatbot.etiquetaEstadoReporte(ultimo.getEstado().name());
    String estadoDetalle = ultimo.getEstado().name().equals("RESUELTO")
        ? "ya esta resuelto"
        : "esta " + estado.toLowerCase();
    String respuesta = "Tu ultimo reporte es REP-" + ultimo.getId()
        + ": " + ultimo.getTipo()
        + " en " + ultimo.getZona()
        + " y " + estadoDetalle + ". "
        + contextoChatbot.explicacionEstadoReporte(ultimo.getEstado().name());
    return new ChatbotRespuestaDto(
        respuesta,
        "local",
        "reportes-service",
        false,
        null,
        null,
        false,
        List.of(new ChatbotAccionDto("Ver Mis Reportes", "/mis-reportes")));
  }

  private ChatbotRespuestaDto responderReportePorCodigo(Long usuarioId, Long reporteId) {
    ReporteRespuestaDto reporte;
    try {
      reporte = reporteServicio.obtenerMiReporte(reporteId, usuarioId);
    } catch (ExcepcionApi ex) {
      return new ChatbotRespuestaDto(
          "No encontre el reporte REP-" + reporteId + " entre tus reportes. Verifica el numero de consulta en Mis Reportes.",
          "local",
          "reportes-service",
          false,
          null,
          null,
          false,
          List.of(new ChatbotAccionDto("Ver Mis Reportes", "/mis-reportes")));
    }

    String respuesta = "El reporte REP-" + reporte.getId()
        + " (" + reporte.getTipo()
        + " en " + reporte.getZona()
        + ") esta en estado " + contextoChatbot.etiquetaEstadoReporte(reporte.getEstado().name()) + ". "
        + contextoChatbot.explicacionEstadoReporte(reporte.getEstado().name());
    return new ChatbotRespuestaDto(
        respuesta,
        "local",
        "reportes-service",
        false,
        null,
        null,
        false,
        List.of(new ChatbotAccionDto("Ver Mis Reportes", "/mis-reportes")));
  }

  private List<ChatbotAccionDto> accionesParaRespuesta(String mensajeUsuario, String respuesta) {
    List<ChatbotAccionDto> accionesPorIntencion = accionesParaTexto(mensajeUsuario);
    if (!accionesPorIntencion.isEmpty()) {
      return accionesPorIntencion;
    }
    return accionesParaTexto(respuesta);
  }

  private List<ChatbotAccionDto> accionesParaTexto(String textoOriginal) {
    String texto = normalizar(textoOriginal);
    if (contieneAlguno(texto, "registr", "crear cuenta", "cuenta")) {
      return List.of(new ChatbotAccionDto("Registrarse", "/inicio?auth=registro"));
    }
    if (contieneAlguno(texto, "iniciar sesion", "iniciar sesión", "acceder", "login")) {
      return List.of(new ChatbotAccionDto("Acceder", "/inicio?auth=login"));
    }
    if (contieneAlguno(texto, "mis reportes", "seguimiento", "estado", "trazabilidad", "consulta")) {
      return List.of(new ChatbotAccionDto("Ver Mis Reportes", "/mis-reportes"));
    }
    if (contieneAlguno(texto, "reportar", "reporte", "incidencia", "fuga")) {
      return List.of(new ChatbotAccionDto("Ir a reportar", "/reportar"));
    }
    if (contieneAlguno(texto, "contacto", "soporte", "emergencia")) {
      return List.of(new ChatbotAccionDto("Ir a Contacto", "/contacto"));
    }
    return List.of();
  }

  private Long usuarioIdAutenticadoSeguro() {
    try {
      return SeguridadContextoUtil.idUsuarioAutenticado();
    } catch (ExcepcionApi ex) {
      return null;
    }
  }

  private UsuarioEntidad usuarioAutenticadoSeguro() {
    try {
      return SeguridadContextoUtil.usuarioAutenticado();
    } catch (ExcepcionApi ex) {
      return null;
    }
  }

  private boolean esConsultaMisReportes(String mensaje) {
    String normalizado = normalizar(mensaje);
    return contieneAlguno(normalizado, "mis reportes", "reportes tengo", "que reportes", "estado de mis reportes",
        "ver mis incidencias", "mis incidencias", "seguimiento de mis reportes")
        || esConsultaCantidadReportes(normalizado)
        || esConsultaUltimoReporte(normalizado)
        || extraerReporteId(normalizado) != null;
  }

  private boolean esConsultaCantidadReportes(String mensaje) {
    String normalizado = normalizar(mensaje);
    return contieneAlguno(normalizado, "cuantos reportes", "cuántos reportes", "cantidad de reportes",
        "total de reportes", "reportes pendientes", "pendientes tengo", "cantidad por estado");
  }

  private boolean esConsultaUltimoReporte(String mensaje) {
    String normalizado = normalizar(mensaje);
    return contieneAlguno(normalizado, "ultimo reporte", "último reporte", "reporte mas reciente",
        "reporte más reciente", "lo ultimo que reporte", "lo último que reporté", "ya esta acabado",
        "ya está acabado", "ya esta resuelto", "ya está resuelto");
  }

  private boolean esPreguntaFueraDeAlcance(String mensaje) {
    String normalizado = normalizar(mensaje);
    boolean temaAquaComunidad = contieneAlguno(normalizado,
        "aquacomunidad",
        "reporte",
        "reportar",
        "incidencia",
        "fuga",
        "agua",
        "presion",
        "presión",
        "corte",
        "turbia",
        "registro",
        "registrar",
        "registrarme",
        "iniciar sesion",
        "iniciar sesión",
        "login",
        "acceder",
        "seguimiento",
        "trazabilidad",
        "estado",
        "contacto",
        "soporte",
        "emergencia",
        "telefono",
        "teléfono",
        "whatsapp",
        "horario",
        "cobertura",
        "empresa",
        "foto",
        "evidencia",
        "distrito",
        "mapa",
        "plataforma");
    if (temaAquaComunidad) {
      return false;
    }
    return contieneAlguno(normalizado,
        "cuanto es",
        "cuánto es",
        "calcula",
        "chiste",
        "clima",
        "lima",
        "tarea",
        "poema",
        "historia",
        "traducir",
        "programa",
        "codigo",
        "código")
        || normalizado.matches(".*\\d+\\s*[+\\-*/]\\s*\\d+.*");
  }

  private boolean contieneAlguno(String texto, String... terminos) {
    for (String termino : terminos) {
      if (texto.contains(termino)) {
        return true;
      }
    }
    return false;
  }

  private Long extraerReporteId(String texto) {
    var matcher = Pattern.compile("\\b(?:rep-?|reporte\\s+)(\\d+)\\b", Pattern.CASE_INSENSITIVE)
        .matcher(texto == null ? "" : texto);
    if (!matcher.find()) {
      return null;
    }
    return Long.valueOf(matcher.group(1));
  }

  private String normalizar(String texto) {
    return texto == null ? "" : texto.toLowerCase().trim();
  }

  private String fragmentoCantidad(long cantidad, String singular, String plural) {
    return cantidad + " " + pluralizar(cantidad, singular, plural);
  }

  private String pluralizar(long cantidad, String singular, String plural) {
    return cantidad == 1 ? singular : plural;
  }

}
