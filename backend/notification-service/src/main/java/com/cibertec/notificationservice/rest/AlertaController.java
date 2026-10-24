package com.cibertec.notificationservice.rest;

import com.cibertec.notificationservice.negocio.Alerta;
import com.cibertec.notificationservice.negocio.AlertaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AlertaController {

	private final AlertaService alertaService;

	public AlertaController(AlertaService alertaService) {
		this.alertaService = alertaService;
	}

	@GetMapping({"/alertas", "/notificaciones"})
	public ResponseEntity<List<Alerta>> listar() {
		return ResponseEntity.ok(alertaService.listar());
	}
}
