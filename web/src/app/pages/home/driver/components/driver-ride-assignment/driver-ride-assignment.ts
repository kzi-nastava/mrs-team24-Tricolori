import {Component, inject, signal} from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIcon, provideIcons } from '@ng-icons/core';
import {
  heroMapPin,
  heroFlag,
  heroClock,
  heroCheck,
  heroXMark,
  heroArrowLeft
} from '@ng-icons/heroicons/outline';
import {CancelRideModalComponent} from '../cancel-ride-modal/cancel-ride-modal';
import {RideService} from '../../../../../services/ride.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-driver-ride-assignment',
  standalone: true,
  imports: [CommonModule, NgIcon, CancelRideModalComponent],
  templateUrl: './driver-ride-assignment.html',
  viewProviders: [provideIcons({
    heroMapPin, heroFlag, heroClock, heroCheck, heroXMark, heroArrowLeft
  })]
})
export class DriverRideAssignment {
  showCancelModal = signal(false);
  rideService = inject(RideService);
  private router = inject(Router);
  errorMessage = signal<string | null>(null);

  activeRide = signal({
    id: 1,
    pickup: 'Trg Slobode 1',
    destination: 'Kisačka 71',
    eta: 5
  });

  handleBack() {
    console.log('Navigating back...');
  }

  handleCancel() {
    this.showCancelModal.set(true);
  }

  submitCancellation(reason: string) {
    this.errorMessage.set(null);

    this.rideService.cancelRide(this.activeRide().id, reason).subscribe({
      next: () => {
        console.log("Ride cancelled successfully.");
        this.showCancelModal.set(false);
        // TODO: switch to waiting state
      },
      error: (err) => {
        console.error("Error cancelling the ride", err);
      }
    });
  }

  handleStartRide() {
    this.errorMessage.set(null);

    this.rideService.startRide(this.activeRide().id).subscribe({
      next: () => {
        console.log("Ride started successfully.");
        this.router.navigate(['/driver/ride-tracking', this.activeRide().id]);
      },
      error: (err) => {
        // console.error("Error cancelling the ride", err);
        this.router.navigate(['/driver/ride-tracking', this.activeRide().id]);
      }
    });
  }
}
