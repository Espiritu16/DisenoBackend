package com.aquacomunidad.backend.features.chatbot.support;

import org.springframework.stereotype.Component;

@Component
public class ContextoChatbotAquaComunidad {

  public String instruccionesSistema() {
    return """
        Eres el asistente virtual de AquaComunidad, una plataforma ciudadana para reportar incidencias de agua.
        Responde siempre en espanol, de forma breve, clara y util.
        Ayuda solo con temas de la plataforma: registro, inicio de sesion, reportar incidencias, adjuntar evidencia, consultar reportes, trazabilidad, contacto y uso general.
        No inventes estados, numeros de reporte, datos personales, responsables, fechas ni informacion de base de datos.
        Identidad del sistema:
        - AquaComunidad ayuda a una comunidad a reportar problemas de agua, revisar avances y mantener trazabilidad desde cualquier dispositivo.
        - No es una empresa prestadora de agua; es una plataforma de gestion y seguimiento ciudadano.
        - La vista Inicio presenta el proceso, aliados/referentes de saneamiento y accesos a Reportar, Mis Reportes y Contacto.
        Rutas publicas:
        - Inicio: /inicio.
        - Reportar: /reportar.
        - Mis Reportes: /mis-reportes.
        - Contacto: /contacto.
        - Registro: /inicio?auth=registro.
        - Acceso: /inicio?auth=login.
        Roles:
        - Ciudadano: registra reportes, adjunta evidencia y consulta su seguimiento.
        - Administrador: valida reportes, detecta duplicados, deriva casos y gestiona usuarios.
        - Operador: atiende casos asignados, actualiza avances, escala o cierra con evidencia.
        Proceso de atencion:
        - Reporta: el ciudadano registra ubicacion, tipo de incidencia y evidencias.
        - Valida: el reporte queda pendiente y se revisa si es valido o duplicado.
        - Asigna: el administrador deriva el reporte y crea un caso con responsable.
        - Atiende: el operador trabaja el caso, actualiza avances o escala si corresponde.
        - Consulta: el vecino revisa trazabilidad, historial y resolucion del caso.
        Contacto:
        - Telefono principal: +51 999 000 111 para consultas generales, servicios, plataforma, orientacion ciudadana, informacion institucional, cobertura y horarios.
        - Soporte tecnico: +51 944 555 221 para problemas de presion, fugas reportadas, calidad del agua, incidencias operativas, validacion de reportes y casos en atencion.
        - WhatsApp general: +51 999 000 111 para atencion rapida.
        - WhatsApp soporte: +51 944 555 221 para seguimiento de incidencias.
        - Horario de atencion: Lun - Sab, 8:00 AM - 6:00 PM.
        - Emergencias: atencion 24/7 para casos criticos.
        - Cobertura: Provincia de Lima.
        Tipos de incidencia:
        - Fuga: perdida visible de agua en tuberias, conexiones, pistas, veredas o medidores.
        - Baja presion: el agua llega con poca fuerza o de forma irregular.
        - Agua turbia: agua con color, olor, sedimentos o apariencia anormal.
        - Corte: interrupcion no programada o falta de servicio de agua.
        Si el usuario necesita crear un reporte, guialo a Reportar y menciona: distrito, ubicacion en mapa, tipo de incidencia, direccion detectada, referencia, descripcion, evidencia y resumen antes de enviar.
        Si pregunta por seguimiento, guialo a Mis Reportes con su numero de consulta o con su sesion iniciada.
        Estados de reporte:
        - Pendiente: el reporte fue recibido y espera validacion o derivacion.
        - En proceso: el reporte ya fue derivado a un caso operativo y esta siendo atendido.
        - Escalado: el caso requiere una revision o atencion de mayor prioridad.
        - Resuelto: el caso fue atendido y cerrado; para cerrar como resuelto el equipo debe registrar evidencia u observacion de cierre.
        - Rechazado: el reporte no procede por informacion insuficiente, invalida o fuera del alcance.
        - Duplicado: el reporte coincide con otra incidencia ya registrada y se evita repetir la atencion.
        Si pregunta si un reporte ya termino, explica que el estado que confirma cierre es Resuelto.
        Si pide soporte urgente, recomiendale ir a Contacto y usar el canal de emergencias.
        Si pregunta algo fuera del sistema, indica que solo puedes ayudar con AquaComunidad.
        """;
  }

  public boolean esConsultaSobreSistema(String mensaje) {
    String texto = normalizar(mensaje);
    return contieneAlguno(texto,
        "que es aquacomunidad",
        "qué es aquacomunidad",
        "sobre aquacomunidad",
        "nuestra empresa",
        "su empresa",
        "la empresa",
        "plataforma",
        "para que sirve",
        "para qué sirve",
        "quienes son",
        "quiénes son");
  }

  public boolean esConsultaSobreContacto(String mensaje) {
    String texto = normalizar(mensaje);
    return contieneAlguno(texto,
        "contacto",
        "telefono",
        "teléfono",
        "whatsapp",
        "soporte",
        "emergencia",
        "horario",
        "cobertura",
        "llamar");
  }

  public boolean esConsultaSobreEstados(String mensaje) {
    String texto = normalizar(mensaje);
    return contieneAlguno(texto,
        "terminado",
        "finalizado",
        "acabado",
        "cerrado",
        "resuelto",
        "estado",
        "pendiente",
        "en proceso",
        "escalado",
        "rechazado",
        "duplicado");
  }

  public boolean esConsultaConceptualParaIa(String mensaje) {
    String texto = normalizar(mensaje);
    boolean pideDefinicion = contieneAlguno(texto,
        "que es",
        "qué es",
        "que significa",
        "qué significa",
        "explica",
        "explicame",
        "explícame",
        "definicion",
        "definición");
    boolean temaIncidencia = contieneAlguno(texto,
        "fuga",
        "baja presion",
        "baja presión",
        "agua turbia",
        "corte",
        "incidencia",
        "presion",
        "presión");
    return pideDefinicion && temaIncidencia;
  }

  public String respuestaGuiaEstadosReporte() {
    return "Un reporte se considera terminado cuando aparece en estado Resuelto. "
        + "Pendiente significa que fue recibido y espera validacion. "
        + "En proceso significa que ya fue derivado y esta siendo atendido. "
        + "Escalado significa que necesita una revision de mayor prioridad. "
        + "Rechazado significa que no procede por informacion insuficiente, invalida o fuera del alcance. "
        + "Duplicado significa que coincide con otra incidencia ya registrada. "
        + "Para confirmar tu caso especifico entra a Mis Reportes y revisa el historial.";
  }

  public String respuestaSistema() {
    return "AquaComunidad es una plataforma ciudadana para reportar incidencias de agua, "
        + "consultar avances y mantener trazabilidad clara de cada caso. Sirve para que el ciudadano registre "
        + "ubicacion, tipo de problema y evidencia; luego el equipo valida, asigna, atiende y actualiza el estado "
        + "hasta la resolucion.";
  }

  public String respuestaContacto() {
    return "Canales de AquaComunidad: Telefono principal +51 999 000 111 para consultas generales. "
        + "Soporte tecnico +51 944 555 221 para incidencias operativas y validacion de reportes. "
        + "WhatsApp general +51 999 000 111 y WhatsApp soporte +51 944 555 221. "
        + "Horario: Lun - Sab, 8:00 AM - 6:00 PM. Emergencias 24/7 para casos criticos. "
        + "Cobertura: Provincia de Lima.";
  }

  public String respuestaIaNoDisponibleParaConsultaConceptual() {
    return "Esa explicacion requiere el asistente con IA para responderla guiandose del contexto de AquaComunidad. "
        + "Ahora la IA no esta disponible porque falta configurar la API key.";
  }

  public String etiquetaEstadoReporte(String estado) {
    return switch (estado) {
      case "PENDIENTE" -> "Pendiente";
      case "EN_PROCESO" -> "En proceso";
      case "RESUELTO" -> "Resuelto";
      case "DUPLICADO" -> "Duplicado";
      case "RECHAZADO" -> "Rechazado";
      case "ESCALADO" -> "Escalado";
      default -> estado;
    };
  }

  public String descripcionEstadoReporte(String estado) {
    return switch (estado) {
      case "PENDIENTE" -> "Fue recibido y espera validacion o derivacion.";
      case "EN_PROCESO" -> "Ya fue derivado a un caso operativo y esta siendo atendido.";
      case "RESUELTO" -> "El caso fue atendido y cerrado; ese estado indica que el reporte ya esta terminado.";
      case "DUPLICADO" -> "Coincide con otra incidencia ya registrada y se evita duplicar la atencion.";
      case "RECHAZADO" -> "No procede por informacion insuficiente, invalida o fuera del alcance.";
      case "ESCALADO" -> "Necesita una revision o atencion de mayor prioridad.";
      default -> "Revisa el historial para conocer el detalle actualizado.";
    };
  }

  public String explicacionEstadoReporte(String estado) {
    return "Estado " + etiquetaEstadoReporte(estado) + ": " + descripcionEstadoReporte(estado);
  }

  private boolean contieneAlguno(String texto, String... terminos) {
    for (String termino : terminos) {
      if (texto.contains(termino)) {
        return true;
      }
    }
    return false;
  }

  private String normalizar(String texto) {
    return texto == null ? "" : texto.toLowerCase().trim();
  }
}
