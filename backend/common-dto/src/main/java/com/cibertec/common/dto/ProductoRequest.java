package com.cibertec.common.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductoRequest(
		@NotBlank(message = "El cÃ³digo es obligatorio")
		@Size(max = 30, message = "El cÃ³digo admite mÃ¡ximo 30 caracteres") String codigo,
		@NotBlank(message = "El nombre es obligatorio")
		@Size(max = 120, message = "El nombre admite mÃ¡ximo 120 caracteres") String nombre,
		@Size(max = 255, message = "La descripciÃ³n admite mÃ¡ximo 255 caracteres") String descripcion,
		@NotNull(message = "El precio de compra es obligatorio")
		@DecimalMin(value = "0.00", message = "El precio de compra no puede ser negativo") BigDecimal precioCompra,
		@NotNull(message = "El precio de venta es obligatorio")
		@DecimalMin(value = "0.00", message = "El precio de venta no puede ser negativo") BigDecimal precioVenta,
		@Min(value = 0, message = "El stock inicial no puede ser negativo") Integer stockInicial,
		@NotNull(message = "El stock mÃ­nimo es obligatorio")
		@Min(value = 0, message = "El stock mÃ­nimo no puede ser negativo") Integer stockMinimo,
		LocalDate fechaVencimiento,
		@NotNull(message = "La categorÃ­a es obligatoria") Long categoriaId,
		Long proveedorId
) {
}

