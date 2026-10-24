import { HttpErrorResponse } from '@angular/common/http';

/** Convierte un error HTTP del backend en un mensaje legible para el usuario. */
export function errorMessage(err: unknown): string {
  const e = err as HttpErrorResponse;
  if (e?.status === 0) {
    return 'No se pudo conectar con el servidor. Verifica que el backend esté encendido.';
  }
  const body = e?.error;
  if (body?.errors?.length) {
    return body.errors.join(' · ');
  }
  if (body?.message) {
    return body.message;
  }
  return 'Ocurrió un error inesperado.';
}
