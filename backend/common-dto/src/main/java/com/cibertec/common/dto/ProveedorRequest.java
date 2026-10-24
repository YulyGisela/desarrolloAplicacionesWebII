package com.cibertec.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProveedorRequest(
		@NotBlank(message = "La razÃ³n social es obligatoria")
		@Size(max = 120, message = "La razÃ³n social admite mÃ¡ximo 120 caracteres") String razonSocial,
		@NotBlank(message = "El RUC es obligatorio")
		@Pattern(regexp = "\\d{11}", message = "El RUC debe tener 11 dÃ­gitos") String ruc,
		@Size(max = 20, message = "El telÃ©fono admite mÃ¡ximo 20 caracteres") String telefono,
		@Email(message = "El correo no es vÃ¡lido") String email
) {
}

