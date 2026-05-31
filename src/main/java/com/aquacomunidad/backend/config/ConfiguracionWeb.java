package com.aquacomunidad.backend.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ConfiguracionWeb implements WebMvcConfigurer {

  private final String uploadDir;
  private final long cacheMaxAgeSeconds;
  private final String[] allowedOriginPatterns;

  public ConfiguracionWeb(
      @Value("${app.upload.dir:${UPLOAD_DIR:/app/uploads}}") String uploadDir,
      @Value("${app.upload.cache.max-age-seconds:2592000}") long cacheMaxAgeSeconds,
      @Value("${app.cors.allowed-origin-patterns:https://diseno-frontend.vercel.app,https://proyectoutp.com,http://proyectoutp.com,http://localhost:*,https://localhost:*,http://127.0.0.1:*,https://127.0.0.1:*}") String[] allowedOriginPatterns) {
    this.uploadDir = uploadDir;
    this.cacheMaxAgeSeconds = Math.max(cacheMaxAgeSeconds, 0);
    this.allowedOriginPatterns = allowedOriginPatterns;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOriginPatterns(allowedOriginPatterns)
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .exposedHeaders("Authorization")
        .allowCredentials(true);
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
    String location = root.toUri().toString();
    if (!location.endsWith("/")) {
      location = location + "/";
    }
    registry.addResourceHandler("/uploads/**")
        .addResourceLocations(location)
        .setCacheControl(CacheControl.maxAge(Duration.ofSeconds(cacheMaxAgeSeconds)).cachePublic());
  }
}
