package com.cibertec.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
		@NotBlank(message = "El nombre es obligatorio")
		@Size(max = 120, message = "El nombre admite mÃ¡ximo 120 caracteres") String nombre,
		@NotBlank(message = "El usuario es obligatorio")
		@Size(min = 4, max = 50, message = "El usuario debe tener entre 4 y 50 caracteres") String username,
		@NotBlank(message = "El correo es obligatorio")
		@Email(message = "El correo no es vÃ¡lido") String email,
		@NotBlank(message = "La contraseÃ±a es obligatoria")
		@Size(min = 6, message = "La contraseÃ±a debe tener mÃ­nimo 6 caracteres") String password
) {
}

