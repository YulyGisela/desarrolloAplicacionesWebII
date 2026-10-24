package com.cibertec.catalogservice.rest;

import com.cibertec.common.dto.ProveedorRequest;
import com.cibertec.common.dto.ProveedorResponse;
import com.cibertec.catalogservice.negocio.ProveedorService;
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
public class ProveedorController {

	private final ProveedorService proveedorService;

	public ProveedorController(ProveedorService proveedorService) {
		this.proveedorService = proveedorService;
	}

	@GetMapping("/proveedores")
	public ResponseEntity<List<ProveedorResponse>> getAllProveedores() {
		return ResponseEntity.ok(proveedorService.getAllProveedores());
	}

	@GetMapping("/proveedores/{id}")
	public ResponseEntity<ProveedorResponse> getProveedorById(@PathVariable Long id) {
		return ResponseEntity.ok(proveedorService.getProveedorById(id));
	}

	@PostMapping("/proveedores")
	public ResponseEntity<ProveedorResponse> createProveedor(@Valid @RequestBody ProveedorRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(proveedorService.createProveedor(request));
	}

	@PutMapping("/proveedores/{id}")
	public ResponseEntity<ProveedorResponse> updateProveedor(@PathVariable Long id, @Valid @RequestBody ProveedorRequest request) {
		return ResponseEntity.ok(proveedorService.updateProveedor(id, request));
	}

	@DeleteMapping("/proveedores/{id}")
	public ResponseEntity<Void> deleteProveedor(@PathVariable Long id) {
		proveedorService.deleteProveedor(id);
		return ResponseEntity.noContent().build();
	}
}

