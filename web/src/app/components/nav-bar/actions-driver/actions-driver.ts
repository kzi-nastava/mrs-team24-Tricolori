import {Component, inject, signal} from '@angular/core';
import {NgIcon} from '@ng-icons/core';
import {NgClass} from '@angular/common';
import { AuthService } from '../../../services/auth.service';
import {DriverDailyLogService} from '../../../services/driver-daily-log.service';
import {ToastService} from '../../../services/toast.service';

@Component({
  selector: 'app-actions-driver',
  imports: [
    NgIcon,
    NgClass,
  ],
  templateUrl: './actions-driver.html',
  styleUrl: './actions-driver.css',
})
export class ActionsDriver {
  active = signal<boolean>(true);

  private authService = inject(AuthService);
  private driverDailyLogService = inject(DriverDailyLogService);
  private toastService = inject(ToastService);

  toggleStatus() {
    const requestedStatus = !this.active();

    this.driverDailyLogService.changeStatus(requestedStatus).subscribe({
      next: () => {
        this.active.set(requestedStatus);
        const message = requestedStatus ? 'You are now Online' : 'You are now Offline';
        this.toastService.show(message, requestedStatus ? 'success' : 'warning');
      },
      error: (err) => {
        const errorMsg = err.error?.message || 'Error changing driver status.';
        this.toastService.show(errorMsg, 'error');
      }
    });
  }

  logout() {
    if (this.active()) {
      this.driverDailyLogService.changeStatus(false).subscribe({
        next: () => this.completeLogout(),
        error: () => {
          this.toastService.show("Finish your active ride before logging out!", "error");
        }
      });
    } else {
      this.completeLogout();
    }
  }

  private completeLogout() {
    this.authService.logout();
  }
}
