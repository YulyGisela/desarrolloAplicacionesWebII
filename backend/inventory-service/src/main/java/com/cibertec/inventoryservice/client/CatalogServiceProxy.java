package com.cibertec.inventoryservice.client;

import com.cibertec.common.dto.ProductoResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@Service
public class CatalogServiceProxy {

	private static final Logger log = LoggerFactory.getLogger(CatalogServiceProxy.class);
	private final CatalogClient catalogClient;

	public CatalogServiceProxy(CatalogClient catalogClient) {
		this.catalogClient = catalogClient;
	}

	@CircuitBreaker(name = "catalogServiceCB", fallbackMethod = "fallbackGetProductoById")
	@Retry(name = "catalogServiceRetry")
	public ProductoResponse getProductoById(Long id) {
		return catalogClient.getProductoById(id);
	}

	@CircuitBreaker(name = "catalogServiceCB", fallbackMethod = "fallbackUpdateStock")
	@Retry(name = "catalogServiceRetry")
	public ProductoResponse updateStock(Long id, Integer nuevoStock) {
		return catalogClient.updateStock(id, nuevoStock);
	}

	@CircuitBreaker(name = "catalogServiceCB", fallbackMethod = "fallbackGetAllProductos")
	@Retry(name = "catalogServiceRetry")
	public List<ProductoResponse> getAllProductos() {
		return catalogClient.getAllProductos();
	}

	@CircuitBreaker(name = "catalogServiceCB", fallbackMethod = "fallbackGetAllCategorias")
	@Retry(name = "catalogServiceRetry")
	public List<Object> getAllCategorias() {
		return catalogClient.getAllCategorias();
	}

	@CircuitBreaker(name = "catalogServiceCB", fallbackMethod = "fallbackGetAllProveedores")
	@Retry(name = "catalogServiceRetry")
	public List<Object> getAllProveedores() {
		return catalogClient.getAllProveedores();
	}

	// --- Métodos Fallback con idéntica firma ---

	public ProductoResponse fallbackGetProductoById(Long id, Throwable ex) {
		log.warn("FALLBACK activado para getProductoById({}). Causa: {}", id, ex.getMessage());
		if (ex instanceof ResponseStatusException rse && rse.getStatusCode() == HttpStatus.NOT_FOUND) {
			throw rse;
		}
		throw new ResponseStatusException(
				HttpStatus.SERVICE_UNAVAILABLE,
				"El servicio de catálogo no se encuentra disponible temporalmente (CircuitBreaker abierto o timeout)"
		);
	}

	public ProductoResponse fallbackUpdateStock(Long id, Integer nuevoStock, Throwable ex) {
		log.warn("FALLBACK activado para updateStock({}, {}). Causa: {}", id, nuevoStock, ex.getMessage());
		throw new ResponseStatusException(
				HttpStatus.SERVICE_UNAVAILABLE,
				"No se pudo sincronizar el stock: Catalog-service no responde o circuito abierto"
		);
	}

	public List<ProductoResponse> fallbackGetAllProductos(Throwable ex) {
		log.warn("FALLBACK activado para getAllProductos(). Devolviendo lista vacía controlada. Causa: {}", ex.getMessage());
		return Collections.emptyList();
	}

	public List<Object> fallbackGetAllCategorias(Throwable ex) {
		log.warn("FALLBACK activado para getAllCategorias(). Devolviendo lista vacía controlada. Causa: {}", ex.getMessage());
		return Collections.emptyList();
	}

	public List<Object> fallbackGetAllProveedores(Throwable ex) {
		log.warn("FALLBACK activado para getAllProveedores(). Devolviendo lista vacía controlada. Causa: {}", ex.getMessage());
		return Collections.emptyList();
	}
}
