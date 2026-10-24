import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { API_URL } from './config';
import { LoginRequest, LoginResponse, RegistroRequest } from './models';

const STORAGE_KEY = 'bodegapro.session';

export interface Session {
  token: string;
  username: string;
  nombre: string;
  role: string;
  expiresAt: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly _session = signal<Session | null>(this.load());

  readonly session = this._session.asReadonly();
  readonly isAdmin = computed(() => this._session()?.role === 'ADMIN');

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API_URL}/auth/login`, request).pipe(
      tap((res) => {
        const session: Session = {
          token: res.token,
          username: res.username,
          nombre: res.nombre,
          role: res.role,
          expiresAt: Date.now() + res.expiresInMinutes * 60_000,
        };
        localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
        this._session.set(session);
      }),
    );
  }

  register(request: RegistroRequest): Observable<unknown> {
    return this.http.post(`${API_URL}/auth/register`, request);
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    this._session.set(null);
    this.router.navigateByUrl('/login');
  }

  token(): string | null {
    return this.isActive() ? this._session()!.token : null;
  }

  /** true si existe sesión y el token no ha expirado. */
  isActive(): boolean {
    const s = this._session();
    return !!s && s.expiresAt > Date.now();
  }

  private load(): Session | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? (JSON.parse(raw) as Session) : null;
    } catch {
      return null;
    }
  }
}
