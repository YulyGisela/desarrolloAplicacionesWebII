package com.cibertec.authservice.rest;

import com.cibertec.authservice.negocio.AuthService;
import com.cibertec.common.dto.LoginRequest;
import com.cibertec.common.dto.LoginResponse;
import com.cibertec.common.dto.RegistroRequest;
import com.cibertec.common.dto.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/auth/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@PostMapping("/auth/register")
	public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody RegistroRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
	}
}
