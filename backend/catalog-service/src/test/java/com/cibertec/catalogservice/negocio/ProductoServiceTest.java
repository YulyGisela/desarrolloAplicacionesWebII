package com.cibertec.catalogservice.negocio;

import com.cibertec.catalogservice.repositorio.CategoriaRepository;
import com.cibertec.catalogservice.repositorio.ProductoRepository;
import com.cibertec.catalogservice.repositorio.ProveedorRepository;
import com.cibertec.common.dto.ProductoRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

	@Mock
	private ProductoRepository productoRepository;

	@Mock
	private CategoriaRepository categoriaRepository;

	@Mock
	private ProveedorRepository proveedorRepository;

	private ProductoService productoService;

	@BeforeEach
	void setUp() {
		productoService = new ProductoService(productoRepository, categoriaRepository, proveedorRepository);
	}

	private ProductoRequest request(String codigo, String compra, String venta) {
		return new ProductoRequest(codigo, "Arroz extra 5 kg", null,
				new BigDecimal(compra), new BigDecimal(venta), 10, 5, null, 1L, null);
	}

	@Test
	@DisplayName("No permite un precio de venta menor al precio de compra (400)")
	void precioVentaMenorAlDeCompra() {
		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> productoService.createProducto(request("ABR-001", "10.00", "8.00")));

		assertEquals(400, ex.getStatusCode().value());
	}

	@Test
	@DisplayName("No permite registrar un codigo de producto repetido (409)")
	void codigoRepetidoEnCreacion() {
		when(productoRepository.existsByCodigo("ABR-001")).thenReturn(true);

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> productoService.createProducto(request("ABR-001", "10.00", "12.00")));

		assertEquals(409, ex.getStatusCode().value());
	}
}
