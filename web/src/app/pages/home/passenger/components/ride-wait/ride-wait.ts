import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIcon, provideIcons } from '@ng-icons/core';
import {
  heroMapPin,
  heroFlag,
  heroClock,
  heroXMark,
  heroUser,
  heroShieldCheck
} from '@ng-icons/heroicons/outline';
import { CancelRideModalComponent } from '../../../driver/components/cancel-ride-modal/cancel-ride-modal';
import { RideService } from '../../../../../services/ride.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-ride-wait',
  standalone: true,
  imports: [CommonModule, NgIcon, CancelRideModalComponent],
  templateUrl: './ride-wait.html',
  viewProviders: [provideIcons({
    heroMapPin, heroFlag, heroClock, heroXMark, heroUser, heroShieldCheck
  })]
})
export class RideWait {
  private rideService = inject(RideService);
  private router = inject(Router);

  showCancelModal = signal(false);
  errorMessage = signal<string | null>(null);

  activeRide = signal({
    id: 123,
    pickup: 'Kraljev park, Novi Sad',
    destination: 'Jevrejska 23b, Novi Sad',
    eta: 4,
    driverName: 'Marko Marković',
    carModel: 'Skoda Octavia',
    plateNum: 'NS-123-TX',
    price: 450
  });

  handleCancel() {
    this.showCancelModal.set(true);
  }

  submitCancellation(reason: string) {
    this.errorMessage.set(null);
    this.rideService.cancelRide(this.activeRide().id, reason).subscribe({
      next: () => {
        this.showCancelModal.set(false);
        this.router.navigate(['/passenger/home']);
      },
      error: (err) => {
        console.error("Cancellation failed", err);
        this.errorMessage.set("Could not cancel ride. Please try again.");
      }
    });
  }
}
