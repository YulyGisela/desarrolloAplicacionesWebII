package com.cibertec.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
		@NotBlank(message = "El nombre es obligatorio")
		@Size(max = 80, message = "El nombre admite mÃ¡ximo 80 caracteres") String nombre,
		@Size(max = 255, message = "La descripciÃ³n admite mÃ¡ximo 255 caracteres") String descripcion
) {
}

