package com.cibertec.notificationservice.config;

import com.cibertec.common.event.StockAlertEvent;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	public static final String EXCHANGE_ALERTS = StockAlertEvent.EXCHANGE_ALERTS;
	public static final String QUEUE_NOTIFICATION_ALERT = StockAlertEvent.QUEUE_NOTIFICATION_ALERT;
	public static final String ROUTING_KEY_STOCK = StockAlertEvent.ROUTING_KEY_STOCK;

	@Bean
	public DirectExchange alertsExchange() {
		return new DirectExchange(EXCHANGE_ALERTS);
	}

	@Bean
	public Queue notificationAlertQueue() {
		return new Queue(QUEUE_NOTIFICATION_ALERT, true);
	}

	@Bean
	public Binding notificationAlertBinding(Queue notificationAlertQueue, DirectExchange alertsExchange) {
		return BindingBuilder.bind(notificationAlertQueue).to(alertsExchange).with(ROUTING_KEY_STOCK);
	}

	@Bean
	public MessageConverter jacksonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}
