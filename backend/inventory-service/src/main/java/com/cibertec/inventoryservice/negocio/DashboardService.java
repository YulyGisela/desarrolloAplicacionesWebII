package com.cibertec.inventoryservice.negocio;

import com.cibertec.common.dto.DashboardResponse;
import com.cibertec.common.dto.ProductoResponse;
import com.cibertec.inventoryservice.client.CatalogServiceProxy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class DashboardService {

	private static final int DIAS_ALERTA_VENCIMIENTO = 30;

	private final CatalogServiceProxy catalogServiceProxy;

	public DashboardService(CatalogServiceProxy catalogServiceProxy) {
		this.catalogServiceProxy = catalogServiceProxy;
	}

	public DashboardResponse getDashboard() {
		List<ProductoResponse> productos;
		long categoriasCount = 0;
		long proveedoresCount = 0;

		try {
			productos = catalogServiceProxy.getAllProductos();
		} catch (Exception e) {
			productos = Collections.emptyList();
		}

		try {
			categoriasCount = catalogServiceProxy.getAllCategorias().size();
		} catch (Exception ignored) {
		}

		try {
			proveedoresCount = catalogServiceProxy.getAllProveedores().size();
		} catch (Exception ignored) {
		}

		LocalDate limite = LocalDate.now().plusDays(DIAS_ALERTA_VENCIMIENTO);

		List<ProductoResponse> stockBajo = productos.stream()
				.filter(p -> p.stockActual() <= p.stockMinimo())
				.toList();

		List<ProductoResponse> porVencer = productos.stream()
				.filter(p -> p.fechaVencimiento() != null && !p.fechaVencimiento().isAfter(limite))
				.toList();

		BigDecimal valorInventario = productos.stream()
				.map(p -> p.precioCompra().multiply(BigDecimal.valueOf(p.stockActual())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		return new DashboardResponse(
				productos.size(),
				categoriasCount,
				proveedoresCount,
				stockBajo.size(),
				porVencer.size(),
				valorInventario,
				stockBajo,
				porVencer
		);
	}
}
