package com.cibertec.inventoryservice.rest;

import com.cibertec.common.dto.MovimientoResponse;
import com.cibertec.common.dto.TipoMovimiento;
import com.cibertec.inventoryservice.negocio.MovimientoService;
import com.cibertec.inventoryservice.negocio.ReporteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@RestController
public class ReporteController {

	private static final MediaType EXCEL_MEDIA_TYPE = MediaType.parseMediaType(
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

	private final MovimientoService movimientoService;
	private final ReporteService reporteService;

	public ReporteController(MovimientoService movimientoService, ReporteService reporteService) {
		this.movimientoService = movimientoService;
		this.reporteService = reporteService;
	}

	@GetMapping("/reportes/movimientos/excel")
	public ResponseEntity<byte[]> descargarExcel(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) String tipo) throws IOException {
		TipoMovimiento tipoMovimiento = parseTipo(tipo);
		validarRango(desde, hasta);
		List<MovimientoResponse> movimientos = movimientoService.getMovimientosParaReporte(desde, hasta, tipoMovimiento);
		String filename = "reporte-movimientos-" + fechaArchivo(desde, hasta) + ".xlsx";
		return ResponseEntity.ok()
				.contentType(EXCEL_MEDIA_TYPE)
				.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
				.body(reporteService.generarExcel(movimientos, desde, hasta, tipoMovimiento));
	}

	@GetMapping("/reportes/movimientos/pdf")
	public ResponseEntity<byte[]> descargarPdf(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) String tipo) throws IOException {
		TipoMovimiento tipoMovimiento = parseTipo(tipo);
		validarRango(desde, hasta);
		List<MovimientoResponse> movimientos = movimientoService.getMovimientosParaReporte(desde, hasta, tipoMovimiento);
		String filename = "reporte-movimientos-" + fechaArchivo(desde, hasta) + ".pdf";
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
				.body(reporteService.generarPdf(movimientos, desde, hasta, tipoMovimiento));
	}

	private TipoMovimiento parseTipo(String tipo) {
		if (tipo == null || tipo.isBlank()) {
			return null;
		}
		try {
			return TipoMovimiento.valueOf(tipo.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"El tipo de movimiento debe ser ENTRADA o SALIDA");
		}
	}

	private void validarRango(LocalDate desde, LocalDate hasta) {
		if (desde != null && hasta != null && desde.isAfter(hasta)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha inicial no puede ser posterior a la fecha final");
		}
	}

	private String fechaArchivo(LocalDate desde, LocalDate hasta) {
		if (desde == null && hasta == null) {
			return "completo";
		}
		if (desde == null) {
			return "hasta-" + hasta.format(FILE_DATE);
		}
		if (hasta == null) {
			return "desde-" + desde.format(FILE_DATE);
		}
		return desde.format(FILE_DATE) + "-a-" + hasta.format(FILE_DATE);
	}
}

