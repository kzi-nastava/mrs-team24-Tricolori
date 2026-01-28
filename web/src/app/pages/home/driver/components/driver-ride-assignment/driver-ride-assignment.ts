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
import * as L from 'leaflet';

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

  // ================= state =================

  showCancelModal = signal(false);
  errorMessage = signal<string | null>(null);

  activeRide = signal<RideAssignment>({
    id: 6,
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

  get pickup(): string {
  return this.activeRide().pickupAddress;
}
get destination(): string {
  return this.activeRide().destinationAddress;
}
get eta(): number {
  return this.activeRide().eta;
}

  // ================= services =================

  private rideService = inject(RideService);
  private mapService = inject(MapService);
  private router = inject(Router);

  // ================= lifecycle =================

  ngOnInit(): void {
    setTimeout(() => this.drawRoute(), 500);
  }

  ngOnDestroy(): void {
    this.mapService.clearRouteAndMarkers();
  }

  // ================= map =================

  private async drawRoute(): Promise<void> {
    const { pickupCoords, destinationCoords } = this.activeRide();
    const map = this.mapService.getMap();
    if (!map) return;

    try {
      const route = await this.fetchOsrmRoute(pickupCoords, destinationCoords);
      this.mapService.drawRoute('', pickupCoords, destinationCoords, route);
    } catch {
      // fallback: samo markeri
      this.mapService.drawRoute('', pickupCoords, destinationCoords);
    }
  }

  private async fetchOsrmRoute(
    pickup: [number, number],
    dropoff: [number, number]
  ): Promise<L.LatLng[]> {
    const url = `https://router.project-osrm.org/route/v1/driving/${pickup[1]},${pickup[0]};${dropoff[1]},${dropoff[0]}?overview=full&geometries=geojson`;
    const res = await fetch(url);
    const data = await res.json();

    return data.routes?.[0]?.geometry?.coordinates.map(
      (c: number[]) => L.latLng(c[1], c[0])
    ) ?? [];
  }

  // ================= actions =================

  handleBack(): void {
    this.router.navigate(['/driver/home']);
  }

  handleCancel(): void {
    this.showCancelModal.set(true);
  }

  submitCancellation(reason: string): void {
    this.errorMessage.set(null);

    this.rideService.cancelRide(this.activeRide().id, reason).subscribe({
      next: () => {
        this.showCancelModal.set(false);
        this.router.navigate(['/driver/home']);
      },
      error: () => {
        this.errorMessage.set('Failed to cancel ride. Please try again.');
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
