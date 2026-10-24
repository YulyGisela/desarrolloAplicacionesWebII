package com.cibertec.catalogservice.rest;

import com.cibertec.common.dto.CategoriaRequest;
import com.cibertec.common.dto.CategoriaResponse;
import com.cibertec.catalogservice.negocio.CategoriaService;
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
public class CategoriaController {

	private final CategoriaService categoriaService;

	public CategoriaController(CategoriaService categoriaService) {
		this.categoriaService = categoriaService;
	}

	@GetMapping("/categorias")
	public ResponseEntity<List<CategoriaResponse>> getAllCategorias() {
		return ResponseEntity.ok(categoriaService.getAllCategorias());
	}

	@GetMapping("/categorias/{id}")
	public ResponseEntity<CategoriaResponse> getCategoriaById(@PathVariable Long id) {
		return ResponseEntity.ok(categoriaService.getCategoriaById(id));
	}

	@PostMapping("/categorias")
	public ResponseEntity<CategoriaResponse> createCategoria(@Valid @RequestBody CategoriaRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.createCategoria(request));
	}

	@PutMapping("/categorias/{id}")
	public ResponseEntity<CategoriaResponse> updateCategoria(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
		return ResponseEntity.ok(categoriaService.updateCategoria(id, request));
	}

	@DeleteMapping("/categorias/{id}")
	public ResponseEntity<Void> deleteCategoria(@PathVariable Long id) {
		categoriaService.deleteCategoria(id);
		return ResponseEntity.noContent().build();
	}
}

