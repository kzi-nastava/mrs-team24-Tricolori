// guest-ride-tracking.component.ts
import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import {
  heroMapPin,
  heroClock,
  heroExclamationCircle,
  heroInformationCircle,
  heroCheckCircle,
  heroXCircle
} from '@ng-icons/heroicons/outline';
import * as L from 'leaflet';
import 'leaflet-routing-machine';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';

// Use existing models and services
import { RideService } from '../../../services/ride.service';
import { RideTrackingResponse } from '../../../model/ride-tracking';

interface RideDetails {
  pickup: string;
  destination: string;
  pickupCoords: [number, number];
  destinationCoords: [number, number];
  driverName: string;
  vehicleType: string;
  licensePlate: string;
  totalDistance: number;
}

interface Location {
  lat: number;
  lng: number;
}

interface TokenValidationResponse {
  valid: boolean;
  rideId?: number;
  isRegistered?: boolean;
  message?: string;
}

type ViewState = 'validating' | 'login-prompt' | 'tracking' | 'error';

@Component({
  selector: 'app-guest-ride-tracking',
  standalone: true,
  imports: [CommonModule, NgIconComponent],
  providers: [
    provideIcons({
      heroMapPin,
      heroClock,
      heroExclamationCircle,
      heroInformationCircle,
      heroCheckCircle,
      heroXCircle
    })
  ],
  templateUrl: './guest-ride-tracking.html'
})
export class GuestRideTrackingComponent implements OnInit, OnDestroy {
  // View state management
  viewState = signal<ViewState>('validating');
  error = signal<string | null>(null);
  loading = signal(false);
  
  // Ride completion tracking
  rideStatus = signal<string>('ACTIVE');
  rideCompleted = computed(() => 
    this.rideStatus() === 'FINISHED' || this.rideStatus() === 'CANCELLED'
  );
  finalPrice = signal<number>(0);
  showCompletionDialog = signal(false);

  // Token and user info
  token: string | null = null;
  rideId: number | null = null;
  isRegisteredUser = signal(false);

  rideDetails = signal<RideDetails>({
    pickup: '',
    destination: '',
    pickupCoords: [0, 0],
    destinationCoords: [0, 0],
    driverName: '',
    vehicleType: '',
    licensePlate: '',
    totalDistance: 0
  });

  estimatedArrival = signal(0);
  remainingDistance = signal(0);
  vehicleLocation = signal<Location>({ lat: 0, lng: 0 });

  progressPercentage = computed(() => {
    const total = this.rideDetails().totalDistance;
    const remaining = this.remainingDistance();
    if (total === 0) return 0;
    return Math.round(((total - remaining) / total) * 100);
  });

  private map: L.Map | null = null;
  private routeControl: any = null;
  private vehicleMarker: L.Marker | null = null;
  private updateSubscription: Subscription | null = null;
  private routeCoordinates: [number, number][] = [];
  private currentRouteIndex: number = 0;
  private totalRouteDistance: number = 0;
  private mockMovementInterval: any = null;
  private previousStatus: string = 'ACTIVE';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private rideService: RideService
  ) {}

  ngOnInit() {
    // Try to get token from query params first, then from route params
    this.token = this.route.snapshot.queryParamMap.get('token') || 
                 this.route.snapshot.paramMap.get('token');

    console.log('Token from URL:', this.token);
    console.log('Query params:', this.route.snapshot.queryParamMap.keys);
    console.log('Route params:', this.route.snapshot.paramMap.keys);

    if (!this.token) {
      this.error.set('No tracking token provided. Please use the link sent to you.');
      this.viewState.set('error');
      return;
    }

    this.validateToken();
  }

  ngOnDestroy() {
    this.cleanup();
  }

  cleanup() {
    if (this.updateSubscription) {
      this.updateSubscription.unsubscribe();
    }
    if (this.mockMovementInterval) {
      clearInterval(this.mockMovementInterval);
    }
    if (this.routeControl && this.map) {
      this.map.removeControl(this.routeControl);
    }
    if (this.map) {
      this.map.remove();
    }
  }

  validateToken() {
    this.viewState.set('validating');
    
    // Use { responseType: 'text' } first to see what we're getting
    this.http.get(`/api/tracking/validate?token=${this.token}`, { responseType: 'text' })
      .subscribe({
        next: (response) => {
          console.log('Raw response from server:', response);
          console.log('Response type:', typeof response);
          
          // Try to parse as JSON
          try {
            const jsonResponse = JSON.parse(response) as TokenValidationResponse;
            console.log('Parsed JSON response:', jsonResponse);
            
            if (jsonResponse.valid && jsonResponse.rideId) {
              this.rideId = jsonResponse.rideId;
              this.isRegisteredUser.set(jsonResponse.isRegistered || false);
              
              if (jsonResponse.isRegistered) {
                // Show login prompt for registered users
                this.viewState.set('login-prompt');
              } else {
                // Continue to tracking for guest users
                this.continueToTracking();
              }
            } else {
              this.error.set(jsonResponse.message || 'Invalid or expired tracking token. Please request a new tracking link.');
              this.viewState.set('error');
            }
          } catch (parseError) {
            console.error('Failed to parse response as JSON:', parseError);
            console.error('Response was:', response);
            this.error.set('Server returned an invalid response. Please contact support.');
            this.viewState.set('error');
          }
        },
        error: (err) => {
          console.error('Token validation HTTP error:', err);
          console.error('Error status:', err.status);
          console.error('Error message:', err.message);
          this.error.set('Failed to validate tracking link. Please try again or contact support.');
          this.viewState.set('error');
        }
      });
  }

  continueToTracking() {
    this.viewState.set('tracking');
    this.loading.set(true);
    this.loadRideData();
  }

  loadRideData() {
    if (!this.rideId) return;

    this.rideService.trackRide(this.rideId)
      .subscribe({
        next: (data) => {
          this.updateRideData(data);
          this.initMap();
          this.startLiveUpdates();
          this.loading.set(false);
        },
        error: (err) => {
          console.error('Ride data error:', err);
          this.error.set('Failed to load ride tracking data. The ride may no longer be available.');
          this.viewState.set('error');
          this.loading.set(false);
        }
      });
  }

  updateRideData(data: RideTrackingResponse) {
    if (!data.route) return;

    // Check for ride completion status change
    const newStatus = data.status || 'ACTIVE';
    if (this.previousStatus !== 'FINISHED' && newStatus === 'FINISHED') {
      // Ride just finished - show dialog
      this.showCompletionDialog.set(true);
      
      // Stop mock movement and live updates
      if (this.mockMovementInterval) {
        clearInterval(this.mockMovementInterval);
      }
      if (this.updateSubscription) {
        this.updateSubscription.unsubscribe();
      }
    }
    
    this.previousStatus = newStatus;
    this.rideStatus.set(newStatus);
    
    // Update final price if available
    if (data.price) {
      this.finalPrice.set(data.price);
    }

    // Extract data from RideTrackingResponse using existing models
    this.rideDetails.set({
      pickup: data.route.pickupAddress,
      destination: data.route.destinationAddress,
      pickupCoords: [data.route.pickupLatitude, data.route.pickupLongitude],
      destinationCoords: [data.route.destinationLatitude, data.route.destinationLongitude],
      driverName: data.driver ? `${data.driver.firstName} ${data.driver.lastName}` : 'Not assigned',
      vehicleType: data.currentLocation?.model || 'Unknown',
      licensePlate: data.currentLocation?.plateNum || 'N/A',
      totalDistance: data.route.distanceKm
    });

    this.estimatedArrival.set(data.estimatedTimeMinutes || 0);
    this.remainingDistance.set(data.route.distanceKm);

    // Update vehicle location from VehicleLocationResponse
    if (data.currentLocation) {
      this.vehicleLocation.set({
        lat: data.currentLocation.latitude,
        lng: data.currentLocation.longitude
      });
      this.updateVehicleMarker();
    }
  }

  initMap() {
    setTimeout(() => {
      const mapElement = document.getElementById('trackingMap');
      if (!mapElement) return;

      const ride = this.rideDetails();
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

      this.extractRouteCoordinates();
    }, 100);
  }

  extractRouteCoordinates() {
    if (!this.routeControl) return;

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

        // Only start mock movement if ride is not completed
        if (!this.rideCompleted()) {
          setTimeout(() => this.startMockMovement(), 1000);
        }
      }
    });
  }

  startMockMovement() {
    if (this.routeCoordinates.length === 0 || this.rideCompleted()) return;

    this.currentRouteIndex = 0;
    const [initialLat, initialLng] = this.routeCoordinates[0];
    this.vehicleLocation.set({ lat: initialLat, lng: initialLng });
    this.updateVehicleMarker();

    const totalPoints = this.routeCoordinates.length;
    const jumpSize = Math.ceil(totalPoints / 10);

    this.mockMovementInterval = setInterval(() => {
      if (this.rideCompleted()) {
        clearInterval(this.mockMovementInterval);
        return;
      }

      this.currentRouteIndex += jumpSize;

      if (this.currentRouteIndex >= this.routeCoordinates.length) {
        this.estimatedArrival.set(0);
        this.remainingDistance.set(0);
        clearInterval(this.mockMovementInterval);
        return;
      }

      const [lat, lng] = this.routeCoordinates[this.currentRouteIndex];
      this.vehicleLocation.set({ lat, lng });
      this.updateVehicleMarker();
      this.updateProgressMetrics();
    }, 5000);
  }

  updateProgressMetrics() {
    let remainingDist = 0;
    for (let i = this.currentRouteIndex; i < this.routeCoordinates.length - 1; i++) {
      remainingDist += this.calculateDistance(
        this.routeCoordinates[i],
        this.routeCoordinates[i + 1]
      );
    }
    this.remainingDistance.set(parseFloat(remainingDist.toFixed(2)));

    const totalDistance = this.totalRouteDistance;
    const totalTimeMinutes = this.estimatedArrival();

    if (totalDistance > 0 && totalTimeMinutes > 0) {
      const remainingTimeMinutes = Math.round((remainingDist / totalDistance) * totalTimeMinutes);
      this.estimatedArrival.set(Math.max(0, remainingTimeMinutes));
    }
  }

  calculateDistance(coord1: [number, number], coord2: [number, number]): number {
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

  toRad(degrees: number): number {
    return degrees * (Math.PI / 180);
  }

  updateVehicleMarker() {
    const loc = this.vehicleLocation();
    if (loc.lat === 0 || loc.lng === 0) return;

    if (this.vehicleMarker && this.map) {
      this.vehicleMarker.setLatLng([loc.lat, loc.lng]);
    } else if (this.map) {
      const vehicleIcon = L.divIcon({
        className: 'vehicle-marker',
        html: `
          <div style="background: #10b981; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white; box-shadow: 0 3px 6px rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center;">
            <svg xmlns="http://www.w3.org/2000/svg" fill="white" viewBox="0 0 24 24" width="12" height="12">
              <path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z"/>
            </svg>
          </div>`,
        iconSize: [26, 26],
        iconAnchor: [13, 13]
      });

      this.vehicleMarker = L.marker([loc.lat, loc.lng], {
        icon: vehicleIcon,
        zIndexOffset: 1000
      }).addTo(this.map);

      this.vehicleMarker.bindPopup(`<b>Driver Location</b><br>${this.rideDetails().driverName}`);
    }
  }

  startLiveUpdates() {
    if (!this.rideId || this.rideCompleted()) return;

    this.updateSubscription = interval(5000)
      .pipe(
        switchMap(() => this.rideService.trackRide(this.rideId!))
      )
      .subscribe({
        next: (data) => this.updateRideData(data),
        error: (err) => console.error('Update error:', err)
      });
  }

  // Dialog and navigation methods
  closeCompletionDialog() {
    this.showCompletionDialog.set(false);
  }

  redirectToLogin() {
    this.router.navigate(['/login'], {
      queryParams: { 
        redirect: `/passenger/ride-tracking/${this.rideId}`,
        token: this.token 
      }
    });
  }

  goToHome() {
    this.router.navigate(['/unregistered']);
  }

  retryValidation() {
    this.error.set(null);
    this.validateToken();
  }

  // Status helper methods
  getStatusColor(): string {
    switch (this.rideStatus()) {
      case 'FINISHED':
        return 'green';
      case 'CANCELLED':
        return 'red';
      default:
        return 'teal';
    }
  }

  getStatusMessage(): string {
    switch (this.rideStatus()) {
      case 'FINISHED':
        return 'Ride Completed Successfully!';
      case 'CANCELLED':
        return 'Ride Cancelled';
      default:
        return 'Ride in Progress';
    }
  }
}