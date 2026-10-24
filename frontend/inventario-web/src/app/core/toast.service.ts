import { Injectable, signal } from '@angular/core';

export interface Toast {
  type: 'success' | 'error';
  text: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly toast = signal<Toast | null>(null);
  private timer?: ReturnType<typeof setTimeout>;

  success(text: string): void {
    this.show({ type: 'success', text });
  }

  error(text: string): void {
    this.show({ type: 'error', text });
  }

  private show(toast: Toast): void {
    clearTimeout(this.timer);
    this.toast.set(toast);
    this.timer = setTimeout(() => this.toast.set(null), 4000);
  }
}
