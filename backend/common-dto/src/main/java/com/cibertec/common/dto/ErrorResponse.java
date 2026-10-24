package com.cibertec.common.dto;

import java.util.List;

public record ErrorResponse(
		int status,
		String message,
		List<String> errors
) {
}

