import { Component, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { heroXMark } from '@ng-icons/heroicons/outline';

@Component({
  selector: 'app-cancel-ride-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, NgIcon],
  templateUrl: 'cancel-ride-modal.html',
  viewProviders: [provideIcons({ heroXMark })]
})
export class CancelRideModalComponent {
  isVisible = input<boolean>(false);

  onClose = output<void>();
  onConfirm = output<string>();

  reason = signal('');

  handleConfirm() {
    const reasonValue = this.reason().trim();
    if (reasonValue) {
      this.onConfirm.emit(reasonValue);
      this.reason.set('');
    }
  }
}
