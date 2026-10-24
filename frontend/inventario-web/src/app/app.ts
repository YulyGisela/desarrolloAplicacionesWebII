import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ToastService } from './core/toast.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: `
    <router-outlet />
    @if (toastService.toast(); as t) {
      <div class="toast" [class.toast-error]="t.type === 'error'">{{ t.text }}</div>
    }
  `,
})
export class App {
  protected readonly toastService = inject(ToastService);
}
