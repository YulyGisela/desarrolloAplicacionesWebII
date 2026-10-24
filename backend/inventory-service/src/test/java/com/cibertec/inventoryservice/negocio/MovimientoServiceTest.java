package com.cibertec.inventoryservice.negocio;

import com.cibertec.common.dto.MovimientoRequest;
import com.cibertec.common.dto.MovimientoResponse;
import com.cibertec.common.dto.ProductoResponse;
import com.cibertec.common.dto.TipoMovimiento;
import com.cibertec.inventoryservice.client.CatalogServiceProxy;
import com.cibertec.inventoryservice.entidades.MovimientoStock;
import com.cibertec.inventoryservice.repositorio.MovimientoStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

	@Mock
	private MovimientoStockRepository movimientoStockRepository;

	@Mock
	private CatalogServiceProxy catalogServiceProxy;

	private MovimientoService movimientoService;
	private ProductoResponse producto;

	@BeforeEach
	void setUp() {
		movimientoService = new MovimientoService(movimientoStockRepository, catalogServiceProxy);
		producto = new ProductoResponse(
				1L,
				"ABR-001",
				"Arroz extra 5 kg",
				null,
				new BigDecimal("18.50"),
				new BigDecimal("22.00"),
				10,
				5,
				LocalDate.now().plusMonths(6),
				1L,
				"Abarrotes",
				null,
				null,
				false
		);
	}

	@Test
	@DisplayName("Una ENTRADA suma la cantidad al stock")
	void entradaSumaStock() {
		when(catalogServiceProxy.getProductoById(1L)).thenReturn(producto);
		when(movimientoStockRepository.save(any(MovimientoStock.class))).thenAnswer(inv -> inv.getArgument(0));

		MovimientoResponse response = movimientoService.registrarMovimiento(
				new MovimientoRequest(1L, TipoMovimiento.ENTRADA, 5, "Compra a proveedor"), "admin");

		verify(catalogServiceProxy).updateStock(1L, 15);
		assertEquals(TipoMovimiento.ENTRADA, response.tipo());
		assertEquals("admin", response.usuario());
	}

	@Test
	@DisplayName("Una SALIDA con stock suficiente resta la cantidad")
	void salidaRestaStock() {
		when(catalogServiceProxy.getProductoById(1L)).thenReturn(producto);
		when(movimientoStockRepository.save(any(MovimientoStock.class))).thenAnswer(inv -> inv.getArgument(0));

		movimientoService.registrarMovimiento(new MovimientoRequest(1L, TipoMovimiento.SALIDA, 4, "Venta"), "vendedor");

		verify(catalogServiceProxy).updateStock(1L, 6);
	}

	@Test
	@DisplayName("Una SALIDA mayor al stock responde 409 y no modifica nada")
	void salidaConStockInsuficiente() {
		when(catalogServiceProxy.getProductoById(1L)).thenReturn(producto);

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> movimientoService.registrarMovimiento(
						new MovimientoRequest(1L, TipoMovimiento.SALIDA, 20, "Venta"), "vendedor"));

		assertEquals(409, ex.getStatusCode().value());
		verify(catalogServiceProxy, never()).updateStock(eq(1L), any());
		verify(movimientoStockRepository, never()).save(any());
	}

	@Test
	@DisplayName("Un movimiento sobre un producto inexistente responde 404")
	void productoInexistente() {
		when(catalogServiceProxy.getProductoById(99L))
				.thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> movimientoService.registrarMovimiento(
						new MovimientoRequest(99L, TipoMovimiento.ENTRADA, 1, null), "admin"));

		assertEquals(404, ex.getStatusCode().value());
	}
}
