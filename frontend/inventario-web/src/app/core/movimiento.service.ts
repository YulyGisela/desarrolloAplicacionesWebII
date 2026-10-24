import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from './config';
import { Dashboard, Movimiento, MovimientoRequest, TipoMovimiento } from './models';

@Injectable({ providedIn: 'root' })
export class MovimientoService {
  private readonly http = inject(HttpClient);

  listar(): Observable<Movimiento[]> {
    return this.http.get<Movimiento[]>(`${API_URL}/movimientos`);
  }

  registrar(request: MovimientoRequest): Observable<Movimiento> {
    return this.http.post<Movimiento>(`${API_URL}/movimientos`, request);
  }

  dashboard(): Observable<Dashboard> {
    return this.http.get<Dashboard>(`${API_URL}/dashboard`);
  }

  descargarReporte(
    formato: 'excel' | 'pdf',
    desde: string,
    hasta: string,
    tipo: TipoMovimiento | '',
  ): Observable<Blob> {
    let params = new HttpParams();
    if (desde) params = params.set('desde', desde);
    if (hasta) params = params.set('hasta', hasta);
    if (tipo) params = params.set('tipo', tipo);
    return this.http.get(`${API_URL}/reportes/movimientos/${formato}`, {
      params,
      responseType: 'blob',
    });
  }
}
