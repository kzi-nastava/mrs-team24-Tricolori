import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
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
  heroStopCircle,
  heroXMark,
  heroUser
} from '@ng-icons/heroicons/outline';

import * as L from 'leaflet';
import 'leaflet-routing-machine';

import { RideDetails, StopRideRequest, StopRideResponse } from '../../../model/ride';
import { Location } from '../../../model/location';
import { RideService } from '../../../services/ride.service';
import { VehicleService } from '../../../services/vehicle.service';
import { PanicRideRequest } from '../../../model/ride-tracking';
import { PriceConfigResponse, PricelistService } from '../../../services/pricelist.service';
import { Map } from '../../../components/map/map';
import { MapService } from '../../../services/map.service';
import { Vehicle } from '../../../model/vehicle.model';

@Component({
  selector: 'app-driver-ride-tracking',
  standalone: true,
  imports: [CommonModule, NgIconComponent, Map],
  providers: [
    provideIcons({
      heroArrowLeft,
      heroMapPin,
      heroClock,
      heroExclamationTriangle,
      heroCheckCircle,
      heroPhone,
      heroExclamationCircle,
      heroStopCircle,
      heroXMark,
      heroUser
    })
  ],
  templateUrl: './driver-ride-tracking.html'
})
export class DriverRideTrackingComponent implements OnInit, OnDestroy {

  panicTriggered = signal(false);
  stopTriggered = signal(false);

  estimatedArrival = signal(8);
  remainingDistance = signal(2.3);

  pricing = signal<PriceConfigResponse | null>(null);

  showCompletionModal = signal(false);
  completedRideInfo = signal<{
    distance: number;
    duration: number;
    price: number;
  } | null>(null);

  rideDetails = signal<RideDetails>({
    id: 1,
    pickup: 'Loading...',
    destination: 'Loading...',
    pickupCoords: [45.2671, 19.8335],
    destinationCoords: [45.2550, 19.8450],
    driverName: '',
    vehicleType: '',
    licensePlate: '',
    totalDistance: 0,
    estimatedDuration: 0,
    passengers: []
  });

  vehicleLocation = signal<Location>({
    lat: 45.2671,
    lng: 19.8335
  });

  vehiclesForMap = signal<Vehicle[]>([]);

  progressPercentage = computed(() => {
    const total = this.rideDetails().totalDistance || 1;
    const remaining = this.remainingDistance();
    return Math.round(((total - remaining) / total) * 100);
  });

  private updateInterval: any = null;
  private routePoints: L.LatLng[] = [];
  private currentPointIndex = 0;
  private rideId: number | null = null;
  private vehicleId: number | null = null;
  private routingControl: any = null;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private rideService: RideService,
    private vehicleService: VehicleService,
    private pricelistService: PricelistService,
    private mapService: MapService
  ) {}

  ngOnInit(): void {
    this.pricelistService.getCurrentPricing().subscribe(p => {
      this.pricing.set(p);
    });

    this.route.params.subscribe(params => {
      this.rideId = +params['id'] || 6;
      this.loadRideData(this.rideId);
    });
  }

  ngOnDestroy(): void {
    this.stopTracking();
    const map = this.mapService.getMap();
    if (map && this.routingControl) {
      map.removeControl(this.routingControl);
    }
  }

  private loadRideData(id: number): void {
    this.rideService.trackRide(id).subscribe(res => {

      const route = res.route;

      if (res.currentLocation?.vehicleId) {
        this.vehicleId = res.currentLocation.vehicleId;
      }

      this.rideDetails.set({
        id: res.rideId,
        pickup: route?.pickupAddress || '',
        destination: route?.destinationAddress || '',
        pickupCoords: route
          ? [route.pickupLatitude, route.pickupLongitude]
          : [45.26,19.83],
        destinationCoords: route
          ? [route.destinationLatitude, route.destinationLongitude]
          : [45.25,19.84],
        driverName: res.driver
          ? `${res.driver.firstName} ${res.driver.lastName}`
          : '',
        vehicleType: res.currentLocation?.model || '',
        licensePlate: res.currentLocation?.plateNum || '',
        totalDistance: route?.distanceKm || 0,
        estimatedDuration: route
          ? Math.round(route.estimatedTimeSeconds / 60)
          : 0,
        passengers: []
      });

      this.remainingDistance.set(Number(route?.distanceKm?.toFixed(2) ?? 0));
      this.estimatedArrival.set(res.estimatedTimeMinutes || 0);

      if (res.currentLocation) {
        this.vehicleLocation.set({
          lat: res.currentLocation.latitude,
          lng: res.currentLocation.longitude
        });
      }

      setTimeout(() => this.drawOsrmRoute(), 100);
    });
  }

  private drawOsrmRoute(): void {
    const map = this.mapService.getMap();
    if (!map) return;

    const ride = this.rideDetails();
    const pickup = L.latLng(ride.pickupCoords[0], ride.pickupCoords[1]);
    const dest = L.latLng(ride.destinationCoords[0], ride.destinationCoords[1]);

    if (this.routingControl) {
      map.removeControl(this.routingControl);
    }

    this.routingControl = L.Routing.control({
      waypoints: [pickup, dest],
      router: L.Routing.osrmv1({
        serviceUrl: 'https://router.project-osrm.org/route/v1'
      }),
      show: false,
      addWaypoints: false,
      fitSelectedRoutes: true,
      createMarker: () => null,
      lineOptions: {
        styles: [{ color: '#00acc1', weight: 5 }]
      }
    } as any).addTo(map);

    this.routingControl.on('routesfound', (e: any) => {
      const coords = e.routes[0].coordinates;
      this.routePoints = coords.map((c: any) => L.latLng(c.lat, c.lng));
      this.currentPointIndex = 0;

      this.mapService.drawRoute(this.routePoints);

      if (this.routePoints.length) {
        const start = this.routePoints[0];
        this.vehicleLocation.set({ lat: start.lat, lng: start.lng });
        this.updateVehicleOnMap(start.lat, start.lng);
        this.startTracking();
      }
    });
  }

  private startTracking(): void {
    this.stopTracking();
    this.updateInterval = setInterval(
      () => this.updateVehiclePosition(),
      2000
    );
  }

  private stopTracking(): void {
    if (this.updateInterval) {
      clearInterval(this.updateInterval);
      this.updateInterval = null;
    }
  }

  private updateVehiclePosition(): void {
    if (!this.routePoints.length) return;

    this.currentPointIndex += 5;

    if (this.currentPointIndex >= this.routePoints.length) {
      this.currentPointIndex = this.routePoints.length - 1;
    }

    const next = this.routePoints[this.currentPointIndex];

    this.vehicleLocation.set({
      lat: next.lat,
      lng: next.lng
    });

    this.updateVehicleOnMap(next.lat, next.lng);

    const ride = this.rideDetails();
    const progress = this.currentPointIndex / (this.routePoints.length - 1);

    this.remainingDistance.set(
      Number((ride.totalDistance * (1 - progress)).toFixed(2))
    );

    this.estimatedArrival.set(
      Math.max(0, Math.ceil(ride.estimatedDuration * (1 - progress)))
    );

    if (this.currentPointIndex >= this.routePoints.length - 1) {
      this.stopTracking();
      this.showRideCompletionModal();
    }
  }

    private updateVehicleOnMap(lat: number, lng: number): void {
    const ride = this.rideDetails();

    const vehicle: Vehicle = {
      vehicleId: this.vehicleId ?? 0,
      model: ride.vehicleType || 'STANDARD',
      plateNum: ride.licensePlate || '',
      latitude: lat,
      longitude: lng,
      available: false,
      specification: {
        type: 'STANDARD',
        seats: 4,
        babyTransport: false,
        petTransport: false
      }
    };

    this.vehiclesForMap.set([vehicle]);

    this.mapService.updateVehicleMarkers([vehicle]);

    if (this.vehicleId) {
      this.vehicleService.updateVehicleLocation(this.vehicleId, {
        latitude: lat,
        longitude: lng
      }).subscribe();
    }
  }

  private showRideCompletionModal(): void {
    if (!this.rideId) return;

    const ride = this.rideDetails();
    const pricing = this.pricing();

    let base = 150;
    let perKm = 80;

    if (pricing) {
      base = pricing.standardPrice;
      perKm = pricing.kmPrice;
    }

    const final = base + ride.totalDistance * perKm;

    this.completedRideInfo.set({
      distance: ride.totalDistance,
      duration: ride.estimatedDuration,
      price: Math.round(final)
    });

    this.rideService.completeRide(this.rideId)
      .subscribe(() => this.showCompletionModal.set(true));
  }

  triggerPanic(): void {
    if (this.panicTriggered()) return;

    this.rideService.ridePanic({
      vehicleLocation: this.vehicleLocation()
    }).subscribe(() => {
      this.panicTriggered.set(true);
      this.stopTracking();
    });
  }

  triggerStop(): void {
    if (this.stopTriggered()) return;

    this.rideService.stopRide({
      location: this.vehicleLocation()
    }).subscribe((res: StopRideResponse) => {
      this.stopTriggered.set(true);
      this.stopTracking();
      console.log(res.updatedPrice);
    });
  }

  handleBack(): void {
    this.router.navigate(['/driver/home']);
  }

  closeCompletionModal(): void {
    this.showCompletionModal.set(false);
  }

  closeAndNavigateHome(): void {
    this.closeCompletionModal();
    this.handleBack();
  }
}
