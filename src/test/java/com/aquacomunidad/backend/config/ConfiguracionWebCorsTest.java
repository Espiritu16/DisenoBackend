package com.aquacomunidad.backend.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

@SpringBootTest
@AutoConfigureMockMvc
class ConfiguracionWebCorsTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private CorsConfigurationSource corsConfigurationSource;

  @Test
  void permiteFrontendDeVercel() throws Exception {
    mockMvc.perform(options("/api/v1/auth/login")
        .header("Origin", "https://diseno-frontend.vercel.app")
        .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "https://diseno-frontend.vercel.app"));
  }

  @Test
  void permitePreviewDeVercelDelFrontend() throws Exception {
    String origin = "https://diseno-frontend-7n1672j2y-espiritu16s-projects.vercel.app";
    MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/v1/chatbot/mensajes");
    var corsConfiguration = corsConfigurationSource.getCorsConfiguration(request);
    assertThat(corsConfiguration.checkOrigin(origin))
        .as("allowed origin patterns %s", corsConfiguration.getAllowedOriginPatterns())
        .isEqualTo(origin);

    mockMvc.perform(options("/api/v1/chatbot/mensajes")
        .header("Origin", origin)
        .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", origin));
  }

  @Test
  void permiteDominioDelVpsConHttp() throws Exception {
    mockMvc.perform(options("/api/v1/auth/login")
        .header("Origin", "http://proyectoutp.com")
        .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://proyectoutp.com"));
  }

  @Test
  void permiteDominioDelVpsConHttps() throws Exception {
    mockMvc.perform(options("/api/v1/auth/login")
        .header("Origin", "https://proyectoutp.com")
        .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "https://proyectoutp.com"));
  }

  @Test
  void noPermiteIpDelVpsPorDefecto() throws Exception {
    mockMvc.perform(options("/api/v1/auth/login")
        .header("Origin", "http://85.239.248.109")
        .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isForbidden())
        .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
  }

  @Test
  void permiteLocalhostConCualquierPuerto() throws Exception {
    mockMvc.perform(options("/api/v1/auth/login")
        .header("Origin", "http://localhost:4200")
        .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
  }

  @Test
  void permiteConsultarAlertasDelServicioSinSesion() throws Exception {
    mockMvc.perform(get("/api/v1/estado-servicio/alertas"))
        .andExpect(status().isOk());
  }

  @Test
  void permiteConsultarZonasDelServicioSinSesion() throws Exception {
    mockMvc.perform(get("/api/v1/estado-servicio/zonas"))
        .andExpect(status().isOk());
  }

  @Test
  void permiteLoopbackConCualquierPuerto() throws Exception {
    mockMvc.perform(options("/api/v1/auth/login")
        .header("Origin", "http://127.0.0.1:5173")
        .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://127.0.0.1:5173"));
  }
}
