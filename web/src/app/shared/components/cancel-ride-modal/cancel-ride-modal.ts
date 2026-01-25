import { Component, EventEmitter, Output, input } from '@angular/core';
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
  @Output() onClose = new EventEmitter<void>();
  @Output() onConfirm = new EventEmitter<string>();

  reason: string = '';

  handleConfirm() {
    if (this.reason.trim()) {
      this.onConfirm.emit(this.reason);
      this.reason = ''; // reset
    }
  }
}
