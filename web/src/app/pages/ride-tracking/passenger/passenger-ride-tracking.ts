import { Component, OnInit, OnDestroy, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import {
  heroArrowLeft,
  heroMapPin,
  heroClock,
  heroExclamationTriangle,
  heroCheckCircle,
  heroStar
} from '@ng-icons/heroicons/outline';
import { heroStarSolid } from '@ng-icons/heroicons/solid';
import { forkJoin } from 'rxjs';
import * as L from 'leaflet';
import 'leaflet-routing-machine';

import { RideService } from '../../../services/ride.service';
import { MapService } from '../../../services/map.service';
import { Map as MapComponent } from '../../../components/map/map';
import { RideDetailResponse } from '../../../model/ride-history';
import { RideTrackingResponse, PanicRideRequest, InconsistencyReportRequest } from '../../../model/ride-tracking';
import { Vehicle } from '../../../model/vehicle.model';

interface RideDetails {
  id: number;
  pickup: string;
  destination: string;
  pickupCoords: [number, number];
  destinationCoords: [number, number];
  driverName: string;
  vehicleType: string;
  licensePlate: string;
  totalDistance: number;
  estimatedDuration: number;
}

@Component({
  selector: 'app-passenger-ride-tracking',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgIconComponent, MapComponent],
  providers: [
    provideIcons({
      heroArrowLeft, heroMapPin, heroClock,
      heroExclamationTriangle, heroCheckCircle,
      heroStar, heroStarSolid
    })
  ],
  templateUrl: './passenger-ride-tracking.html'
})
export class PassengerRideTrackingComponent implements OnInit, OnDestroy {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private rideService = inject(RideService);
  private mapService = inject(MapService);

  reportForm: FormGroup;
  ratingForm: FormGroup;

  showReportForm = signal<boolean>(false);
  isSubmittingReport = signal<boolean>(false);
  reportSubmitted = signal<boolean>(false);
  panicTriggered = signal<boolean>(false);

  showCompletionModal = signal<boolean>(false);
  isSubmittingRating = signal<boolean>(false);
  driverRating = signal<number>(0);
  vehicleRating = signal<number>(0);
  finalPrice = signal<number>(0);
  actualDuration = signal<number>(0);

  estimatedArrival = signal<number>(0);
  remainingDistance = signal<number>(0);

  rideDetails = signal<RideDetails>({
    id: 0, pickup: '', destination: '',
    pickupCoords: [0, 0], destinationCoords: [0, 0],
    driverName: '', vehicleType: '', licensePlate: '',
    totalDistance: 0, estimatedDuration: 0
  });

  progressPercentage = computed(() => {
    const total = this.rideDetails().totalDistance;
    const remaining = this.remainingDistance();
    if (total === 0) return 0;
    return Math.round(((total - remaining) / total) * 100);
  });

  private rideId = 0;
  private trackingInterval: any = null;
  private rideStartTime: Date | null = null;

  // Last known vehicle position — used for panic payload and marker updates
  private lastVehicleLat = 0;
  private lastVehicleLng = 0;

  constructor() {
    this.reportForm = this.fb.group({
      description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(500)]]
    });
    this.ratingForm = this.fb.group({
      comment: ['', [Validators.maxLength(500)]]
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.rideId = +params['id'] || 7; // DELETE DEFAULT AFTER TESTING
      if (this.rideId) {
        this.rideStartTime = new Date();
        this.loadInitialData();
      }
    });
  }

  ngOnDestroy(): void {
    this.stopTracking();
  }

  // ---------------------------------------------------------------------------
  // Data loading
  // ---------------------------------------------------------------------------

  private loadInitialData(): void {
    forkJoin({
      details: this.rideService.getPassengerRideDetail(this.rideId),
      tracking: this.rideService.trackRide(this.rideId)
    }).subscribe({
      next: ({ details, tracking }) => {
        this.updateRideDetailsFromDetail(details);
        this.updateTrackingData(tracking);
        this.initRoute();
        this.startTracking();
      },
      error: (err) => console.error('Error loading initial ride data:', err)
    });
  }

  private updateRideDetailsFromDetail(detail: RideDetailResponse): void {
    this.rideDetails.set({
      id: detail.id,
      pickup: detail.pickupAddress,
      destination: detail.dropoffAddress,
      pickupCoords: [detail.pickupLatitude, detail.pickupLongitude],
      destinationCoords: [detail.dropoffLatitude, detail.dropoffLongitude],
      driverName: detail.driverName,
      vehicleType: detail.vehicleModel,
      licensePlate: detail.vehicleLicensePlate,
      totalDistance: detail.distance,
      estimatedDuration: detail.duration // seconds from backend
    });
    this.estimatedArrival.set(Math.round(detail.duration / 60));
    this.remainingDistance.set(detail.distance);
  }

  private loadTrackingData(): void {
    this.rideService.trackRide(this.rideId).subscribe({
      next: (response) => this.updateTrackingData(response),
      error: (err) => console.error('Error loading tracking data:', err)
    });
  }

  /**
   * Called on every poll — updates vehicle marker, ETA, distance, and status
   * from the live data the driver is pushing to the DB.
   */
  private updateTrackingData(response: RideTrackingResponse): void {
    if (response.driver) {
      this.rideDetails.update(current => ({
        ...current,
        driverName: `${response.driver!.firstName} ${response.driver!.lastName}`
      }));
    }

    // Update vehicle marker position from real DB location
    if (response.currentLocation) {
      this.lastVehicleLat = response.currentLocation.latitude;
      this.lastVehicleLng = response.currentLocation.longitude;
      this.refreshVehicleMarker();
    }

    // Derive remaining distance from vehicle's current position to destination
    if (response.currentLocation) {
      const dest = this.rideDetails().destinationCoords;
      if (dest[0] !== 0 && dest[1] !== 0) {
        const remaining = this.calculateDistance(
          response.currentLocation.latitude,
          response.currentLocation.longitude,
          dest[0],
          dest[1]
        );
        this.remainingDistance.set(parseFloat(remaining.toFixed(2)));

        // Recalculate ETA proportionally based on remaining vs total distance
        const total = this.rideDetails().totalDistance;
        const duration = this.rideDetails().estimatedDuration; // seconds
        if (total > 0 && duration > 0) {
          const remainingSeconds = Math.round((remaining / total) * duration);
          this.estimatedArrival.set(Math.max(0, Math.round(remainingSeconds / 60)));
        }
      }
    }

    // Handle ride completion
    if (response.status === 'FINISHED') {
      this.stopTracking();
      this.handleRideCompletion();
      return;
    }

    // Handle panic triggered by driver-side
    if (response.status === 'PANIC' && !this.panicTriggered()) {
      this.panicTriggered.set(true);
      this.stopTracking();
      this.refreshVehicleMarker(); // redraws in red
    }
  }

  // ---------------------------------------------------------------------------
  // Route drawing — routing machine draws the polyline, MapService places pins
  // ---------------------------------------------------------------------------

  private initRoute(): void {
    const ride = this.rideDetails();
    if (ride.pickupCoords[0] === 0 || ride.destinationCoords[0] === 0) return;

    const attempt = () => {
      const map = this.mapService.getMap();
      if (map) {
        this.drawRoute(ride, map);
      } else {
        setTimeout(attempt, 50);
      }
    };
    attempt();
  }

  private drawRoute(ride: RideDetails, map: L.Map): void {
    const routeControl = L.Routing.control({
      waypoints: [
        L.latLng(ride.pickupCoords[0], ride.pickupCoords[1]),
        L.latLng(ride.destinationCoords[0], ride.destinationCoords[1])
      ],
      router: L.Routing.osrmv1({ serviceUrl: 'https://router.project-osrm.org/route/v1' }),
      lineOptions: {
        styles: [{ color: '#00acc1', opacity: 0.7, weight: 4 }],
        extendToWaypoints: false,
        missingRouteTolerance: 0
      },
      show: false,
      addWaypoints: false,
      fitSelectedRoutes: true,
      createMarker: () => null as any // suppress routing machine pins — MapService.drawRoute() handles them
    } as any).addTo(map);

    routeControl.on('routesfound', (e: any) => {
      const route = e.routes?.[0];
      if (!route) return;
      // Place pickup + destination pins via MapService
      this.mapService.drawRoute(route.coordinates.map((c: any) => L.latLng(c.lat, c.lng)));
    });
  }

  // ---------------------------------------------------------------------------
  // Vehicle marker — reflects real DB position on every poll
  // ---------------------------------------------------------------------------

  private refreshVehicleMarker(): void {
    if (this.lastVehicleLat === 0 && this.lastVehicleLng === 0) return;
    const ride = this.rideDetails();

    const driverVehicle: Vehicle = {
      vehicleId: ride.id,
      latitude: this.lastVehicleLat,
      longitude: this.lastVehicleLng,
      model: ride.vehicleType,
      plateNum: ride.licensePlate,
      available: !this.panicTriggered(), // false → red marker in MapService
      specification: { type: 'STANDARD', seats: 0, babyTransport: false, petTransport: false }
    };

    this.mapService.updateVehicleMarkers([driverVehicle]);
  }

  // ---------------------------------------------------------------------------
  // Tracking interval
  // ---------------------------------------------------------------------------

  private startTracking(): void {
    this.trackingInterval = setInterval(() => this.loadTrackingData(), 5000);
  }

  private stopTracking(): void {
    if (this.trackingInterval) {
      clearInterval(this.trackingInterval);
      this.trackingInterval = null;
    }
  }

  // ---------------------------------------------------------------------------
  // Utility
  // ---------------------------------------------------------------------------

  private calculateDistance(lat1: number, lng1: number, lat2: number, lng2: number): number {
    const R = 6371;
    const dLat = this.toRad(lat2 - lat1);
    const dLon = this.toRad(lng2 - lng1);
    const a = Math.sin(dLat / 2) ** 2 +
              Math.cos(this.toRad(lat1)) * Math.cos(this.toRad(lat2)) * Math.sin(dLon / 2) ** 2;
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  private toRad(deg: number): number {
    return deg * (Math.PI / 180);
  }

  // ---------------------------------------------------------------------------
  // Ride completion
  // ---------------------------------------------------------------------------

  private handleRideCompletion(): void {
    if (this.rideStartTime) {
      this.actualDuration.set(Math.round((Date.now() - this.rideStartTime.getTime()) / 60000));
    }

    this.rideService.getPassengerRideDetail(this.rideId).subscribe({
      next: (detail) => { this.finalPrice.set(detail.totalPrice); this.showCompletionModal.set(true); },
      error: () => { this.finalPrice.set(this.rideDetails().totalDistance * 100); this.showCompletionModal.set(true); }
    });
  }

  // ---------------------------------------------------------------------------
  // Template-facing methods
  // ---------------------------------------------------------------------------

  setDriverRating(rating: number): void { this.driverRating.set(rating); }
  setVehicleRating(rating: number): void { this.vehicleRating.set(rating); }

  submitRating(): void {
    if (this.isSubmittingRating()) return;
    this.isSubmittingRating.set(true);

    this.rideService.rateRide(this.rideId, {
      driverRating: this.driverRating(),
      vehicleRating: this.vehicleRating(),
      comment: this.ratingForm.value.comment || ''
    }).subscribe({
      next: () => { this.isSubmittingRating.set(false); this.showCompletionModal.set(false); this.router.navigate(['/passenger/rate']); },
      error: () => this.isSubmittingRating.set(false)
    });
  }

  skipRating(): void {
    this.showCompletionModal.set(false);
    this.router.navigate(['/passenger/home']);
  }

  toggleReportForm(): void {
    this.showReportForm.update(v => !v);
    if (!this.showReportForm()) this.reportForm.reset();
  }

  submitReport(): void {
    if (this.reportForm.invalid || this.isSubmittingReport()) return;
    this.isSubmittingReport.set(true);

    this.rideService.reportInconsistency(this.rideId, {
      description: this.reportForm.value.description
    } as InconsistencyReportRequest).subscribe({
      next: () => {
        this.isSubmittingReport.set(false);
        this.reportSubmitted.set(true);
        this.showReportForm.set(false);
        this.reportForm.reset();
        setTimeout(() => this.reportSubmitted.set(false), 5000);
      },
      error: () => this.isSubmittingReport.set(false)
    });
  }

  triggerPanic(): void {
    if (this.panicTriggered()) return;

    this.rideService.ridePanic({
      vehicleLocation: { lat: this.lastVehicleLat, lng: this.lastVehicleLng }
    } as PanicRideRequest).subscribe({
      next: () => {
        this.panicTriggered.set(true);
        this.stopTracking();
        this.refreshVehicleMarker();
      },
      error: (err) => console.error('Error triggering panic:', err)
    });
  }

  handleBack(): void {
    this.router.navigate(['/passenger/home']);
  }
}