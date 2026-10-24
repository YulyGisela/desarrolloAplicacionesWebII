package com.cibertec.common.dto;

public record ProveedorResponse(
		Long id,
		String razonSocial,
		String ruc,
		String telefono,
		String email
) {
}

