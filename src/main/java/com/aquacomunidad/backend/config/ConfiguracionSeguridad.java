package com.aquacomunidad.backend.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.aquacomunidad.backend.security.FiltroJwt;
import com.aquacomunidad.backend.security.ManejadorAccesoDenegado;
import com.aquacomunidad.backend.security.ManejadorAutenticacionNoValida;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class ConfiguracionSeguridad {

  private static final String DEFAULT_ALLOWED_ORIGIN_PATTERNS = "https://diseno-frontend.vercel.app,"
      + "https://diseno-frontend-7n1672j2y-espiritu16s-projects.vercel.app,"
      + "https://*.vercel.app,"
      + "https://proyectoutp.com,"
      + "http://proyectoutp.com,"
      + "http://localhost:*,"
      + "https://localhost:*,"
      + "http://127.0.0.1:*,"
      + "https://127.0.0.1:*";

  private final FiltroJwt filtroJwt;
  private final ManejadorAutenticacionNoValida manejadorAutenticacionNoValida;
  private final ManejadorAccesoDenegado manejadorAccesoDenegado;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource)
      throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(manejadorAutenticacionNoValida)
            .accessDeniedHandler(manejadorAccesoDenegado))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/auth/**", "/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/uploads/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/v1/chatbot/mensajes").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/chatbot/**").authenticated()
            .requestMatchers(HttpMethod.GET, "/api/v1/estado-servicio/**")
            .hasAnyRole("CIUDADANO", "ADMIN", "OPERADOR", "AUTORIDAD")
            .requestMatchers(HttpMethod.POST, "/api/v1/estado-servicio/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/v1/estado-servicio/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PATCH, "/api/v1/estado-servicio/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/v1/estado-servicio/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/v1/iot/**").hasAnyRole("ADMIN", "OPERADOR", "AUTORIDAD")
            .requestMatchers(HttpMethod.POST, "/api/v1/iot/lecturas").hasAnyRole("ADMIN", "OPERADOR")
            .requestMatchers(HttpMethod.GET, "/api/v1/reportes/mis-reportes").hasAnyRole("CIUDADANO", "ADMIN", "OPERADOR")
            .requestMatchers(HttpMethod.GET, "/api/v1/reportes").hasAnyRole("ADMIN", "OPERADOR", "AUTORIDAD")
            .requestMatchers(HttpMethod.GET, "/api/v1/reportes/*/trazabilidad")
            .hasAnyRole("CIUDADANO", "ADMIN", "OPERADOR", "AUTORIDAD")
            .requestMatchers("/api/v1/usuarios/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/v1/uploads/reportes").hasRole("CIUDADANO")
            .requestMatchers(HttpMethod.POST, "/api/v1/uploads/casos").hasAnyRole("ADMIN", "OPERADOR")
            .requestMatchers(HttpMethod.POST, "/api/v1/reportes").hasRole("CIUDADANO")
            .requestMatchers("/api/v1/casos/**").hasAnyRole("ADMIN", "OPERADOR")
            .requestMatchers("/api/v1/dashboard/**").hasAnyRole("ADMIN", "OPERADOR", "AUTORIDAD")
            .anyRequest().authenticated())
        .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource(Environment environment) {
    String allowedOriginPatterns = environment.getProperty("CORS_ALLOWED_ORIGIN_PATTERNS");
    if (allowedOriginPatterns == null || allowedOriginPatterns.isBlank()) {
      allowedOriginPatterns = environment.getProperty(
        "app.cors.allowed-origin-patterns",
        DEFAULT_ALLOWED_ORIGIN_PATTERNS);
    }
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(separarPatrones(allowedOriginPatterns));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setExposedHeaders(List.of("Authorization"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  private List<String> separarPatrones(String value) {
    if (value == null || value.isBlank()) {
      return List.of();
    }
    return Arrays.stream(value.split(","))
        .map(String::trim)
        .filter(pattern -> !pattern.isBlank())
        .toList();
  }
}
