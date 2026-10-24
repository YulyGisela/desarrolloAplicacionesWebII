package com.cibertec.inventoryservice.negocio;

import com.cibertec.common.event.StockAlertEvent;
import com.cibertec.inventoryservice.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class StockAlertConsumerService {

	private static final Logger log = LoggerFactory.getLogger(StockAlertConsumerService.class);
	private final List<StockAlertEvent> alertasRecibidas = Collections.synchronizedList(new ArrayList<>());

	@RabbitListener(queues = RabbitMQConfig.QUEUE_STOCK_ALERT)
	public void handleStockAlert(StockAlertEvent event) {
		log.warn("=== [NOTIFICACION ASINCRONA RECIBIDA] ===");
		log.warn("Tipo: {}", event.getTipoAlerta());
		log.warn("Producto: {} ({})", event.getProductoNombre(), event.getProductoCodigo());
		log.warn("Stock Actual: {} | Stock Minimo: {}", event.getStockActual(), event.getStockMinimo());
		log.warn("Mensaje: {}", event.getMensaje());
		log.warn("=========================================");

		alertasRecibidas.add(event);
	}

	public List<StockAlertEvent> getAlertasRecibidas() {
		return Collections.unmodifiableList(alertasRecibidas);
	}
}
