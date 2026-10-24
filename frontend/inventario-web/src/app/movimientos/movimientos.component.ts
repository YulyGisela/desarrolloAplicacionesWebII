import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { errorMessage } from '../core/http-error';
import { Movimiento, Producto, TipoMovimiento } from '../core/models';
import { MovimientoService } from '../core/movimiento.service';
import { ProductoService } from '../core/producto.service';
import { ToastService } from '../core/toast.service';

@Component({
  selector: 'app-movimientos',
  imports: [ReactiveFormsModule, DatePipe],
  template: `
    <header class="page-header">
      <div>
        <h2>Movimientos de stock</h2>
        <p class="muted">Registra entradas (compras) y salidas (ventas, mermas) de mercadería</p>
      </div>
    </header>

    <div class="grid-form">
      <div class="panel">
        <h3>Registrar movimiento</h3>
        <form [formGroup]="form" (ngSubmit)="registrar()">
          <label>
            Producto
            <select formControlName="productoId" [class.invalid]="invalid('productoId')">
              <option [ngValue]="null">Seleccione...</option>
              @for (p of productos(); track p.id) {
                <option [ngValue]="p.id">{{ p.codigo }} · {{ p.nombre }} (stock {{ p.stockActual }})</option>
              }
            </select>
            @if (invalid('productoId')) { <span class="field-error">Seleccione un producto</span> }
          </label>

          <label>
            Tipo de movimiento
            <select formControlName="tipo">
              <option value="ENTRADA">⬆️ Entrada (ingreso de mercadería)</option>
              <option value="SALIDA">⬇️ Salida (venta / merma)</option>
            </select>
          </label>

          <label>
            Cantidad
            <input type="number" min="1" formControlName="cantidad" [class.invalid]="invalid('cantidad')" />
            @if (invalid('cantidad')) { <span class="field-error">La cantidad debe ser al menos 1</span> }
          </label>

          <label>
            Motivo
            <input type="text" formControlName="motivo" placeholder="Ej. Compra a proveedor, venta del día..." />
          </label>

          <button class="btn btn-primary btn-block" type="submit" [disabled]="saving()">
            {{ saving() ? 'Registrando...' : 'Registrar movimiento' }}
          </button>
        </form>
      </div>

      <div class="panel">
        <div class="toolbar">
          <h3>Historial (kardex)</h3>
          <select class="select-inline" (change)="cambiarFiltroTipo($any($event.target).value)">
            <option value="">Todos</option>
            <option value="ENTRADA">Solo entradas</option>
            <option value="SALIDA">Solo salidas</option>
          </select>
        </div>

        <div class="report-filters">
          <label class="report-date">
            Desde
            <input type="date" [value]="filtroDesde()" (input)="filtroDesde.set($any($event.target).value)" />
          </label>
          <label class="report-date">
            Hasta
            <input type="date" [value]="filtroHasta()" (input)="filtroHasta.set($any($event.target).value)" />
          </label>
          <div class="report-actions">
            <button
              class="btn btn-ghost"
              type="button"
              (click)="descargar('excel')"
              [disabled]="!!exportando() || rangoInvalido()"
            >
              {{ exportando() === 'excel' ? 'Generando...' : 'Descargar Excel' }}
            </button>
            <button
              class="btn btn-ghost"
              type="button"
              (click)="descargar('pdf')"
              [disabled]="!!exportando() || rangoInvalido()"
            >
              {{ exportando() === 'pdf' ? 'Generando...' : 'Descargar PDF' }}
            </button>
          </div>
        </div>
        @if (rangoInvalido()) {
          <p class="field-error">La fecha inicial no puede ser posterior a la fecha final.</p>
        }

        <div class="table-wrap">
          <table>
            <thead>
              <tr><th>Fecha</th><th>Producto</th><th>Tipo</th><th>Cant.</th><th>Motivo</th><th>Usuario</th></tr>
            </thead>
            <tbody>
              @for (m of filtrados(); track m.id) {
                <tr>
                  <td>{{ m.fecha | date: 'dd/MM/yyyy HH:mm' }}</td>
                  <td><strong>{{ m.productoNombre }}</strong><br /><small class="muted">{{ m.productoCodigo }}</small></td>
                  <td>
                    <span class="badge" [class.badge-ok]="m.tipo === 'ENTRADA'" [class.badge-danger]="m.tipo === 'SALIDA'">
                      {{ m.tipo }}
                    </span>
                  </td>
                  <td>{{ m.cantidad }}</td>
                  <td>{{ m.motivo || '—' }}</td>
                  <td>{{ m.usuario }}</td>
                </tr>
              } @empty {
                <tr><td colspan="6" class="empty">No hay movimientos para los filtros seleccionados</td></tr>
              }
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
})
export class MovimientosComponent {
  private readonly fb = inject(FormBuilder);
  private readonly movimientoService = inject(MovimientoService);
  private readonly productoService = inject(ProductoService);
  private readonly toast = inject(ToastService);

  protected readonly productos = signal<Producto[]>([]);
  protected readonly movimientos = signal<Movimiento[]>([]);
  protected readonly filtroTipo = signal<TipoMovimiento | ''>('');
  protected readonly saving = signal(false);
  protected readonly filtroDesde = signal('');
  protected readonly filtroHasta = signal('');
  protected readonly exportando = signal<'excel' | 'pdf' | null>(null);

  protected readonly rangoInvalido = computed(
    () => !!this.filtroDesde() && !!this.filtroHasta() && this.filtroDesde() > this.filtroHasta(),
  );

  protected readonly filtrados = computed(() => {
    const tipo = this.filtroTipo();
    const desde = this.filtroDesde();
    const hasta = this.filtroHasta();
    return this.movimientos().filter((m) => {
      const fecha = m.fecha.slice(0, 10);
      return (
        (!tipo || m.tipo === tipo) &&
        (!desde || fecha >= desde) &&
        (!hasta || fecha <= hasta)
      );
    });
  });

  protected readonly form = this.fb.group({
    productoId: this.fb.control<number | null>(null, Validators.required),
    tipo: this.fb.nonNullable.control<'ENTRADA' | 'SALIDA'>('ENTRADA'),
    cantidad: this.fb.nonNullable.control(1, [Validators.required, Validators.min(1)]),
    motivo: this.fb.nonNullable.control(''),
  });

  constructor() {
    this.cargar();
  }

  protected invalid(name: 'productoId' | 'cantidad'): boolean {
    const c = this.form.controls[name];
    return c.invalid && c.touched;
  }

  protected cambiarFiltroTipo(tipo: string): void {
    this.filtroTipo.set(tipo === 'ENTRADA' || tipo === 'SALIDA' ? tipo : '');
  }

  private cargar(): void {
    forkJoin({
      productos: this.productoService.listar(),
      movimientos: this.movimientoService.listar(),
    }).subscribe({
      next: (r) => {
        this.productos.set(r.productos);
        this.movimientos.set(r.movimientos);
      },
      error: (err) => this.toast.error(errorMessage(err)),
    });
  }

  protected registrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    this.saving.set(true);
    this.movimientoService
      .registrar({
        productoId: v.productoId!,
        tipo: v.tipo,
        cantidad: v.cantidad,
        motivo: v.motivo.trim() || null,
      })
      .subscribe({
        next: () => {
          this.toast.success('Movimiento registrado');
          this.saving.set(false);
          this.form.reset({ productoId: null, tipo: 'ENTRADA', cantidad: 1, motivo: '' });
          this.cargar();
        },
        error: (err) => {
          this.toast.error(errorMessage(err));
          this.saving.set(false);
        },
      });
  }

  protected descargar(formato: 'excel' | 'pdf'): void {
    if (this.rangoInvalido()) return;
    this.exportando.set(formato);
    this.movimientoService
      .descargarReporte(
        formato,
        this.filtroDesde(),
        this.filtroHasta(),
        this.filtroTipo(),
      )
      .subscribe({
        next: (archivo) => {
          const url = URL.createObjectURL(archivo);
          const enlace = document.createElement('a');
          enlace.href = url;
          const rango = this.filtroDesde() || this.filtroHasta()
            ? `${this.filtroDesde() || 'inicio'}-a-${this.filtroHasta() || 'hoy'}`
            : 'completo';
          const tipo = this.filtroTipo() ? `-${this.filtroTipo().toLowerCase()}` : '';
          enlace.download = `reporte-movimientos-${rango}${tipo}.${formato === 'excel' ? 'xlsx' : 'pdf'}`;
          enlace.click();
          window.setTimeout(() => URL.revokeObjectURL(url));
          this.exportando.set(null);
        },
        error: (err) => {
          this.toast.error(errorMessage(err));
          this.exportando.set(null);
        },
      });
  }
}
