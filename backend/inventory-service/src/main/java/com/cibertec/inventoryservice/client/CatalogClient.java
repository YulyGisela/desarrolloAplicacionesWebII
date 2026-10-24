package com.cibertec.inventoryservice.client;

import com.cibertec.common.dto.ProductoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "catalog-service", url = "${catalog-service.url:http://localhost:8082}")
public interface CatalogClient {

	@GetMapping("/productos")
	List<ProductoResponse> getAllProductos();

	@GetMapping("/productos/{id}")
	ProductoResponse getProductoById(@PathVariable("id") Long id);

	@PutMapping("/productos/{id}/stock")
	ProductoResponse updateStock(@PathVariable("id") Long id, @RequestBody Integer nuevoStock);

	@GetMapping("/categorias")
	List<Object> getAllCategorias();

	@GetMapping("/proveedores")
	List<Object> getAllProveedores();
}
