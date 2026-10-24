package com.cibertec.common.dto;

public record UsuarioResponse(
		Long id,
		String username,
		String nombre,
		String email,
		String role
) {
}

