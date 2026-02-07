// toast.service.ts
import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface Toast {
  message: string;
  type: ToastType;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  currentToast = signal<Toast | null>(null);

  show(message: string, type: ToastType = 'success') {
    this.currentToast.set({ message, type });

    setTimeout(() => this.clear(), 4000);
  }

  clear() {
    this.currentToast.set(null);
  }
}
