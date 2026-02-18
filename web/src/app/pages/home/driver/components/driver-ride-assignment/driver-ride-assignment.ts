import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
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
import {RideAssignmentResponse} from '../../../../../model/ride';
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
  activeRide = signal<RideAssignmentResponse | null>(null);

  private rideService = inject(RideService);
  private mapService = inject(MapService);
  private toastService = inject(ToastService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  ngOnInit(): void {
    const rideId = this.route.snapshot.paramMap.get('id');

    if (rideId) {
      this.loadRideDetails(+rideId);
    } else {
      console.error('Missing Ride Id');
      this.toastService.show('Something went wrong', 'error');
      this.router.navigate(['/driver/']);
    }
  }

  private loadRideDetails(rideId: number): void {
    this.rideService.getRideAssignment(rideId).subscribe({
      next: (ride) => {
        this.activeRide.set(ride);
        if (ride.routeGeometry) {
          this.mapService.drawRoute(ride.routeGeometry);
        }
      },
      error: (error) => {
        console.error('Error loading ride assignment:', error);
        this.toastService.show('Could not load ride assignment', 'error');
        this.router.navigate(['/driver/']);
      }
    });
  }

  ngOnDestroy(): void {
    this.mapService.clearRouteAndMarkers();
  }

  handleCancel(): void {
    this.showCancelModal.set(true);
  }

  submitCancellation(reason: string): void {
    const rideId = this.activeRide()?.id;

    if (!rideId) {
      this.toastService.show('Ride ID missing', 'error');
      this.showCancelModal.set(false);
      return;
    }

    this.rideService.cancelRide(rideId, reason).subscribe({
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

  handleStartRide(): void {
    const rideId = this.activeRide()?.id;

    if (!rideId) {
      this.toastService.show('Cannot start ride: missing ID', 'error');
      return;
    }

    this.rideService.startRide(rideId).subscribe({
      next: () => {
        this.toastService.show('Ride started successfully', 'success');
        this.router.navigate(['/driver/ride-tracking', rideId]);
      },
      error: (err) => {
        console.error("Error starting the ride", err);
        const msg = err.error?.message || err.error || 'Failed to start ride';
        this.toastService.show(msg, 'error');
      }
    });
  }

  get pickup(): string {
    return this.activeRide()?.pickupAddress || 'N/A';
  }

  get destination(): string {
    return this.activeRide()?.destinationAddress || 'N/A';
  }
}
