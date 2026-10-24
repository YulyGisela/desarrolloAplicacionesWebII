package com.cibertec.inventoryservice.negocio;

import com.cibertec.common.dto.MovimientoRequest;
import com.cibertec.common.dto.MovimientoResponse;
import com.cibertec.common.dto.ProductoResponse;
import com.cibertec.common.dto.TipoMovimiento;
import com.cibertec.inventoryservice.client.CatalogServiceProxy;
import com.cibertec.inventoryservice.entidades.MovimientoStock;
import com.cibertec.inventoryservice.repositorio.MovimientoStockRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovimientoService {

	private final MovimientoStockRepository movimientoStockRepository;
	private final CatalogServiceProxy catalogServiceProxy;

	public MovimientoService(
			MovimientoStockRepository movimientoStockRepository,
			CatalogServiceProxy catalogServiceProxy
	) {
		this.movimientoStockRepository = movimientoStockRepository;
		this.catalogServiceProxy = catalogServiceProxy;
	}

	@Transactional(readOnly = true)
	public List<MovimientoResponse> getAllMovimientos() {
		return movimientoStockRepository.findAllByOrderByFechaDesc().stream()
				.map(this::mapToResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<MovimientoResponse> getMovimientosParaReporte(
			LocalDate desde, LocalDate hasta, TipoMovimiento tipo) {
		LocalDateTime fechaDesde = desde == null ? null : desde.atStartOfDay();
		LocalDateTime fechaHasta = hasta == null ? null : hasta.plusDays(1).atStartOfDay();
		return movimientoStockRepository.findAllParaReporte(fechaDesde, fechaHasta, tipo).stream()
				.map(this::mapToResponse)
				.toList();
	}

	@Transactional
	public MovimientoResponse registrarMovimiento(MovimientoRequest request, String username) {
		ProductoResponse producto = catalogServiceProxy.getProductoById(request.productoId());

		int stockActual = producto.stockActual();
		int nuevoStock = request.tipo() == TipoMovimiento.ENTRADA
				? stockActual + request.cantidad()
				: stockActual - request.cantidad();

		if (nuevoStock < 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Stock insuficiente. Disponible: " + stockActual + ", solicitado: " + request.cantidad());
		}

		catalogServiceProxy.updateStock(request.productoId(), nuevoStock);

		MovimientoStock movimiento = MovimientoStock.builder()
				.productoId(request.productoId())
				.tipo(request.tipo())
				.cantidad(request.cantidad())
				.motivo(request.motivo() == null || request.motivo().isBlank() ? null : request.motivo().trim())
				.fecha(LocalDateTime.now())
				.usuario(username)
				.build();

		MovimientoStock guardado = movimientoStockRepository.save(movimiento);
		return mapToResponse(guardado, producto.codigo(), producto.nombre());
	}

	private MovimientoResponse mapToResponse(MovimientoStock m) {
		String codigo = "PROD-" + m.getProductoId();
		String nombre = "Producto #" + m.getProductoId();
		try {
			ProductoResponse p = catalogServiceProxy.getProductoById(m.getProductoId());
			if (p != null) {
				codigo = p.codigo();
				nombre = p.nombre();
			}
		} catch (Exception ignored) {
		}
		return mapToResponse(m, codigo, nombre);
	}

	private MovimientoResponse mapToResponse(MovimientoStock m, String codigo, String nombre) {
		return new MovimientoResponse(
				m.getId(),
				m.getProductoId(),
				codigo,
				nombre,
				m.getTipo(),
				m.getCantidad(),
				m.getMotivo(),
				m.getFecha(),
				m.getUsuario()
		);
	}
}
