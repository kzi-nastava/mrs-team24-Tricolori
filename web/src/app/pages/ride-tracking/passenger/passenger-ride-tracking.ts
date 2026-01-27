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
  heroExclamationCircle
} from '@ng-icons/heroicons/outline';
import * as L from 'leaflet';
import 'leaflet-routing-machine';
import { forkJoin } from 'rxjs';

import { RideService, RideDetailResponse } from '../../../services/ride.service';
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
      heroExclamationCircle
    })
  ],
  templateUrl: './passenger-ride-tracking.html'
})
export class PassengerRideTrackingComponent implements OnInit, OnDestroy {
  reportForm: FormGroup;
  showReportForm = signal<boolean>(false);
  isSubmittingReport = signal<boolean>(false);
  reportSubmitted = signal<boolean>(false);
  panicTriggered = signal<boolean>(false);

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
  private updateInterval: any = null;
  private isInitialized = false;

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
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.rideId = +params['id'];
      if (this.rideId) {
        this.loadInitialData();
        this.startTracking();
      }
    });
  }

  ngOnDestroy(): void {
    this.stopTracking();

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
   * Load initial data - combine ride details and tracking
   */
  private loadInitialData(): void {
    forkJoin({
      details: this.rideService.getPassengerRideDetail(this.rideId),
      tracking: this.rideService.trackRide(this.rideId)
    }).subscribe({
      next: (result) => {
        this.updateRideDetailsFromDetail(result.details);
        this.updateTrackingData(result.tracking);
        
        if (!this.isInitialized) {
          setTimeout(() => this.initMap(), 100);
          this.isInitialized = true;
        }
      },
      error: (error) => {
        console.error('Error loading initial ride data:', error);
      }
    });
  }

  /**
   * Update ride details from detail response (static info)
   */
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
      estimatedDuration: detail.duration
    });
  }

  /**
   * Poll tracking data (called repeatedly)
   */
  private loadTrackingData(): void {
    this.rideService.trackRide(this.rideId).subscribe({
      next: (response: RideTrackingResponse) => {
        this.updateTrackingData(response);
        this.updateVehiclePosition();
      },
      error: (error) => {
        console.error('Error loading tracking data:', error);
      }
    });
  }

  /**
   * Update tracking data (dynamic info - location, ETA, etc.)
   */
  private updateTrackingData(response: RideTrackingResponse): void {
    // Update estimated arrival
    this.estimatedArrival.set(response.estimatedTimeMinutes || 0);
    
    // Calculate remaining distance
    // Use route data from backend if available, otherwise estimate from time
    let totalDistance = this.rideDetails().totalDistance;
    
    // If route is present in tracking response, use its distance
    if (response.route && response.route.distanceKm) {
      totalDistance = response.route.distanceKm;
      
      // Update ride details with route info if we have it
      if (response.route.pickupAddress && response.route.destinationAddress) {
        this.rideDetails.update(current => ({
          ...current,
          pickup: response.route!.pickupAddress,
          destination: response.route!.destinationAddress,
          pickupCoords: [response.route!.pickupLatitude, response.route!.pickupLongitude],
          destinationCoords: [response.route!.destinationLatitude, response.route!.destinationLongitude],
          totalDistance: response.route!.distanceKm
        }));
      }
    }

    // Calculate remaining distance based on time progress
    if (totalDistance > 0 && this.rideDetails().estimatedDuration > 0) {
      const timeElapsed = this.rideDetails().estimatedDuration - (response.estimatedTimeMinutes || 0);
      const progressPercent = (timeElapsed / this.rideDetails().estimatedDuration) * 100;
      const remainingDist = totalDistance * (1 - progressPercent / 100);
      this.remainingDistance.set(Math.max(0, parseFloat(remainingDist.toFixed(2))));
    }

    // Update vehicle location
    if (response.currentLocation) {
      this.vehicleLocation.set({
        lat: response.currentLocation.latitude,
        lng: response.currentLocation.longitude
      });

      // Update vehicle info
      this.rideDetails.update(current => ({
        ...current,
        licensePlate: response.currentLocation?.plateNum || current.licensePlate,
        vehicleType: response.currentLocation?.model || current.vehicleType
      }));
    }

    // Update driver info if present
    if (response.driver) {
      this.rideDetails.update(current => ({
        ...current,
        driverName: `${response.driver!.firstName} ${response.driver!.lastName}`
      }));
    }

    // Check for panic mode
    if (response.status === 'PANIC' && !this.panicTriggered()) {
      this.panicTriggered.set(true);
      this.stopTracking();
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

    const vehicleIcon = this.createVehicleIcon(this.panicTriggered());

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

    const vehicleLoc = this.vehicleLocation();
    if (vehicleLoc.lat !== 0 && vehicleLoc.lng !== 0) {
      this.vehicleMarker = L.marker([vehicleLoc.lat, vehicleLoc.lng], {
        icon: vehicleIcon,
        zIndexOffset: 1000
      }).addTo(this.map!);

      this.vehicleMarker.bindPopup(`<b>Driver Location</b><br>${ride.driverName}`);
    }
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