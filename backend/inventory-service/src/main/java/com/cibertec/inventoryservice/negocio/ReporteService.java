package com.cibertec.inventoryservice.negocio;

import com.cibertec.common.dto.MovimientoResponse;
import com.cibertec.common.dto.TipoMovimiento;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReporteService {

	private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	private static final String[] COLUMNAS = {
			"Fecha", "CÃ³digo", "Producto", "Tipo", "Cantidad", "Motivo", "Usuario"
	};
	private static final float[] ANCHOS_PDF = {85, 65, 155, 55, 55, 250, 90};
	private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");
	private static final PDType1Font FONT_NORMAL = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
	private static final PDType1Font FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

	public byte[] generarExcel(
			List<MovimientoResponse> movimientos, LocalDate desde, LocalDate hasta, TipoMovimiento tipo)
			throws IOException {
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			Sheet sheet = workbook.createSheet("Movimientos");
			Row title = sheet.createRow(0);
			title.createCell(0).setCellValue("Reporte de movimientos de stock");
			Row filtros = sheet.createRow(1);
			filtros.createCell(0).setCellValue("Desde: " + fechaFiltro(desde));
			filtros.createCell(1).setCellValue("Hasta: " + fechaFiltro(hasta));
			filtros.createCell(2).setCellValue("Tipo: " + (tipo == null ? "Todos" : tipo.name()));

			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setColor(IndexedColors.WHITE.getIndex());
			headerStyle.setFont(headerFont);

			Row header = sheet.createRow(3);
			for (int i = 0; i < COLUMNAS.length; i++) {
				Cell cell = header.createCell(i);
				cell.setCellValue(COLUMNAS[i]);
				cell.setCellStyle(headerStyle);
			}

			int rowIndex = 4;
			for (MovimientoResponse movimiento : movimientos) {
				Row row = sheet.createRow(rowIndex++);
				row.createCell(0).setCellValue(movimiento.fecha() == null ? "" : FECHA_HORA.format(movimiento.fecha()));
				row.createCell(1).setCellValue(safeString(movimiento.productoCodigo()));
				row.createCell(2).setCellValue(safeString(movimiento.productoNombre()));
				row.createCell(3).setCellValue(movimiento.tipo() == null ? "" : movimiento.tipo().name());
				row.createCell(4).setCellValue(movimiento.cantidad());
				row.createCell(5).setCellValue(safeString(movimiento.motivo()));
				row.createCell(6).setCellValue(safeString(movimiento.usuario()));
			}
			for (int i = 0; i < COLUMNAS.length; i++) {
				sheet.autoSizeColumn(i);
			}
			workbook.write(output);
			return output.toByteArray();
		}
	}

	public byte[] generarPdf(
			List<MovimientoResponse> movimientos, LocalDate desde, LocalDate hasta, TipoMovimiento tipo)
			throws IOException {
		try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			PDRectangle pageSize = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
			PDPageContentStream stream = null;
			float y = 0;
			try {
				for (int index = 0; index < movimientos.size(); index++) {
					if (stream == null || y < 45) {
						if (stream != null) {
							stream.close();
						}
						PDPage page = new PDPage(pageSize);
						document.addPage(page);
						stream = new PDPageContentStream(document, page);
						y = escribirEncabezado(stream, pageSize, desde, hasta, tipo);
					}
					MovimientoResponse movimiento = movimientos.get(index);
					String[] valores = {
							movimiento.fecha() == null ? "" : FECHA_HORA.format(movimiento.fecha()),
							safeString(movimiento.productoCodigo()),
							safeString(movimiento.productoNombre()),
							movimiento.tipo() == null ? "" : movimiento.tipo().name(),
							String.valueOf(movimiento.cantidad()),
							safeString(movimiento.motivo()),
							safeString(movimiento.usuario())
					};
					escribirFila(stream, y, valores);
					y -= 15;
				}
				if (stream == null) {
					PDPage page = new PDPage(pageSize);
					document.addPage(page);
					stream = new PDPageContentStream(document, page);
					escribirEncabezado(stream, pageSize, desde, hasta, tipo);
					escribirTexto(stream, "Sin movimientos para los filtros seleccionados", 30, 490,
							FONT_NORMAL, 9);
				}
			} finally {
				if (stream != null) {
					stream.close();
				}
			}
			document.save(output);
			return output.toByteArray();
		}
	}

	private float escribirEncabezado(
			PDPageContentStream stream, PDRectangle pageSize, LocalDate desde, LocalDate hasta,
			TipoMovimiento tipo) throws IOException {
		escribirTexto(stream, "Reporte de movimientos de stock", 30, pageSize.getHeight() - 35,
				FONT_BOLD, 14);
		escribirTexto(stream, "Desde: " + fechaFiltro(desde) + "    Hasta: " + fechaFiltro(hasta)
						+ "    Tipo: " + (tipo == null ? "Todos" : tipo.name()),
				30, pageSize.getHeight() - 55, FONT_NORMAL, 9);
		escribirFila(stream, pageSize.getHeight() - 80, COLUMNAS);
		return pageSize.getHeight() - 95;
	}

	private void escribirFila(PDPageContentStream stream, float y, String[] valores) throws IOException {
		float x = 30;
		for (int i = 0; i < valores.length; i++) {
			escribirTexto(stream, truncar(valores[i], ANCHOS_PDF[i]), x, y,
					y > 500 ? FONT_BOLD : FONT_NORMAL, 8);
			x += ANCHOS_PDF[i];
		}
	}

	private void escribirTexto(
			PDPageContentStream stream, String texto, float x, float y,
			PDType1Font font, float fontSize) throws IOException {
		stream.beginText();
		stream.setFont(font, fontSize);
		stream.newLineAtOffset(x, y);
		stream.showText(textoPdf(texto));
		stream.endText();
	}

	private String textoPdf(String texto) throws IOException {
		String textoSeguro = safeString(texto).replaceAll("[\\p{Cntrl}]", " ");
		return WINDOWS_1252.decode(WINDOWS_1252.newEncoder()
				.onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE)
				.replaceWith(new byte[]{'?'})
				.encode(java.nio.CharBuffer.wrap(textoSeguro))).toString();
	}

	private String safeString(String valor) {
		return valor == null ? "" : valor.replace("\u0000", " ").trim();
	}

	private String truncar(String valor, float ancho) {
		String texto = valor == null ? "" : valor;
		int maxCaracteres = Math.max(1, (int) (ancho / 4.2f));
		return texto.length() <= maxCaracteres ? texto : texto.substring(0, maxCaracteres - 1) + "...";
	}

	private String fechaFiltro(LocalDate fecha) {
		return fecha == null ? "Todas" : fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	}
}

