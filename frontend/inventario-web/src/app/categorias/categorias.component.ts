import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../core/auth.service';
import { CategoriaService } from '../core/categoria.service';
import { errorMessage } from '../core/http-error';
import { Categoria } from '../core/models';
import { ToastService } from '../core/toast.service';

@Component({
  selector: 'app-categorias',
  imports: [ReactiveFormsModule],
  template: `
    <header class="page-header">
      <div>
        <h2>Categorías</h2>
        <p class="muted">Agrupa tus productos para ubicarlos más rápido</p>
      </div>
      <button class="btn btn-primary" (click)="nuevo()">＋ Nueva categoría</button>
    </header>

    <div class="panel">
      <input class="search" type="search" placeholder="Buscar categoría..."
             (input)="search.set($any($event.target).value)" />

      <div class="table-wrap">
        <table>
          <thead><tr><th>#</th><th>Nombre</th><th>Descripción</th><th class="actions-col">Acciones</th></tr></thead>
          <tbody>
            @for (c of filtradas(); track c.id) {
              <tr>
                <td>{{ c.id }}</td>
                <td><strong>{{ c.nombre }}</strong></td>
                <td>{{ c.descripcion || '—' }}</td>
                <td class="actions">
                  <button class="btn btn-ghost btn-sm" (click)="editar(c)">✏️ Editar</button>
                  @if (auth.isAdmin()) {
                    <button class="btn btn-danger btn-sm" (click)="eliminar(c)">🗑️</button>
                  }
                </td>
              </tr>
            } @empty {
              <tr><td colspan="4" class="empty">No hay categorías registradas</td></tr>
            }
          </tbody>
        </table>
      </div>
    </div>

    @if (showForm()) {
      <div class="modal-backdrop" (click)="cerrar()">
        <div class="modal" (click)="$event.stopPropagation()">
          <h3>{{ editing() ? 'Editar categoría' : 'Nueva categoría' }}</h3>
          <form [formGroup]="form" (ngSubmit)="guardar()">
            <label>
              Nombre
              <input type="text" formControlName="nombre" [class.invalid]="invalid('nombre')" />
              @if (invalid('nombre')) { <span class="field-error">El nombre es obligatorio (máx. 80)</span> }
            </label>
            <label>
              Descripción
              <textarea formControlName="descripcion" rows="3"></textarea>
            </label>
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
export class CategoriasComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(CategoriaService);
  private readonly toast = inject(ToastService);
  protected readonly auth = inject(AuthService);

  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly search = signal('');
  protected readonly showForm = signal(false);
  protected readonly editing = signal<Categoria | null>(null);
  protected readonly saving = signal(false);

  protected readonly filtradas = computed(() => {
    const q = this.search().toLowerCase().trim();
    return q
      ? this.categorias().filter((c) => c.nombre.toLowerCase().includes(q))
      : this.categorias();
  });

  protected readonly form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(80)]],
    descripcion: [''],
  });

  constructor() {
    this.cargar();
  }

  protected invalid(name: 'nombre'): boolean {
    const c = this.form.controls[name];
    return c.invalid && c.touched;
  }

  private cargar(): void {
    this.service.listar().subscribe({
      next: (data) => this.categorias.set(data),
      error: (err) => this.toast.error(errorMessage(err)),
    });
  }

  protected nuevo(): void {
    this.editing.set(null);
    this.form.reset({ nombre: '', descripcion: '' });
    this.showForm.set(true);
  }

  protected editar(c: Categoria): void {
    this.editing.set(c);
    this.form.reset({ nombre: c.nombre, descripcion: c.descripcion ?? '' });
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
    const body = this.form.getRawValue();
    const current = this.editing();
    this.saving.set(true);

    const request = current
      ? this.service.actualizar(current.id!, body)
      : this.service.crear(body);

    request.subscribe({
      next: () => {
        this.toast.success(current ? 'Categoría actualizada' : 'Categoría creada');
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

  protected eliminar(c: Categoria): void {
    if (!confirm(`¿Eliminar la categoría "${c.nombre}"?`)) {
      return;
    }
    this.service.eliminar(c.id!).subscribe({
      next: () => {
        this.toast.success('Categoría eliminada');
        this.cargar();
      },
      error: (err) => this.toast.error(errorMessage(err)),
    });
  }
}
