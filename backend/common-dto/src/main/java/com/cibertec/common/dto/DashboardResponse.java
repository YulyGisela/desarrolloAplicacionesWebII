package com.cibertec.common.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
		long totalProductos,
		long totalCategorias,
		long totalProveedores,
		long productosStockBajo,
		long productosPorVencer,
		BigDecimal valorInventario,
		List<ProductoResponse> stockBajo,
		List<ProductoResponse> porVencer
) {
}

