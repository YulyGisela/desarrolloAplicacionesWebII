package com.cibertec.common.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductoResponse(
		Long id,
		String codigo,
		String nombre,
		String descripcion,
		BigDecimal precioCompra,
		BigDecimal precioVenta,
		Integer stockActual,
		Integer stockMinimo,
		LocalDate fechaVencimiento,
		Long categoriaId,
		String categoriaNombre,
		Long proveedorId,
		String proveedorNombre,
		boolean stockBajo
) {
}

