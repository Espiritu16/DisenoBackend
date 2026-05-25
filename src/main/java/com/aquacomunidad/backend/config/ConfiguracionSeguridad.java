package com.aquacomunidad.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.aquacomunidad.backend.security.FiltroJwt;
import com.aquacomunidad.backend.security.ManejadorAccesoDenegado;
import com.aquacomunidad.backend.security.ManejadorAutenticacionNoValida;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class ConfiguracionSeguridad {

  private final FiltroJwt filtroJwt;
  private final ManejadorAutenticacionNoValida manejadorAutenticacionNoValida;
  private final ManejadorAccesoDenegado manejadorAccesoDenegado;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(manejadorAutenticacionNoValida)
            .accessDeniedHandler(manejadorAccesoDenegado))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/auth/**", "/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/uploads/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/reportes/mis-reportes").hasAnyRole("CIUDADANO", "ADMIN", "OPERADOR")
            .requestMatchers(HttpMethod.GET, "/api/v1/reportes").hasAnyRole("ADMIN", "OPERADOR")
            .requestMatchers(HttpMethod.GET, "/api/v1/reportes/*/trazabilidad")
            .hasAnyRole("CIUDADANO", "ADMIN", "OPERADOR")
            .requestMatchers("/api/v1/usuarios/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/v1/reportes").hasRole("CIUDADANO")
            .requestMatchers("/api/v1/casos/**").hasAnyRole("ADMIN", "OPERADOR")
            .requestMatchers("/api/v1/dashboard/**").hasAnyRole("ADMIN", "OPERADOR")
            .anyRequest().authenticated())
        .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
