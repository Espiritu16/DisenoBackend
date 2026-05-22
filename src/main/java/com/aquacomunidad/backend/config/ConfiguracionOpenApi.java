package com.aquacomunidad.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class ConfiguracionOpenApi {

  @Bean
  public OpenAPI aquacomunidadOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("AquaComunidad API")
            .description("Documentacion de endpoints para AquaComunidad Backend")
            .version("v1")
            .contact(new Contact().name("AquaComunidad Team")));
  }
}
