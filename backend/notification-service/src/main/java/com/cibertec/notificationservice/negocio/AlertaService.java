package com.cibertec.notificationservice.negocio;

import com.cibertec.common.event.StockAlertEvent;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

// Guarda en memoria las ultimas alertas de stock bajo (las 50 mas recientes).
@Service
public class AlertaService {

	private static final Logger log = LoggerFactory.getLogger(AlertaService.class);
	private static final int MAXIMO_ALERTAS = 50;

	private final Deque<Alerta> alertas = new ArrayDeque<>();
	private final MeterRegistry meterRegistry;

	public AlertaService(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	public synchronized void procesar(StockAlertEvent evento) {
		meterRegistry.counter("bodegapro.eventos.recibidos").increment();
		boolean stockBajo = evento.getStockActual() != null
				&& evento.getStockMinimo() != null
				&& evento.getStockActual() <= evento.getStockMinimo();

		if (!stockBajo) {
			log.info("Evento del producto {} procesado sin alertas", evento.getProductoCodigo());
			return;
		}

		String mensaje = (evento.getMensaje() != null && !evento.getMensaje().isBlank())
				? evento.getMensaje()
				: "Stock bajo en " + evento.getProductoNombre() + ": quedan " + evento.getStockActual()
						+ " unidades (minimo " + evento.getStockMinimo() + ")";

		Alerta alerta = new Alerta(
				evento.getProductoId(),
				evento.getProductoCodigo(),
				evento.getProductoNombre(),
				evento.getStockActual(),
				evento.getStockMinimo(),
				mensaje,
				evento.getFechaEvento() != null ? evento.getFechaEvento() : LocalDateTime.now()
		);
		alertas.addFirst(alerta);
		while (alertas.size() > MAXIMO_ALERTAS) {
			alertas.removeLast();
		}
		meterRegistry.counter("bodegapro.alertas.stock_bajo").increment();
		log.warn("ALERTA: {}", alerta.mensaje());
	}

	public synchronized List<Alerta> listar() {
		return List.copyOf(alertas);
	}
}
