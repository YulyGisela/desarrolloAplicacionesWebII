package com.cibertec.notificationservice.evento;

import com.cibertec.common.event.StockAlertEvent;
import com.cibertec.notificationservice.config.RabbitMQConfig;
import com.cibertec.notificationservice.negocio.AlertaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// Consumidor asincrono: se ejecuta cuando llega un evento de alerta de stock a la cola de notificaciones.
@Component
public class StockAlertListener {

	private static final Logger log = LoggerFactory.getLogger(StockAlertListener.class);

	private final AlertaService alertaService;

	public StockAlertListener(AlertaService alertaService) {
		this.alertaService = alertaService;
	}

	@RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATION_ALERT)
	public void onStockAlert(StockAlertEvent evento) {
		log.info("Evento recibido: alerta [{}] del producto {}", evento.getTipoAlerta(), evento.getProductoCodigo());
		alertaService.procesar(evento);
	}
}
