import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { errorMessage } from '../core/http-error';
import { Dashboard } from '../core/models';
import { MovimientoService } from '../core/movimiento.service';
import { ToastService } from '../core/toast.service';

@Component({
  selector: 'app-dashboard',
  imports: [CurrencyPipe, DatePipe],
  template: `
    <header class="page-header">
      <div>
        <h2>Panel de control</h2>
        <p class="muted">Resumen general de tu bodega</p>
      </div>
    </header>

    @if (data(); as d) {
      <section class="cards">
        <div class="stat">
          <span class="stat-icon">📦</span>
          <div><strong>{{ d.totalProductos }}</strong><small>Productos</small></div>
        </div>
        <div class="stat">
          <span class="stat-icon">🏷️</span>
          <div><strong>{{ d.totalCategorias }}</strong><small>Categorías</small></div>
        </div>
        <div class="stat">
          <span class="stat-icon">🚚</span>
          <div><strong>{{ d.totalProveedores }}</strong><small>Proveedores</small></div>
        </div>
        <div class="stat stat-warn">
          <span class="stat-icon">⚠️</span>
          <div><strong>{{ d.productosStockBajo }}</strong><small>Con stock bajo</small></div>
        </div>
        <div class="stat stat-danger">
          <span class="stat-icon">⏳</span>
          <div><strong>{{ d.productosPorVencer }}</strong><small>Por vencer (30 días)</small></div>
        </div>
        <div class="stat stat-ok">
          <span class="stat-icon">💰</span>
          <div>
            <strong>{{ d.valorInventario | currency: 'PEN' : 'S/ ' }}</strong>
            <small>Valor del inventario</small>
          </div>
        </div>
      </section>

      <section class="grid-2">
        <div class="panel">
          <h3>Productos con stock bajo</h3>
          @if (d.stockBajo.length === 0) {
            <p class="empty">Todo en orden, no hay productos con stock bajo 🎉</p>
          } @else {
            <div class="table-wrap">
              <table>
                <thead><tr><th>Producto</th><th>Stock</th><th>Mínimo</th></tr></thead>
                <tbody>
                  @for (p of d.stockBajo; track p.id) {
                    <tr>
                      <td>{{ p.nombre }}</td>
                      <td><span class="badge badge-danger">{{ p.stockActual }}</span></td>
                      <td>{{ p.stockMinimo }}</td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </div>

        <div class="panel">
          <h3>Próximos a vencer</h3>
          @if (d.porVencer.length === 0) {
            <p class="empty">No hay productos próximos a vencer ✅</p>
          } @else {
            <div class="table-wrap">
              <table>
                <thead><tr><th>Producto</th><th>Stock</th><th>Vence</th></tr></thead>
                <tbody>
                  @for (p of d.porVencer; track p.id) {
                    <tr>
                      <td>{{ p.nombre }}</td>
                      <td>{{ p.stockActual }}</td>
                      <td><span class="badge badge-warn">{{ p.fechaVencimiento | date: 'dd/MM/yyyy' }}</span></td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </div>
      </section>
    } @else {
      <p class="muted">Cargando información...</p>
    }
  `,
})
export class DashboardComponent {
  private readonly service = inject(MovimientoService);
  private readonly toast = inject(ToastService);

  protected readonly data = signal<Dashboard | null>(null);

  constructor() {
    this.service.dashboard().subscribe({
      next: (d) => this.data.set(d),
      error: (err) => this.toast.error(errorMessage(err)),
    });
  }
}
