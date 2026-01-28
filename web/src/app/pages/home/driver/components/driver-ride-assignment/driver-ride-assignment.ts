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

interface RideAssignment {
  id: number;
  pickupAddress: string;
  destinationAddress: string;
  passengerName: string;
  passengerPhone: string;
  estimatedDistance: number;
  estimatedDuration: number;
  estimatedPrice: number;
  pickupCoords: [number, number];
  destinationCoords: [number, number];
  eta: number; // Time to pickup
}

@Component({
  selector: 'app-driver-ride-assignment',
  standalone: true,
  imports: [CommonModule, NgIcon, CancelRideModalComponent],
  templateUrl: './driver-ride-assignment.html',
  viewProviders: [provideIcons({
    heroMapPin, 
    heroFlag, 
    heroClock, 
    heroCheck, 
    heroXMark, 
    heroArrowLeft,
    heroUser,
    heroPhone
  })]
})
export class DriverRideAssignment implements OnInit, OnDestroy {
  showCancelModal = signal(false);
  rideService = inject(RideService);
  mapService = inject(MapService);
  router = inject(Router);
  errorMessage = signal<string | null>(null);

  // Updated activeRide with complete data from driver-waiting
  activeRide = signal<RideAssignment>({
    id: 6,
    pickupAddress: 'Železnička stanica, Novi Sad',
    destinationAddress: 'Štrand, Novi Sad',
    passengerName: 'Putnica Putnić',
    passengerPhone: '+381 64 123 4567',
    estimatedDistance: 3.3,
    estimatedDuration: 8,
    estimatedPrice: 334,
    pickupCoords: [45.2671, 19.8335],
    destinationCoords: [45.2550, 19.8450],
    eta: 5
  });

  // Simplified property accessors for template
  get pickup() {
    return this.activeRide().pickupAddress;
  }

  get destination() {
    return this.activeRide().destinationAddress;
  }

  get eta() {
    return this.activeRide().eta;
  }

  ngOnInit(): void {
    // Wait a bit for the home component's map to be ready
    setTimeout(() => {
      this.drawRouteOnExistingMap();
    }, 500);
  }

  ngOnDestroy(): void {
    // Just clear the route, don't destroy the map (it belongs to home component)
    try {
      this.mapService.clearMap();
    } catch (e) {
      console.log('Map already cleaned up');
    }
  }

  private drawRouteOnExistingMap(): void {
    const ride = this.activeRide();
    
    try {
      // Get the existing map instance
      const map = this.mapService.getMap();
      
      if (!map) {
        console.error('Map not found, cannot draw route');
        return;
      }

      // Draw route with pickup and destination markers
      this.mapService.drawRoute(
        '', // routeGeometry not used
        ride.pickupCoords,
        ride.destinationCoords
      );

      // Fetch and draw actual route from OSRM
      this.fetchAndDrawRoute(ride.pickupCoords, ride.destinationCoords);
    } catch (error) {
      console.error('Error drawing route on map:', error);
    }
  }

  private fetchAndDrawRoute(pickup: [number, number], destination: [number, number]): void {
    // Fetch route from OSRM
    const url = `https://router.project-osrm.org/route/v1/driving/${pickup[1]},${pickup[0]};${destination[1]},${destination[0]}?overview=full&geometries=geojson`;

    fetch(url)
      .then(response => response.json())
      .then(data => {
        if (data.routes && data.routes.length > 0) {
          const coordinates = data.routes[0].geometry.coordinates.map(
            (coord: number[]) => ({ lat: coord[1], lng: coord[0] })
          );

          // Convert to Leaflet LatLng format
          const L = (window as any).L;
          if (L) {
            const latLngs = coordinates.map((c: any) => L.latLng(c.lat, c.lng));

            // Redraw route with actual coordinates
            this.mapService.drawRoute(
              '',
              pickup,
              destination,
              latLngs
            );
          }
        }
      })
      .catch(err => {
        console.error('Failed to fetch route from OSRM:', err);
      });
  }

  handleBack(): void {
    console.log('Navigating back...');
    this.router.navigate(['/driver/home']);
  }

  handleCancel(): void {
    this.showCancelModal.set(true);
  }

  submitCancellation(reason: string): void {
    this.errorMessage.set(null);
    this.rideService.cancelRide(this.activeRide().id, reason).subscribe({
      next: () => {
        console.log("Ride cancelled successfully.");
        this.showCancelModal.set(false);
        // Navigate back to driver home/waiting state
        this.router.navigate(['/driver/home']);
      },
      error: (err) => {
        console.error("Error cancelling the ride", err);
        this.errorMessage.set("Failed to cancel ride. Please try again.");
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