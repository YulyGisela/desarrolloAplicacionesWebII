import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { errorMessage } from '../core/http-error';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth-page">
      <div class="auth-card">
        <div class="auth-logo">🛒</div>
        <h1>Bodega Don Pepe</h1>
        <p class="muted">Inicia sesión para administrar tu inventario</p>

        <form [formGroup]="form" (ngSubmit)="submit()">
          <label>
            Usuario
            <input type="text" formControlName="username" placeholder="admin" autocomplete="username"
                   [class.invalid]="invalid('username')" />
            @if (invalid('username')) { <span class="field-error">El usuario es obligatorio</span> }
          </label>

          <label>
            Contraseña
            <input type="password" formControlName="password" placeholder="••••••••"
                   autocomplete="current-password" [class.invalid]="invalid('password')" />
            @if (invalid('password')) { <span class="field-error">La contraseña es obligatoria</span> }
          </label>

          @if (error()) { <div class="alert alert-error">{{ error() }}</div> }

          <button class="btn btn-primary btn-block" type="submit" [disabled]="loading()">
            {{ loading() ? 'Ingresando...' : 'Ingresar' }}
          </button>
        </form>

        <p class="auth-footer">
          ¿No tienes cuenta? <a routerLink="/registro">Regístrate</a>
        </p>
        <p class="hint">Demo: <code>admin</code> / <code>admin123</code> &nbsp;·&nbsp;
          <code>vendedor</code> / <code>vendedor123</code></p>
      </div>
    </div>
  `,
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly error = signal('');

  protected readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  protected invalid(name: 'username' | 'password'): boolean {
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
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => this.router.navigateByUrl('/dashboard'),
      error: (err) => {
        this.error.set(errorMessage(err));
        this.loading.set(false);
      },
    });
  }
}
