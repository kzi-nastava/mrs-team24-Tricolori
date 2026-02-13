// guest-ride-tracking.component.ts
import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import {
  heroMapPin, heroClock, heroExclamationCircle, heroInformationCircle, heroCheckCircle, heroXCircle
} from '@ng-icons/heroicons/outline';
import * as L from 'leaflet';
import 'leaflet-routing-machine';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { GuestRideTrackingService } from '../../../services/guest.ride.tracking.service';
import { MapService } from '../../../services/map.service';

type ViewState = 'validating' | 'login-prompt' | 'tracking' | 'error';

@Component({
  selector: 'app-guest-ride-tracking',
  standalone: true,
  imports: [CommonModule, NgIconComponent],
  providers: [MapService, provideIcons({ heroMapPin, heroClock, heroExclamationCircle, heroInformationCircle, heroCheckCircle, heroXCircle })],
  templateUrl: './guest-ride-tracking.html'
})
export class GuestRideTrackingComponent implements OnInit, OnDestroy {
  // State
  viewState = signal<ViewState>('validating');
  error = signal<string | null>(null);
  loading = signal(false);
  rideStatus = signal<string>('ACTIVE');
  rideCompleted = computed(() => this.rideStatus() === 'FINISHED' || this.rideStatus() === 'CANCELLED');
  finalPrice = signal<number>(0);
  showCompletionDialog = signal(false);
  
  token: string | null = null;
  rideId: number | null = null;
  isRegisteredUser = signal(false);
  
  rideDetails = signal({
    pickup: '', destination: '', pickupCoords: [0, 0] as [number, number],
    destinationCoords: [0, 0] as [number, number], driverName: '',
    vehicleType: '', licensePlate: '', totalDistance: 0
  });
  
  estimatedArrival = signal(0);
  remainingDistance = signal(0);
  vehicleLocation = signal({ lat: 0, lng: 0 });
  progressPercentage = computed(() => {
    const total = this.rideDetails().totalDistance;
    const remaining = this.remainingDistance();
    return total === 0 ? 0 : Math.round(((total - remaining) / total) * 100);
  });

  private routeControl: any;
  private vehicleMarker: L.Marker | null = null;
  private updateSub: Subscription | null = null;
  private routeCoords: [number, number][] = [];
  private routeIndex = 0;
  private totalDist = 0;
  private mockInterval: any;
  private prevStatus = 'ACTIVE';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private guestService: GuestRideTrackingService,
    private mapService: MapService
  ) {}

  ngOnInit() {
    this.token = this.route.snapshot.queryParamMap.get('token') || this.route.snapshot.paramMap.get('token');
    if (!this.token) {
      this.error.set('No tracking token provided. Please use the link sent to you.');
      this.viewState.set('error');
      return;
    }
    this.validateToken();
  }

  ngOnDestroy() {
    this.updateSub?.unsubscribe();
    if (this.mockInterval) clearInterval(this.mockInterval);
    if (this.routeControl) this.mapService.getMap()?.removeControl(this.routeControl);
    this.mapService.destroyMap();
  }

  validateToken() {
    this.viewState.set('validating');
    this.guestService.validateToken(this.token!).subscribe({
      next: (res) => {
        if (res.valid && res.rideId) {
          this.rideId = res.rideId;
          this.isRegisteredUser.set(res.isRegistered || false);
          res.isRegistered ? this.viewState.set('login-prompt') : this.continueToTracking();
        } else {
          this.error.set(res.message || 'Invalid or expired tracking token.');
          this.viewState.set('error');
        }
      },
      error: () => {
        this.error.set('Failed to validate tracking link.');
        this.viewState.set('error');
      }
    });
  }

  continueToTracking() {
    this.viewState.set('tracking');
    this.loading.set(true);
    // Use token-based tracking instead of ride ID
    this.guestService.trackRideByToken(this.token!).subscribe({
      next: (data) => {
        this.updateRideData(data);
        this.initMap();
        this.startLiveUpdates();
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load ride tracking data.');
        this.viewState.set('error');
        this.loading.set(false);
      }
    });
  }

  private updateRideData(data: any) {
    if (!data.route) return;

    const newStatus = data.status || 'ACTIVE';
    if (this.prevStatus !== 'FINISHED' && newStatus === 'FINISHED') {
      this.showCompletionDialog.set(true);
      if (this.mockInterval) clearInterval(this.mockInterval);
      this.updateSub?.unsubscribe();
    }
    
    this.prevStatus = newStatus;
    this.rideStatus.set(newStatus);
    if (data.price) this.finalPrice.set(data.price);

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

    if (data.currentLocation) {
      this.vehicleLocation.set({ lat: data.currentLocation.latitude, lng: data.currentLocation.longitude });
      this.updateVehicleMarker();
    }
  }

  private initMap() {
    setTimeout(() => {
      if (!document.getElementById('trackingMap')) return;
      const { pickupCoords, destinationCoords } = this.rideDetails();
      const center: [number, number] = [(pickupCoords[0] + destinationCoords[0]) / 2, (pickupCoords[1] + destinationCoords[1]) / 2];
      this.mapService.initMap('trackingMap', center, 13);

      const icon = (color: string) => L.divIcon({
        className: 'custom-marker-icon',
        html: `<div style="background: ${color}; width: 16px; height: 16px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
        iconSize: [22, 22], iconAnchor: [11, 11]
      });

      this.routeControl = L.Routing.control({
        waypoints: [L.latLng(pickupCoords[0], pickupCoords[1]), L.latLng(destinationCoords[0], destinationCoords[1])],
        router: L.Routing.osrmv1({ serviceUrl: 'https://router.project-osrm.org/route/v1' }),
        lineOptions: { styles: [{ color: '#00acc1', opacity: 0.7, weight: 4 }], extendToWaypoints: false, missingRouteTolerance: 0 },
        show: false, addWaypoints: false, fitSelectedRoutes: true,
        createMarker: (i: number, waypoint: any) => L.marker(waypoint.latLng, { icon: icon(i === 0 ? '#00acc1' : '#ec407a') })
      } as any).addTo(this.mapService.getMap());

      this.routeControl.on('routesfound', (e: any) => {
        if (e.routes?.[0]) {
          this.routeCoords = e.routes[0].coordinates.map((c: any) => [c.lat, c.lng] as [number, number]);
          this.totalDist = this.routeCoords.reduce((sum, coord, i) => 
            i === 0 ? 0 : sum + this.distance(this.routeCoords[i - 1], coord), 0
          );
          if (!this.rideCompleted()) setTimeout(() => this.startMockMovement(), 1000);
        }
      });
    }, 100);
  }

  private startMockMovement() {
    if (this.routeCoords.length === 0 || this.rideCompleted()) return;
    this.routeIndex = 0;
    const [lat, lng] = this.routeCoords[0];
    this.vehicleLocation.set({ lat, lng });
    this.updateVehicleMarker();

    const jumpSize = Math.ceil(this.routeCoords.length / 10);
    this.mockInterval = setInterval(() => {
      if (this.rideCompleted()) return clearInterval(this.mockInterval);
      this.routeIndex += jumpSize;
      if (this.routeIndex >= this.routeCoords.length) {
        this.estimatedArrival.set(0);
        this.remainingDistance.set(0);
        return clearInterval(this.mockInterval);
      }
      const [lat, lng] = this.routeCoords[this.routeIndex];
      this.vehicleLocation.set({ lat, lng });
      this.updateVehicleMarker();
      
      // Update progress
      let remaining = 0;
      for (let i = this.routeIndex; i < this.routeCoords.length - 1; i++) {
        remaining += this.distance(this.routeCoords[i], this.routeCoords[i + 1]);
      }
      this.remainingDistance.set(parseFloat(remaining.toFixed(2)));
      if (this.totalDist > 0 && this.estimatedArrival() > 0) {
        this.estimatedArrival.set(Math.max(0, Math.round((remaining / this.totalDist) * this.estimatedArrival())));
      }
    }, 5000);
  }

  private distance([lat1, lon1]: [number, number], [lat2, lon2]: [number, number]): number {
    const R = 6371, toRad = (d: number) => d * Math.PI / 180;
    const dLat = toRad(lat2 - lat1), dLon = toRad(lon2 - lon1);
    const a = Math.sin(dLat / 2) ** 2 + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) ** 2;
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  private updateVehicleMarker() {
    const { lat, lng } = this.vehicleLocation();
    if (lat === 0 || lng === 0) return;
    const map = this.mapService.getMap();
    if (!map) return;

    if (this.vehicleMarker) {
      this.vehicleMarker.setLatLng([lat, lng]);
    } else {
      this.vehicleMarker = L.marker([lat, lng], {
        icon: L.divIcon({
          className: 'vehicle-marker',
          html: `<div style="background: #10b981; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white; box-shadow: 0 3px 6px rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center;">
            <svg xmlns="http://www.w3.org/2000/svg" fill="white" viewBox="0 0 24 24" width="12" height="12">
              <path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z"/>
            </svg></div>`,
          iconSize: [26, 26], iconAnchor: [13, 13]
        }),
        zIndexOffset: 1000
      }).addTo(map);
      this.vehicleMarker.bindPopup(`<b>Driver Location</b><br>${this.rideDetails().driverName}`);
    }
  }

  private startLiveUpdates() {
    if (!this.token || this.rideCompleted()) return;
    // Use token-based tracking for live updates
    this.updateSub = interval(5000).pipe(
      switchMap(() => this.guestService.trackRideByToken(this.token!))
    ).subscribe({
      next: (data) => this.updateRideData(data),
      error: (err) => console.error('Update error:', err)
    });
  }

  // Template methods
  closeCompletionDialog() { this.showCompletionDialog.set(false); }
  redirectToLogin() { this.router.navigate(['/login'], { queryParams: { redirect: `/passenger/ride-tracking/${this.rideId}`, token: this.token } }); }
  goToHome() { this.router.navigate(['/unregistered']); }
  retryValidation() { this.error.set(null); this.validateToken(); }
  
  getStatusColor(): string {
    return this.rideStatus() === 'FINISHED' ? 'green' : this.rideStatus() === 'CANCELLED' ? 'red' : 'teal';
  }
  
  getStatusMessage(): string {
    return this.rideStatus() === 'FINISHED' ? 'Ride Completed Successfully!' : 
           this.rideStatus() === 'CANCELLED' ? 'Ride Cancelled' : 'Ride in Progress';
  }
}