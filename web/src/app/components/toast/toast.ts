import {Component, inject} from '@angular/core';
import {ToastService, ToastType} from '../../services/toast.service';
import {NgIcon} from '@ng-icons/core';

@Component({
  selector: 'app-toast',
  imports: [
    NgIcon
  ],
  templateUrl: './toast.html',
  styleUrl: './toast.css',
})
export class Toast {
  toastService = inject(ToastService);

  getStyles(type: ToastType): string {
    const base = 'text-white ';
    switch (type) {
      case 'success': return base + 'bg-emerald-600 border-emerald-400';
      case 'warning': return base + 'bg-amber-500 border-amber-300';
      case 'error':   return base + 'bg-rose-600 border-rose-400';
      default:        return base + 'bg-sky-600 border-sky-400';
    }
  }
}
