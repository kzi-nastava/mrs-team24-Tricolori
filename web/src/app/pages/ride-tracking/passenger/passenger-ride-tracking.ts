import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
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
  heroPhone,
  heroExclamationCircle,
  heroStar
} from '@ng-icons/heroicons/outline';
import { heroStarSolid } from '@ng-icons/heroicons/solid';
import * as L from 'leaflet';
import 'leaflet-routing-machine';
import { forkJoin } from 'rxjs';

import { RideService } from '../../../services/ride.service';
import { RideDetailResponse } from '../../../model/ride-history';
import {
  RideTrackingResponse,
  PanicRideRequest,
  InconsistencyReportRequest
} from '../../../model/ride-tracking';

// Local interface for component state
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

interface Location {
  lat: number;
  lng: number;
}

@Component({
  selector: 'app-passenger-ride-tracking',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgIconComponent],
  providers: [
    provideIcons({
      heroArrowLeft,
      heroMapPin,
      heroClock,
      heroExclamationTriangle,
      heroCheckCircle,
      heroPhone,
      heroExclamationCircle,
      heroStar,
      heroStarSolid
    })
  ],
  templateUrl: './passenger-ride-tracking.html'
})
export class PassengerRideTrackingComponent implements OnInit, OnDestroy {
  reportForm: FormGroup;
  ratingForm: FormGroup;
  showReportForm = signal<boolean>(false);
  isSubmittingReport = signal<boolean>(false);
  reportSubmitted = signal<boolean>(false);
  panicTriggered = signal<boolean>(false);
  
  // Ride completion modal
  showCompletionModal = signal<boolean>(false);
  isSubmittingRating = signal<boolean>(false);
  driverRating = signal<number>(0);
  vehicleRating = signal<number>(0);
  finalPrice = signal<number>(0);
  actualDuration = signal<number>(0);

  estimatedArrival = signal<number>(0);
  remainingDistance = signal<number>(0);

  rideDetails = signal<RideDetails>({
    id: 0,
    pickup: '',
    destination: '',
    pickupCoords: [0, 0],
    destinationCoords: [0, 0],
    driverName: '',
    vehicleType: '',
    licensePlate: '',
    totalDistance: 0,
    estimatedDuration: 0
  });

  vehicleLocation = signal<Location>({
    lat: 0,
    lng: 0,
  });

  passengerLocation = signal<Location>({
    lat: 0,
    lng: 0,
  });

  progressPercentage = computed(() => {
    const total = this.rideDetails().totalDistance;
    const remaining = this.remainingDistance();
    if (total === 0) return 0;
    return Math.round(((total - remaining) / total) * 100);
  });

  private rideId: number = 0;
  private map: L.Map | null = null;
  private routeControl: any = null;
  private vehicleMarker: L.Marker | null = null;
  private passengerMarker: L.Marker | null = null;
  private updateInterval: any = null;
  private mockMovementInterval: any = null;
  private isInitialized = false;
  private routeCoordinates: [number, number][] = [];
  private currentRouteIndex: number = 0;
  private totalRouteDistance: number = 0;
  private rideStartTime: Date | null = null;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private rideService: RideService
  ) {
    this.reportForm = this.fb.group({
      description: ['', [
        Validators.required,
        Validators.minLength(10),
        Validators.maxLength(500)
      ]]
    });

    this.ratingForm = this.fb.group({
      comment: ['', [Validators.maxLength(500)]]
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.rideId = +params['id'] || 7; // Default to 7 for testing
      // DELETE DEFAULT AFTER TESTING
      if (this.rideId) {
        this.rideStartTime = new Date();
        this.loadInitialData();
        this.startTracking();
      }
    });
  }

  ngOnDestroy(): void {
    this.stopTracking();
    this.stopMockMovement();

    if (this.routeControl && this.map) {
      this.map.removeControl(this.routeControl);
      this.routeControl = null;
    }
    if (this.map) {
      this.map.remove();
      this.map = null;
    }
  }

  /**
   * Start mock vehicle movement simulation using actual route coordinates
   */
  private startMockMovement(): void {
    if (this.routeCoordinates.length === 0) {
      console.warn('No route coordinates available for mock movement');
      return;
    }

    this.currentRouteIndex = 0;
    
    const [initialLat, initialLng] = this.routeCoordinates[0];
    this.vehicleLocation.set({ lat: initialLat, lng: initialLng });
    this.updateVehiclePosition();
    
    // Calculate how many points to skip to cover 1/10 of the route each interval
    const totalPoints = this.routeCoordinates.length;
    const jumpSize = Math.ceil(totalPoints / 10);
    
    this.mockMovementInterval = setInterval(() => {
      this.currentRouteIndex += jumpSize;
      
      if (this.currentRouteIndex >= this.routeCoordinates.length) {
        console.log('🏁 Mock vehicle reached destination');
        this.estimatedArrival.set(0);
        this.remainingDistance.set(0);
        this.stopMockMovement();
        this.handleRideCompletion();
        return;
      }

      const [lat, lng] = this.routeCoordinates[this.currentRouteIndex];
      
      this.vehicleLocation.set({ lat, lng });
      this.updateVehiclePosition();
      this.updateProgressMetrics();

      this.rideService.updateVehicleLocation(this.rideId, {
        latitude: lat,
        longitude: lng
      }).subscribe({
        next: () => console.log(`🚗 Vehicle location updated (${this.currentRouteIndex}/${this.routeCoordinates.length - 1}) - ${Math.round((this.currentRouteIndex / this.routeCoordinates.length) * 100)}% complete`),
        error: (err) => console.error('Error updating vehicle location:', err)
      });
    }, 5000); // Move every 5 seconds, jumping 1/10 of the route
  }

  /**
   * Handle ride completion - show modal with price and duration
   */
  private handleRideCompletion(): void {
    // Calculate actual duration
    if (this.rideStartTime) {
      const durationMs = new Date().getTime() - this.rideStartTime.getTime();
      const durationMinutes = Math.round(durationMs / 1000 / 60);
      this.actualDuration.set(durationMinutes);
    }

    // Get final price from ride details (in production, fetch from backend)
    this.rideService.getPassengerRideDetail(this.rideId).subscribe({
      next: (detail) => {
        this.finalPrice.set(detail.totalPrice);
        this.showCompletionModal.set(true);
      },
      error: (err) => {
        console.error('Error fetching ride details:', err);
        // Show modal anyway with estimated price
        this.finalPrice.set(this.rideDetails().totalDistance * 100); // Rough estimate
        this.showCompletionModal.set(true);
      }
    });
  }

  /**
   * Update ETA and remaining distance based on current progress
   */
  private updateProgressMetrics(): void {
    let remainingDist = 0;
    for (let i = this.currentRouteIndex; i < this.routeCoordinates.length - 1; i++) {
      remainingDist += this.calculateDistance(
        this.routeCoordinates[i],
        this.routeCoordinates[i + 1]
      );
    }
    this.remainingDistance.set(parseFloat(remainingDist.toFixed(2)));

    // Calculate remaining time
    const totalDistance = this.totalRouteDistance;
    const totalTimeSeconds = this.rideDetails().estimatedDuration;
    
    if (totalDistance > 0 && totalTimeSeconds > 0) {
      const remainingTimeSeconds = Math.round((remainingDist / totalDistance) * totalTimeSeconds);
      const remainingTimeMinutes = Math.round(remainingTimeSeconds / 60);
      this.estimatedArrival.set(Math.max(0, remainingTimeMinutes));
    }
  }

  /**
   * Calculate distance between two coordinates in kilometers (Haversine formula)
   */
  private calculateDistance(coord1: [number, number], coord2: [number, number]): number {
    const [lat1, lon1] = coord1;
    const [lat2, lon2] = coord2;
    
    const R = 6371;
    const dLat = this.toRad(lat2 - lat1);
    const dLon = this.toRad(lon2 - lon1);
    
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
              Math.cos(this.toRad(lat1)) * Math.cos(this.toRad(lat2)) *
              Math.sin(dLon / 2) * Math.sin(dLon / 2);
    
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }

  private toRad(degrees: number): number {
    return degrees * (Math.PI / 180);
  }

  private stopMockMovement(): void {
    if (this.mockMovementInterval) {
      clearInterval(this.mockMovementInterval);
      this.mockMovementInterval = null;
    }
  }

  /**
   * Extract route coordinates from Leaflet Routing Control
   */
  private extractRouteCoordinates(): void {
    if (!this.routeControl) {
      console.warn('Route control not initialized');
      return;
    }

    this.routeControl.on('routesfound', (e: any) => {
      const routes = e.routes;
      if (routes && routes.length > 0) {
        const route = routes[0];
        
        this.routeCoordinates = route.coordinates.map((coord: any) => 
          [coord.lat, coord.lng] as [number, number]
        );

        this.totalRouteDistance = 0;
        for (let i = 0; i < this.routeCoordinates.length - 1; i++) {
          this.totalRouteDistance += this.calculateDistance(
            this.routeCoordinates[i],
            this.routeCoordinates[i + 1]
          );
        }

        // console.log(`📍 Extracted ${this.routeCoordinates.length} route points from OSRM`);
        // console.log(`📏 Total route distance: ${this.totalRouteDistance.toFixed(2)} km`);
        
        setTimeout(() => this.startMockMovement(), 1000);
      }
    });
  }

  private loadInitialData(): void {
    forkJoin({
      details: this.rideService.getPassengerRideDetail(this.rideId),
      tracking: this.rideService.trackRide(this.rideId)
    }).subscribe({
      next: (result) => {
        this.updateRideDetailsFromDetail(result.details);
        this.updateTrackingData(result.tracking);
        
        if (!this.isInitialized) {
          setTimeout(() => {
            this.initMap();
            this.extractRouteCoordinates();
          }, 100);
          this.isInitialized = true;
        }
      },
      error: (error) => {
        console.error('Error loading initial ride data:', error);
      }
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
      estimatedDuration: detail.duration // THIS IS IN SECONDS from backend
    });

    // Set initial ETA in minutes
    const initialEtaMinutes = Math.round(detail.duration / 60);
    this.estimatedArrival.set(initialEtaMinutes);
    this.remainingDistance.set(detail.distance); // Already in KM from backend
  }

  private loadTrackingData(): void {
    this.rideService.trackRide(this.rideId).subscribe({
      next: (response: RideTrackingResponse) => {
        this.updateTrackingData(response);
      },
      error: (error) => {
        console.error('Error loading tracking data:', error);
      }
    });
  }

  private updateTrackingData(response: RideTrackingResponse): void {
    if (response.driver) {
      this.rideDetails.update(current => ({
        ...current,
        driverName: `${response.driver!.firstName} ${response.driver!.lastName}`
      }));
    }

    if (response.status === 'PANIC' && !this.panicTriggered()) {
      this.panicTriggered.set(true);
      this.stopTracking();
      this.stopMockMovement();
      this.updateVehicleMarker();
    }
  }

  private initMap(): void {
    const mapElement = document.getElementById('trackingMap');
    if (!mapElement) {
      console.warn('Map element not found');
      return;
    }

    const ride = this.rideDetails();
    
    if (ride.pickupCoords[0] === 0 || ride.destinationCoords[0] === 0) {
      console.warn('Invalid coordinates for map initialization');
      return;
    }

    const centerLat = (ride.pickupCoords[0] + ride.destinationCoords[0]) / 2;
    const centerLng = (ride.pickupCoords[1] + ride.destinationCoords[1]) / 2;

    this.map = L.map('trackingMap').setView([centerLat, centerLng], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    const pickupIcon = L.divIcon({
      className: 'custom-marker-icon',
      html: `<div style="background: #00acc1; width: 16px; height: 16px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
      iconSize: [22, 22],
      iconAnchor: [11, 11]
    });

    const destinationIcon = L.divIcon({
      className: 'custom-marker-icon',
      html: `<div style="background: #ec407a; width: 16px; height: 16px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
      iconSize: [22, 22],
      iconAnchor: [11, 11]
    });

    this.routeControl = L.Routing.control({
      waypoints: [
        L.latLng(ride.pickupCoords[0], ride.pickupCoords[1]),
        L.latLng(ride.destinationCoords[0], ride.destinationCoords[1])
      ],
      router: L.Routing.osrmv1({
        serviceUrl: 'https://router.project-osrm.org/route/v1'
      }),
      lineOptions: {
        styles: [{ color: '#00acc1', opacity: 0.7, weight: 4 }],
        extendToWaypoints: false,
        missingRouteTolerance: 0
      },
      show: false,
      addWaypoints: false,
      fitSelectedRoutes: true,
      createMarker: (i: number, waypoint: any, n: number) => {
        const icon = i === 0 ? pickupIcon : destinationIcon;
        return L.marker(waypoint.latLng, { icon });
      }
    } as any).addTo(this.map);
  }

  private createVehicleIcon(isPanic: boolean): L.DivIcon {
    const bgColor = isPanic ? '#dc2626' : '#10b981';
    const pulseAnimation = isPanic ? 'animation: pulse 1s cubic-bezier(0.4, 0, 0.6, 1) infinite;' : '';

    return L.divIcon({
      className: 'vehicle-marker',
      html: `
        <style>
          @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.5; }
          }
        </style>
        <div style="background: ${bgColor}; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white; box-shadow: 0 3px 6px rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; ${pulseAnimation}">
          <svg xmlns="http://www.w3.org/2000/svg" fill="white" viewBox="0 0 24 24" width="12" height="12">
            <path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z"/>
          </svg>
        </div>`,
      iconSize: [26, 26],
      iconAnchor: [13, 13]
    });
  }

  private startTracking(): void {
    this.updateInterval = setInterval(() => {
      this.loadTrackingData();
    }, 5000);
  }

  private stopTracking(): void {
    if (this.updateInterval) {
      clearInterval(this.updateInterval);
      this.updateInterval = null;
    }
  }

  private updateVehiclePosition(): void {
    const vehicleLoc = this.vehicleLocation();
    
    if (vehicleLoc.lat === 0 || vehicleLoc.lng === 0) {
      return;
    }

    if (this.vehicleMarker && this.map) {
      this.vehicleMarker.setLatLng([vehicleLoc.lat, vehicleLoc.lng]);
    } else if (this.map && !this.vehicleMarker) {
      const vehicleIcon = this.createVehicleIcon(this.panicTriggered());
      this.vehicleMarker = L.marker([vehicleLoc.lat, vehicleLoc.lng], {
        icon: vehicleIcon,
        zIndexOffset: 1000
      }).addTo(this.map);

      this.vehicleMarker.bindPopup(`<b>Driver Location</b><br>${this.rideDetails().driverName}`);
    }
  }

  // Rating methods
  setDriverRating(rating: number): void {
    this.driverRating.set(rating);
  }

  setVehicleRating(rating: number): void {
    this.vehicleRating.set(rating);
  }

  submitRating(): void {
    if (this.isSubmittingRating()) return;

    this.isSubmittingRating.set(true);

    const ratingRequest = {
      driverRating: this.driverRating(),
      vehicleRating: this.vehicleRating(),
      comment: this.ratingForm.value.comment || ''
    };

    // Call rating API
    this.rideService.rateRide(this.rideId, ratingRequest).subscribe({
      next: () => {
        this.isSubmittingRating.set(false);
        this.showCompletionModal.set(false);
        this.router.navigate(['/passenger/rate']);
      },
      error: (err) => {
        console.error('Error submitting rating:', err);
        this.isSubmittingRating.set(false);
      }
    });
  }

  skipRating(): void {
    this.showCompletionModal.set(false);
    this.router.navigate(['/passenger/home']);
  }

  toggleReportForm(): void {
    this.showReportForm.update(value => !value);
    if (!this.showReportForm()) {
      this.reportForm.reset();
    }
  }

  submitReport(): void {
    if (this.reportForm.invalid || this.isSubmittingReport()) {
      return;
    }

    this.isSubmittingReport.set(true);

    const reportRequest: InconsistencyReportRequest = {
      description: this.reportForm.value.description
    };

    this.rideService.reportInconsistency(this.rideId, reportRequest).subscribe({
      next: () => {
        this.isSubmittingReport.set(false);
        this.reportSubmitted.set(true);
        this.showReportForm.set(false);
        this.reportForm.reset();

        setTimeout(() => {
          this.reportSubmitted.set(false);
        }, 5000);
      },
      error: (error) => {
        console.error('Error submitting report:', error);
        this.isSubmittingReport.set(false);
      }
    });
  }

  triggerPanic(): void {
    if (this.panicTriggered()) {
      return;
    }

    const panicRequest: PanicRideRequest = { 
      vehicleLocation: {
        lat: this.vehicleLocation().lat,
        lng: this.vehicleLocation().lng
      }
    };

    this.rideService.ridePanic(this.rideId, panicRequest).subscribe({
      next: () => {
        this.panicTriggered.set(true);
        this.stopTracking();
        this.stopMockMovement();
        this.updateVehicleMarker();
        console.log('🚨 Panic alert sent to central dispatch');
      },
      error: (err) => {
        console.error('Error triggering panic:', err);
      }
    });
  }

  private updateVehicleMarker(): void {
    if (this.vehicleMarker && this.map) {
      const panicIcon = this.createVehicleIcon(true);
      this.vehicleMarker.setIcon(panicIcon);

      this.vehicleMarker.setPopupContent(
        `<b style="color: #dc2626;">⚠️ EMERGENCY ALERT</b><br>${this.rideDetails().driverName}`
      );
      this.vehicleMarker.openPopup();
    }
  }

  handleBack(): void {
    this.router.navigate(['/passenger/home']);
  }
}