import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { AuthService } from '../core/auth.service';
import { CategoriaService } from '../core/categoria.service';
import { errorMessage } from '../core/http-error';
import { Categoria, Producto, ProductoRequest, Proveedor } from '../core/models';
import { ProductoService } from '../core/producto.service';
import { ProveedorService } from '../core/proveedor.service';
import { ToastService } from '../core/toast.service';

@Component({
  selector: 'app-productos',
  imports: [ReactiveFormsModule, CurrencyPipe, DatePipe],
  template: `
    <header class="page-header">
      <div>
        <h2>Productos</h2>
        <p class="muted">Catálogo de mercadería con control de stock y vencimiento</p>
      </div>
      <button class="btn btn-primary" (click)="nuevo()">＋ Nuevo producto</button>
    </header>

    <div class="panel">
      <div class="toolbar">
        <input class="search" type="search" placeholder="Buscar por código o nombre..."
               (input)="search.set($any($event.target).value)" />
        <select class="select-inline" (change)="filtroCategoria.set($any($event.target).value)">
          <option value="">Todas las categorías</option>
          @for (c of categorias(); track c.id) {
            <option [value]="c.id">{{ c.nombre }}</option>
          }
        </select>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Código</th><th>Producto</th><th>Categoría</th><th>Proveedor</th>
              <th>P. compra</th><th>P. venta</th><th>Stock</th><th>Vence</th>
              <th class="actions-col">Acciones</th>
            </tr>
          </thead>
          <tbody>
            @for (p of filtrados(); track p.id) {
              <tr>
                <td><code>{{ p.codigo }}</code></td>
                <td><strong>{{ p.nombre }}</strong></td>
                <td>{{ p.categoriaNombre }}</td>
                <td>{{ p.proveedorNombre || '—' }}</td>
                <td>{{ p.precioCompra | currency: 'PEN' : 'S/ ' }}</td>
                <td>{{ p.precioVenta | currency: 'PEN' : 'S/ ' }}</td>
                <td>
                  <span class="badge" [class.badge-danger]="p.stockBajo" [class.badge-ok]="!p.stockBajo">
                    {{ p.stockActual }}
                  </span>
                  @if (p.stockBajo) { <small class="muted"> mín. {{ p.stockMinimo }}</small> }
                </td>
                <td>
                  @if (p.fechaVencimiento) {
                    <span class="badge" [class.badge-warn]="porVencer(p)">
                      {{ p.fechaVencimiento | date: 'dd/MM/yyyy' }}
                    </span>
                  } @else { — }
                </td>
                <td class="actions">
                  <button class="btn btn-ghost btn-sm" (click)="editar(p)">✏️ Editar</button>
                  @if (auth.isAdmin()) {
                    <button class="btn btn-danger btn-sm" (click)="eliminar(p)">🗑️</button>
                  }
                </td>
              </tr>
            } @empty {
              <tr><td colspan="9" class="empty">No se encontraron productos</td></tr>
            }
          </tbody>
        </table>
      </div>
    </div>

    @if (showForm()) {
      <div class="modal-backdrop" (click)="cerrar()">
        <div class="modal modal-lg" (click)="$event.stopPropagation()">
          <h3>{{ editing() ? 'Editar producto' : 'Nuevo producto' }}</h3>
          <form [formGroup]="form" (ngSubmit)="guardar()">
            <div class="row">
              <label>
                Código
                <input type="text" formControlName="codigo" [class.invalid]="invalid('codigo')" />
                @if (invalid('codigo')) { <span class="field-error">El código es obligatorio</span> }
              </label>
              <label>
                Nombre
                <input type="text" formControlName="nombre" [class.invalid]="invalid('nombre')" />
                @if (invalid('nombre')) { <span class="field-error">El nombre es obligatorio</span> }
              </label>
            </div>

            <label>
              Descripción
              <textarea formControlName="descripcion" rows="2"></textarea>
            </label>

            <div class="row">
              <label>
                Categoría
                <select formControlName="categoriaId" [class.invalid]="invalid('categoriaId')">
                  <option [ngValue]="null">Seleccione...</option>
                  @for (c of categorias(); track c.id) {
                    <option [ngValue]="c.id">{{ c.nombre }}</option>
                  }
                </select>
                @if (invalid('categoriaId')) { <span class="field-error">Seleccione una categoría</span> }
              </label>
              <label>
                Proveedor
                <select formControlName="proveedorId">
                  <option [ngValue]="null">Sin proveedor</option>
                  @for (pr of proveedores(); track pr.id) {
                    <option [ngValue]="pr.id">{{ pr.razonSocial }}</option>
                  }
                </select>
              </label>
            </div>

            <div class="row">
              <label>
                Precio de compra (S/)
                <input type="number" step="0.01" min="0" formControlName="precioCompra"
                       [class.invalid]="invalid('precioCompra')" />
                @if (invalid('precioCompra')) { <span class="field-error">Precio no válido</span> }
              </label>
              <label>
                Precio de venta (S/)
                <input type="number" step="0.01" min="0" formControlName="precioVenta"
                       [class.invalid]="invalid('precioVenta')" />
                @if (invalid('precioVenta')) { <span class="field-error">Precio no válido</span> }
              </label>
            </div>

            <div class="row">
              @if (!editing()) {
                <label>
                  Stock inicial
                  <input type="number" min="0" formControlName="stockInicial" />
                </label>
              }
              <label>
                Stock mínimo
                <input type="number" min="0" formControlName="stockMinimo"
                       [class.invalid]="invalid('stockMinimo')" />
                @if (invalid('stockMinimo')) { <span class="field-error">Valor no válido</span> }
              </label>
              <label>
                Fecha de vencimiento
                <input type="date" formControlName="fechaVencimiento" />
              </label>
            </div>

            @if (editing()) {
              <p class="muted small">
                El stock se modifica desde <strong>Movimientos</strong> (entradas y salidas).
              </p>
            }

            <div class="modal-actions">
              <button class="btn btn-ghost" type="button" (click)="cerrar()">Cancelar</button>
              <button class="btn btn-primary" type="submit" [disabled]="saving()">
                {{ saving() ? 'Guardando...' : 'Guardar' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    }
  `,
})
export class ProductosComponent {
  private readonly fb = inject(FormBuilder);
  private readonly productoService = inject(ProductoService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly proveedorService = inject(ProveedorService);
  private readonly toast = inject(ToastService);
  protected readonly auth = inject(AuthService);

  protected readonly productos = signal<Producto[]>([]);
  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly proveedores = signal<Proveedor[]>([]);
  protected readonly search = signal('');
  protected readonly filtroCategoria = signal('');
  protected readonly showForm = signal(false);
  protected readonly editing = signal<Producto | null>(null);
  protected readonly saving = signal(false);

  protected readonly filtrados = computed(() => {
    const q = this.search().toLowerCase().trim();
    const cat = this.filtroCategoria();
    return this.productos().filter(
      (p) =>
        (!q || p.nombre.toLowerCase().includes(q) || p.codigo.toLowerCase().includes(q)) &&
        (!cat || String(p.categoriaId) === cat),
    );
  });

  protected readonly form = this.fb.group({
    codigo: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(30)]),
    nombre: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(120)]),
    descripcion: this.fb.nonNullable.control(''),
    precioCompra: this.fb.nonNullable.control(0, [Validators.required, Validators.min(0)]),
    precioVenta: this.fb.nonNullable.control(0, [Validators.required, Validators.min(0)]),
    stockInicial: this.fb.nonNullable.control(0, [Validators.min(0)]),
    stockMinimo: this.fb.nonNullable.control(5, [Validators.required, Validators.min(0)]),
    fechaVencimiento: this.fb.nonNullable.control(''),
    categoriaId: this.fb.control<number | null>(null, Validators.required),
    proveedorId: this.fb.control<number | null>(null),
  });

  constructor() {
    this.cargar();
  }

  protected invalid(
    name: 'codigo' | 'nombre' | 'precioCompra' | 'precioVenta' | 'stockMinimo' | 'categoriaId',
  ): boolean {
    const c = this.form.controls[name];
    return c.invalid && c.touched;
  }

  protected porVencer(p: Producto): boolean {
    if (!p.fechaVencimiento) {
      return false;
    }
    const dias = (new Date(p.fechaVencimiento).getTime() - Date.now()) / 86_400_000;
    return dias <= 30;
  }

  private cargar(): void {
    forkJoin({
      productos: this.productoService.listar(),
      categorias: this.categoriaService.listar(),
      proveedores: this.proveedorService.listar(),
    }).subscribe({
      next: (r) => {
        this.productos.set(r.productos);
        this.categorias.set(r.categorias);
        this.proveedores.set(r.proveedores);
      },
      error: (err) => this.toast.error(errorMessage(err)),
    });
  }

  protected nuevo(): void {
    this.editing.set(null);
    this.form.reset({
      codigo: '', nombre: '', descripcion: '', precioCompra: 0, precioVenta: 0,
      stockInicial: 0, stockMinimo: 5, fechaVencimiento: '', categoriaId: null, proveedorId: null,
    });
    this.showForm.set(true);
  }

  protected editar(p: Producto): void {
    this.editing.set(p);
    this.form.reset({
      codigo: p.codigo,
      nombre: p.nombre,
      descripcion: p.descripcion ?? '',
      precioCompra: p.precioCompra,
      precioVenta: p.precioVenta,
      stockInicial: 0,
      stockMinimo: p.stockMinimo,
      fechaVencimiento: p.fechaVencimiento ?? '',
      categoriaId: p.categoriaId,
      proveedorId: p.proveedorId,
    });
    this.showForm.set(true);
  }

  protected cerrar(): void {
    this.showForm.set(false);
  }

  protected guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const current = this.editing();

    const body: ProductoRequest = {
      codigo: v.codigo.trim(),
      nombre: v.nombre.trim(),
      descripcion: v.descripcion.trim() || null,
      precioCompra: v.precioCompra,
      precioVenta: v.precioVenta,
      stockInicial: current ? null : v.stockInicial,
      stockMinimo: v.stockMinimo,
      fechaVencimiento: v.fechaVencimiento || null,
      categoriaId: v.categoriaId!,
      proveedorId: v.proveedorId,
    };

    this.saving.set(true);
    const request = current
      ? this.productoService.actualizar(current.id, body)
      : this.productoService.crear(body);

    request.subscribe({
      next: () => {
        this.toast.success(current ? 'Producto actualizado' : 'Producto creado');
        this.saving.set(false);
        this.cerrar();
        this.cargar();
      },
      error: (err) => {
        this.toast.error(errorMessage(err));
        this.saving.set(false);
      },
    });
  }

  protected eliminar(p: Producto): void {
    if (!confirm(`¿Eliminar el producto "${p.nombre}"?`)) {
      return;
    }
    this.productoService.eliminar(p.id).subscribe({
      next: () => {
        this.toast.success('Producto eliminado');
        this.cargar();
      },
      error: (err) => this.toast.error(errorMessage(err)),
    });
  }
}
