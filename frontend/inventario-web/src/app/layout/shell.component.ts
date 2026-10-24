import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="layout">
      <aside class="sidebar">
        <div class="brand">
          <span class="brand-icon">🛒</span>
          <div>
            <strong>Bodega Don Pepe</strong>
            <small>Control de inventario</small>
          </div>
        </div>

        <nav>
          <a routerLink="/dashboard" routerLinkActive="active">📊 Panel</a>
          <a routerLink="/productos" routerLinkActive="active">📦 Productos</a>
          <a routerLink="/categorias" routerLinkActive="active">🏷️ Categorías</a>
          <a routerLink="/proveedores" routerLinkActive="active">🚚 Proveedores</a>
          <a routerLink="/movimientos" routerLinkActive="active">🔄 Movimientos</a>
        </nav>

        <div class="user-box">
          <div class="avatar">{{ inicial() }}</div>
          <div class="user-info">
            <strong>{{ auth.session()?.nombre }}</strong>
            <small>{{ auth.session()?.role }}</small>
          </div>
          <button class="btn btn-ghost btn-sm" (click)="auth.logout()" title="Cerrar sesión">⏻</button>
        </div>
      </aside>

      <main class="content">
        <router-outlet />
      </main>
    </div>
  `,
})
export class ShellComponent {
  protected readonly auth = inject(AuthService);

  protected inicial(): string {
    return (this.auth.session()?.nombre ?? '?').charAt(0).toUpperCase();
  }
}
