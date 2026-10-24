package com.cibertec.common.dto;

public record LoginResponse(
		String token,
		String tokenType,
		Long expiresInMinutes,
		String username,
		String nombre,
		String role
) {
}

