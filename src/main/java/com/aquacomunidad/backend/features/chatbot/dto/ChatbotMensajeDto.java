package com.aquacomunidad.backend.features.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChatbotMensajeDto(
    @NotBlank(message = "El rol del mensaje es obligatorio")
    @Pattern(regexp = "user|assistant", message = "El rol debe ser user o assistant")
    String rol,

    @NotBlank(message = "El contenido del mensaje es obligatorio")
    @Size(max = 2500, message = "El contenido del mensaje no debe superar 2500 caracteres")
    String contenido) {
}
