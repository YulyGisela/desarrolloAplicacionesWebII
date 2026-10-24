package com.cibertec.notificationservice.negocio;

import com.cibertec.common.event.StockAlertEvent;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertaServiceTest {

	private AlertaService alertaService;
	private SimpleMeterRegistry registry;

	@BeforeEach
	void setUp() {
		registry = new SimpleMeterRegistry();
		alertaService = new AlertaService(registry);
	}

	private StockAlertEvent evento(long id, int stock, int minimo) {
		return StockAlertEvent.builder()
				.tipoAlerta("STOCK_BAJO")
				.productoId(id)
				.productoCodigo("ABR-002")
				.productoNombre("Azúcar rubia 1 kg")
				.stockActual(stock)
				.stockMinimo(minimo)
				.mensaje("Stock bajo en Azúcar rubia 1 kg: quedan " + stock + " unidades (minimo " + minimo + ")")
				.fechaEvento(LocalDateTime.now())
				.build();
	}

	@Test
	@DisplayName("Un evento con stock bajo genera una alerta")
	void generaAlerta() {
		alertaService.procesar(evento(1L, 5, 15));

		assertEquals(1, alertaService.listar().size());
		assertTrue(alertaService.listar().get(0).mensaje().contains("Stock bajo"));
		assertEquals(1.0, registry.counter("bodegapro.alertas.stock_bajo").count());
	}

	@Test
	@DisplayName("Un evento con stock normal no genera alertas")
	void sinAlerta() {
		alertaService.procesar(evento(2L, 40, 15));

		assertTrue(alertaService.listar().isEmpty());
		assertEquals(1.0, registry.counter("bodegapro.eventos.recibidos").count());
	}

	@Test
	@DisplayName("Solo se conservan las 50 alertas mas recientes")
	void limitaLaCantidad() {
		for (int i = 0; i < 60; i++) {
			alertaService.procesar(evento(i, 1, 15));
		}

		assertEquals(50, alertaService.listar().size());
	}
}
