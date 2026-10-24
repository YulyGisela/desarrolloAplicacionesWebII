package com.cibertec.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAlertEvent implements Serializable {

	public static final String EXCHANGE_ALERTS = "bodegapro.alerts.exchange";
	public static final String ROUTING_KEY_STOCK = "bodegapro.alerts.stock";
	public static final String QUEUE_STOCK_ALERT = "bodegapro.stock.alert.queue";
	public static final String QUEUE_NOTIFICATION_ALERT = "bodegapro.notification.alert.queue";

	private String tipoAlerta; // "STOCK_BAJO" o "CADUCIDAD_INMINENTE"
	private Long productoId;
	private String productoCodigo;
	private String productoNombre;
	private Integer stockActual;
	private Integer stockMinimo;
	private String mensaje;
	private LocalDateTime fechaEvento;
}
