package com.cibertec.catalogservice.negocio;

import com.cibertec.catalogservice.config.RabbitMQConfig;
import com.cibertec.common.event.StockAlertEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlertProducerService {

	private static final Logger log = LoggerFactory.getLogger(AlertProducerService.class);
	private final RabbitTemplate rabbitTemplate;

	public AlertProducerService(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public void emitStockAlert(StockAlertEvent event) {
		try {
			rabbitTemplate.convertAndSend(
					RabbitMQConfig.EXCHANGE_ALERTS,
					RabbitMQConfig.ROUTING_KEY_STOCK,
					event
			);
			log.info("Evento de alerta emitido correctamente: [{}] para producto: {}", event.getTipoAlerta(), event.getProductoCodigo());
		} catch (Exception e) {
			log.warn("No se pudo enviar evento a RabbitMQ (posiblemente broker no conectado en test): {}", e.getMessage());
		}
	}
}
