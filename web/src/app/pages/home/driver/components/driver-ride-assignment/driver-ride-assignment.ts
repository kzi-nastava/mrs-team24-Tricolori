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

  // Updated activeRide with complete data
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
    setTimeout(() => {
      this.drawRouteOnExistingMap();
    }, 500);
  }

  ngOnDestroy(): void {
    try {
      this.mapService.clearRouteAndMarkers();
    } catch (e) {
      console.log('Route already cleaned up');
    }
  }

  private async drawRouteOnExistingMap(): Promise<void> {
    const ride = this.activeRide();
    
    try {
      // Check if map exists
      const map = this.mapService.getMap();
      
      if (!map) {
        console.error('Map not found');
        return;
      }

      console.log('🗺️ Drawing route on existing map');
      console.log('📍 Pickup:', ride.pickupCoords);
      console.log('📍 Destination:', ride.destinationCoords);

      // Fetch route coordinates from OSRM
      const url = `https://router.project-osrm.org/route/v1/driving/${ride.pickupCoords[1]},${ride.pickupCoords[0]};${ride.destinationCoords[1]},${ride.destinationCoords[0]}?overview=full&geometries=geojson`;

      const response = await fetch(url);
      const data = await response.json();

      if (data.routes && data.routes.length > 0) {
        const coordinates = data.routes[0].geometry.coordinates;
        console.log('✅ Route fetched:', coordinates.length, 'points');

        // Convert coordinates to Leaflet LatLng format
        const routeCoordinates = coordinates.map((coord: number[]) => 
          L.latLng(coord[1], coord[0])
        );

        // Draw the route with markers and line
        this.mapService.drawRoute(
          '',
          ride.pickupCoords,
          ride.destinationCoords,
          routeCoordinates
        );

        console.log('✅ Route drawn successfully');
      } else {
        console.warn('⚠️ No routes found in OSRM response');
        
        // Draw at least the markers even if no route
        this.mapService.drawRoute(
          '',
          ride.pickupCoords,
          ride.destinationCoords
        );
      }
    } catch (error) {
      console.error('❌ Error drawing route:', error);
      
      // Fallback: draw at least the markers
      try {
        this.mapService.drawRoute(
          '',
          ride.pickupCoords,
          ride.destinationCoords
        );
      } catch (fallbackError) {
        console.error('❌ Fallback also failed:', fallbackError);
      }
    }
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

  handleStartRide(): void {
    console.log('Starting the ride...');
    const rideId = this.activeRide().id;
    
    // Navigate to ride tracking page
    this.router.navigate(['/driver/ride-tracking', rideId]);
  }
}