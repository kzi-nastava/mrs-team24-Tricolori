import {Component, signal} from '@angular/core';
import {NgIcon} from '@ng-icons/core';
import {NgClass} from '@angular/common';
import { AuthService } from '../../../services/auth.service';
import {DriverDailyLogService} from '../../../services/driver-daily-log.service';

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

  constructor(
    private authService: AuthService,
    private driverDailyLogService: DriverDailyLogService,
  ) {}

  toggleStatus() {
    const requestedStatus = !this.active();

    this.driverDailyLogService.changeStatus(requestedStatus).subscribe({
      next: () => {
        this.active.set(requestedStatus);
        console.log(`Status successfully changed to: ${this.active()}`);
      },
      error: (err) => {
        alert(err.error || 'Error changing driver status.');
        console.error('Error changing driver status', err);
      }
    })
  }

  logout() {
    if (this.active()) {
      this.driverDailyLogService.changeStatus(false).subscribe({
        next: () => this.completeLogout(),
        error: (err) => alert("Complete your ride before logging out!")
      });
    } else {
      this.completeLogout();
    }
  }

  private completeLogout() {
    this.authService.logout();
  }
}
