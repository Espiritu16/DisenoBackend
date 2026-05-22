package com.aquacomunidad.backend.features.autenticacion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestablecerContrasenaSolicitudDto {

  @NotBlank
  @Email
  private String correo;

  @NotBlank
  private String tokenRecuperacion;

  @NotBlank
  @Size(min = 8, message = "La nueva contrasena debe tener al menos 8 caracteres")
  private String nuevaContrasena;

  @NotBlank
  private String confirmarContrasena;
}
