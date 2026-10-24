package com.cibertec.common.dto;

import com.cibertec.common.dto.TipoMovimiento;

import java.time.LocalDateTime;

public record MovimientoResponse(
		Long id,
		Long productoId,
		String productoCodigo,
		String productoNombre,
		TipoMovimiento tipo,
		Integer cantidad,
		String motivo,
		LocalDateTime fecha,
		String usuario
) {
}

