import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { errorMessage } from '../core/http-error';
import { ToastService } from '../core/toast.service';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth-page">
      <div class="auth-card">
        <div class="auth-logo">🛒</div>
        <h1>Crear cuenta</h1>
        <p class="muted">Registra un usuario vendedor</p>

        <form [formGroup]="form" (ngSubmit)="submit()">
          <label>
            Nombre completo
            <input type="text" formControlName="nombre" [class.invalid]="invalid('nombre')" />
            @if (invalid('nombre')) { <span class="field-error">El nombre es obligatorio</span> }
          </label>

          <label>
            Usuario
            <input type="text" formControlName="username" [class.invalid]="invalid('username')" />
            @if (invalid('username')) { <span class="field-error">Entre 4 y 50 caracteres</span> }
          </label>

          <label>
            Correo
            <input type="email" formControlName="email" [class.invalid]="invalid('email')" />
            @if (invalid('email')) { <span class="field-error">Ingresa un correo válido</span> }
          </label>

          <label>
            Contraseña
            <input type="password" formControlName="password" [class.invalid]="invalid('password')" />
            @if (invalid('password')) { <span class="field-error">Mínimo 6 caracteres</span> }
          </label>

          @if (error()) { <div class="alert alert-error">{{ error() }}</div> }

          <button class="btn btn-primary btn-block" type="submit" [disabled]="loading()">
            {{ loading() ? 'Registrando...' : 'Registrarme' }}
          </button>
        </form>

        <p class="auth-footer">¿Ya tienes cuenta? <a routerLink="/login">Inicia sesión</a></p>
      </div>
    </div>
  `,
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  protected readonly loading = signal(false);
  protected readonly error = signal('');

  protected readonly form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(120)]],
    username: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(50)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  protected invalid(name: 'nombre' | 'username' | 'email' | 'password'): boolean {
    const c = this.form.controls[name];
    return c.invalid && c.touched;
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set('');
    this.auth.register(this.form.getRawValue()).subscribe({
      next: () => {
        this.toast.success('Cuenta creada. Ya puedes iniciar sesión.');
        this.router.navigateByUrl('/login');
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.loading.set(false);
      },
    });
  }
}
