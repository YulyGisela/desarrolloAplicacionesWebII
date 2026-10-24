package com.cibertec.notificationservice.negocio;

import java.time.LocalDateTime;

public record Alerta(
		Long productoId,
		String productoCodigo,
		String productoNombre,
		Integer stockActual,
		Integer stockMinimo,
		String mensaje,
		LocalDateTime fecha
) {
}
