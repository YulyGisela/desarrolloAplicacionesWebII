package com.cibertec.catalogservice.rest;

import com.cibertec.common.dto.ProductoRequest;
import com.cibertec.common.dto.ProductoResponse;
import com.cibertec.catalogservice.negocio.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductoController {

	private final ProductoService productoService;

	public ProductoController(ProductoService productoService) {
		this.productoService = productoService;
	}

	@GetMapping("/productos")
	public ResponseEntity<List<ProductoResponse>> getAllProductos() {
		return ResponseEntity.ok(productoService.getAllProductos());
	}

	@GetMapping("/productos/{id}")
	public ResponseEntity<ProductoResponse> getProductoById(@PathVariable Long id) {
		return ResponseEntity.ok(productoService.getProductoById(id));
	}

	@PostMapping("/productos")
	public ResponseEntity<ProductoResponse> createProducto(@Valid @RequestBody ProductoRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(productoService.createProducto(request));
	}

	@PutMapping("/productos/{id}")
	public ResponseEntity<ProductoResponse> updateProducto(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
		return ResponseEntity.ok(productoService.updateProducto(id, request));
	}

	@DeleteMapping("/productos/{id}")
	public ResponseEntity<Void> deleteProducto(@PathVariable Long id) {
		productoService.deleteProducto(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/productos/{id}/stock")
	public ResponseEntity<ProductoResponse> updateStock(@PathVariable Long id, @RequestBody Integer nuevoStock) {
		return ResponseEntity.ok(productoService.updateStock(id, nuevoStock));
	}
}


