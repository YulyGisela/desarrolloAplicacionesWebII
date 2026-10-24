package com.cibertec.common.dto;

import com.cibertec.common.dto.TipoMovimiento;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MovimientoRequest(
		@NotNull(message = "El producto es obligatorio") Long productoId,
		@NotNull(message = "El tipo de movimiento es obligatorio (ENTRADA o SALIDA)") TipoMovimiento tipo,
		@NotNull(message = "La cantidad es obligatoria")
		@Min(value = 1, message = "La cantidad debe ser al menos 1") Integer cantidad,
		@Size(max = 200, message = "El motivo admite mÃ¡ximo 200 caracteres") String motivo
) {
}

