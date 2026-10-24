package com.cibertec.inventoryservice.rest;

import com.cibertec.common.dto.DashboardResponse;
import com.cibertec.inventoryservice.negocio.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping("/dashboard")
	public ResponseEntity<DashboardResponse> getDashboard() {
		return ResponseEntity.ok(dashboardService.getDashboard());
	}
}

