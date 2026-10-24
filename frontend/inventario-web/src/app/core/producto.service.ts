import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from './config';
import { Producto, ProductoRequest } from './models';

@Injectable({ providedIn: 'root' })
export class ProductoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/productos`;

  listar(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.url); // GET
  }

  crear(producto: ProductoRequest): Observable<Producto> {
    return this.http.post<Producto>(this.url, producto); // POST
  }

  actualizar(id: number, producto: ProductoRequest): Observable<Producto> {
    return this.http.put<Producto>(`${this.url}/${id}`, producto); // PUT
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`); // DELETE
  }
}
