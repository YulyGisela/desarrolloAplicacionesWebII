package com.cibertec.inventoryservice.rest;

import com.cibertec.common.dto.MovimientoRequest;
import com.cibertec.common.dto.MovimientoResponse;
import com.cibertec.inventoryservice.negocio.MovimientoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MovimientoController {

	private final MovimientoService movimientoService;

	public MovimientoController(MovimientoService movimientoService) {
		this.movimientoService = movimientoService;
	}

	@GetMapping("/movimientos")
	public ResponseEntity<List<MovimientoResponse>> getAllMovimientos() {
		return ResponseEntity.ok(movimientoService.getAllMovimientos());
	}

	@PostMapping("/movimientos")
	public ResponseEntity<MovimientoResponse> registrarMovimiento(
			@Valid @RequestBody MovimientoRequest request,
			@org.springframework.web.bind.annotation.RequestHeader(value = "X-User", defaultValue = "admin") String username
	) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(movimientoService.registrarMovimiento(request, username));
	}
}

