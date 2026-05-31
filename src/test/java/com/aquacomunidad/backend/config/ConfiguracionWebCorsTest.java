package com.aquacomunidad.backend.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ConfiguracionWebCorsTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void permiteFrontendDeVercel() throws Exception {
    mockMvc.perform(options("/api/v1/auth/login")
        .header("Origin", "https://diseno-frontend.vercel.app")
        .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "https://diseno-frontend.vercel.app"));
  }

  @Test
  void permiteFrontendDelVps() throws Exception {
    mockMvc.perform(options("/api/v1/auth/login")
        .header("Origin", "http://85.239.248.109")
        .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://85.239.248.109"));
  }

  @Test
  void permiteFrontendDelVpsConHttps() throws Exception {
    mockMvc.perform(options("/api/v1/auth/login")
        .header("Origin", "https://85.239.248.109")
        .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "https://85.239.248.109"));
  }

  @Test
  void noPermiteLocalhostPorDefecto() throws Exception {
    mockMvc.perform(options("/api/v1/auth/login")
        .header("Origin", "http://localhost:4200")
        .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isForbidden())
        .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
  }
}
