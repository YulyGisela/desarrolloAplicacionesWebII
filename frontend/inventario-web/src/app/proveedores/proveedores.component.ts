import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../core/auth.service';
import { errorMessage } from '../core/http-error';
import { Proveedor } from '../core/models';
import { ProveedorService } from '../core/proveedor.service';
import { ToastService } from '../core/toast.service';

@Component({
  selector: 'app-proveedores',
  imports: [ReactiveFormsModule],
  template: `
    <header class="page-header">
      <div>
        <h2>Proveedores</h2>
        <p class="muted">Empresas que te abastecen de mercadería</p>
      </div>
      <button class="btn btn-primary" (click)="nuevo()">＋ Nuevo proveedor</button>
    </header>

    <div class="panel">
      <input class="search" type="search" placeholder="Buscar por razón social o RUC..."
             (input)="search.set($any($event.target).value)" />

      <div class="table-wrap">
        <table>
          <thead>
            <tr><th>#</th><th>Razón social</th><th>RUC</th><th>Teléfono</th><th>Correo</th><th class="actions-col">Acciones</th></tr>
          </thead>
          <tbody>
            @for (p of filtrados(); track p.id) {
              <tr>
                <td>{{ p.id }}</td>
                <td><strong>{{ p.razonSocial }}</strong></td>
                <td>{{ p.ruc }}</td>
                <td>{{ p.telefono || '—' }}</td>
                <td>{{ p.email || '—' }}</td>
                <td class="actions">
                  <button class="btn btn-ghost btn-sm" (click)="editar(p)">✏️ Editar</button>
                  @if (auth.isAdmin()) {
                    <button class="btn btn-danger btn-sm" (click)="eliminar(p)">🗑️</button>
                  }
                </td>
              </tr>
            } @empty {
              <tr><td colspan="6" class="empty">No hay proveedores registrados</td></tr>
            }
          </tbody>
        </table>
      </div>
    </div>

    @if (showForm()) {
      <div class="modal-backdrop" (click)="cerrar()">
        <div class="modal" (click)="$event.stopPropagation()">
          <h3>{{ editing() ? 'Editar proveedor' : 'Nuevo proveedor' }}</h3>
          <form [formGroup]="form" (ngSubmit)="guardar()">
            <label>
              Razón social
              <input type="text" formControlName="razonSocial" [class.invalid]="invalid('razonSocial')" />
              @if (invalid('razonSocial')) { <span class="field-error">La razón social es obligatoria</span> }
            </label>
            <label>
              RUC
              <input type="text" formControlName="ruc" maxlength="11" [class.invalid]="invalid('ruc')" />
              @if (invalid('ruc')) { <span class="field-error">El RUC debe tener 11 dígitos</span> }
            </label>
            <div class="row">
              <label>
                Teléfono
                <input type="text" formControlName="telefono" />
              </label>
              <label>
                Correo
                <input type="email" formControlName="email" [class.invalid]="invalid('email')" />
                @if (invalid('email')) { <span class="field-error">Correo no válido</span> }
              </label>
            </div>
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
export class ProveedoresComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ProveedorService);
  private readonly toast = inject(ToastService);
  protected readonly auth = inject(AuthService);

  protected readonly proveedores = signal<Proveedor[]>([]);
  protected readonly search = signal('');
  protected readonly showForm = signal(false);
  protected readonly editing = signal<Proveedor | null>(null);
  protected readonly saving = signal(false);

  protected readonly filtrados = computed(() => {
    const q = this.search().toLowerCase().trim();
    return q
      ? this.proveedores().filter(
          (p) => p.razonSocial.toLowerCase().includes(q) || p.ruc.includes(q),
        )
      : this.proveedores();
  });

  protected readonly form = this.fb.nonNullable.group({
    razonSocial: ['', [Validators.required, Validators.maxLength(120)]],
    ruc: ['', [Validators.required, Validators.pattern(/^\d{11}$/)]],
    telefono: [''],
    email: ['', Validators.email],
  });

  constructor() {
    this.cargar();
  }

  protected invalid(name: 'razonSocial' | 'ruc' | 'email'): boolean {
    const c = this.form.controls[name];
    return c.invalid && c.touched;
  }

  private cargar(): void {
    this.service.listar().subscribe({
      next: (data) => this.proveedores.set(data),
      error: (err) => this.toast.error(errorMessage(err)),
    });
  }

  protected nuevo(): void {
    this.editing.set(null);
    this.form.reset({ razonSocial: '', ruc: '', telefono: '', email: '' });
    this.showForm.set(true);
  }

  protected editar(p: Proveedor): void {
    this.editing.set(p);
    this.form.reset({
      razonSocial: p.razonSocial,
      ruc: p.ruc,
      telefono: p.telefono ?? '',
      email: p.email ?? '',
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
    const body = this.form.getRawValue();
    const current = this.editing();
    this.saving.set(true);

    const request = current
      ? this.service.actualizar(current.id!, body)
      : this.service.crear(body);

    request.subscribe({
      next: () => {
        this.toast.success(current ? 'Proveedor actualizado' : 'Proveedor creado');
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

  protected eliminar(p: Proveedor): void {
    if (!confirm(`¿Eliminar al proveedor "${p.razonSocial}"?`)) {
      return;
    }
    this.service.eliminar(p.id!).subscribe({
      next: () => {
        this.toast.success('Proveedor eliminado');
        this.cargar();
      },
      error: (err) => this.toast.error(errorMessage(err)),
    });
  }
}
