import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { NgIcon, provideIcons } from '@ng-icons/core';
import {
  heroMapPin,
  heroFlag,
  heroClock,
  heroCheck,
  heroXMark,
  heroArrowLeft,
  heroUser,
  heroPhone
} from '@ng-icons/heroicons/outline';
import { CancelRideModalComponent } from '../cancel-ride-modal/cancel-ride-modal';
import { RideService } from '../../../../../services/ride.service';
import { MapService } from '../../../../../services/map.service';
import { RideAssignment } from '../../../../../model/ride';
import {ToastService} from '../../../../../services/toast.service';

@Component({
  selector: 'app-driver-ride-assignment',
  standalone: true,
  imports: [CommonModule, NgIcon, CancelRideModalComponent],
  templateUrl: './driver-ride-assignment.html',
  viewProviders: [
    provideIcons({
      heroMapPin,
      heroFlag,
      heroClock,
      heroCheck,
      heroXMark,
      heroArrowLeft,
      heroUser,
      heroPhone
    })
  ]
})
export class DriverRideAssignment implements OnInit, OnDestroy {

  showCancelModal = signal(false);

  activeRide = signal<RideAssignment>({
    id: 1,
    pickupAddress: 'Železnička stanica, Novi Sad',
    destinationAddress: 'Štrand, Novi Sad',
    passengerName: 'Putnica Putnić',
    passengerPhone: '+381 64 123 4567',
    estimatedDistance: 3.3,
    estimatedDuration: 8,
    estimatedPrice: 334,
    pickupCoords: [45.2656, 19.8289],
    destinationCoords: [45.2397, 19.8514],
    eta: 5
  });

  private rideService = inject(RideService);
  private mapService = inject(MapService);
  private toastService = inject(ToastService);
  private router = inject(Router);

  ngOnInit(): void {
    this.mapService.drawRoute([]);
  }

  ngOnDestroy(): void {
    this.mapService.clearRouteAndMarkers();
  }

  handleCancel(): void {
    this.showCancelModal.set(true);
  }

  submitCancellation(reason: string): void {
    this.rideService.cancelRide(reason).subscribe({
      next: () => {
        this.showCancelModal.set(false);
        this.toastService.show('Ride canceled successfully!', 'success');
        this.router.navigate(['/driver']);
      },
      error: (err) => {
        this.showCancelModal.set(false);
        const msg = err.error?.message || err.error || 'Something went wrong';
        this.toastService.show(msg, 'error');
      }
    });
  }

  handleStartRide() {
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

  get pickup(): string {
    return this.activeRide().pickupAddress;
  }

  get destination(): string {
    return this.activeRide().destinationAddress;
  }

  get eta(): number {
    return this.activeRide().eta;
  }
}
