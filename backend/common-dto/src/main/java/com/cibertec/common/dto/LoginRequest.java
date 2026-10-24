package com.cibertec.common.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank(message = "El usuario es obligatorio") String username,
		@NotBlank(message = "La contraseÃ±a es obligatoria") String password
) {
}

