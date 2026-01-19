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

interface RideDetails {
  id: string;
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

interface VehiclePosition {
  lat: number;
  lng: number;
  timestamp: Date;
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

  estimatedArrival = signal<number>(8);
  remainingDistance = signal<number>(2.3);

  // Mock ride details
  rideDetails = signal<RideDetails>({
    id: 'ride-123',
    pickup: 'Trg Slobode 1',
    destination: 'Kisačka 71',
    pickupCoords: [45.2671, 19.8335],
    destinationCoords: [45.2550, 19.8450],
    driverName: 'Marko Petrović',
    vehicleType: 'Economy - Toyota Corolla',
    licensePlate: 'NS-123-AB',
    totalDistance: 2.3,
    estimatedDuration: 8
  });

  // Current vehicle position (simulated)
  vehiclePosition = signal<VehiclePosition>({
    lat: 45.2671,
    lng: 19.8335,
    timestamp: new Date()
  });

  progressPercentage = computed(() => {
    const total = this.rideDetails().totalDistance;
    const remaining = this.remainingDistance();
    return Math.round(((total - remaining) / total) * 100);
  });

  private map: L.Map | null = null;
  private routeControl: any = null;
  private vehicleMarker: L.Marker | null = null;
  private updateInterval: any = null;
  private routePoints: L.LatLng[] = [];
  private currentPointIndex = 0;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute
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
    // Initialize map after view is ready
    setTimeout(() => this.initMap(), 100);
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

  private initMap(): void {
    const mapElement = document.getElementById('trackingMap');
    if (!mapElement) return;

    const ride = this.rideDetails();

    // Calculate center point
    const centerLat = (ride.pickupCoords[0] + ride.destinationCoords[0]) / 2;
    const centerLng = (ride.pickupCoords[1] + ride.destinationCoords[1]) / 2;

    this.map = L.map('trackingMap').setView([centerLat, centerLng], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    // Create custom icons
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

    // Create vehicle icon
    const vehicleIcon = this.createVehicleIcon(false);

    // Create routing control
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

    // Extract route points once routing is complete
    this.routeControl.on('routesfound', (e: any) => {
      const routes = e.routes;
      if (routes && routes.length > 0) {
        const route = routes[0];
        this.routePoints = route.coordinates || [];

        // Start vehicle at the first point
        if (this.routePoints.length > 0) {
          const startPoint = this.routePoints[0];
          this.vehiclePosition.set({
            lat: startPoint.lat,
            lng: startPoint.lng,
            timestamp: new Date()
          });

          // Add vehicle marker at starting position
          this.vehicleMarker = L.marker([startPoint.lat, startPoint.lng], {
            icon: vehicleIcon,
            zIndexOffset: 1000
          }).addTo(this.map!);

          this.vehicleMarker.bindPopup(`<b>Driver Location</b><br>${ride.driverName}`);

          // Start tracking after route is loaded
          this.startTracking();
        }
      }
    });
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
    // Simulate vehicle movement every 5 seconds
    this.updateInterval = setInterval(() => {
      this.updateVehiclePosition();
    }, 5000);
  }

  private stopTracking(): void {
    if (this.updateInterval) {
      clearInterval(this.updateInterval);
      this.updateInterval = null;
    }
  }

  private updateVehiclePosition(): void {
    if (this.routePoints.length === 0) return;

    // Move to next point in the route
    this.currentPointIndex++;

    // Calculate how many points to skip for realistic speed
    // Skip 3-5 points per update for faster movement
    const pointsToSkip = 4;
    this.currentPointIndex = Math.min(
      this.currentPointIndex + pointsToSkip,
      this.routePoints.length - 1
    );

    if (this.currentPointIndex >= this.routePoints.length - 1) {
      // Reached destination
      this.stopTracking();
      this.remainingDistance.set(0);
      this.estimatedArrival.set(0);
      return;
    }

    const nextPoint = this.routePoints[this.currentPointIndex];

    // Update position
    this.vehiclePosition.set({
      lat: nextPoint.lat,
      lng: nextPoint.lng,
      timestamp: new Date()
    });

    // Update marker on map
    if (this.vehicleMarker) {
      this.vehicleMarker.setLatLng([nextPoint.lat, nextPoint.lng]);
    }

    // Calculate remaining distance based on progress
    const ride = this.rideDetails();
    const progress = this.currentPointIndex / this.routePoints.length;
    const remainingDist = ride.totalDistance * (1 - progress);
    const remainingTime = ride.estimatedDuration * (1 - progress);

    this.remainingDistance.set(Math.max(0, parseFloat(remainingDist.toFixed(2))));
    this.estimatedArrival.set(Math.max(0, Math.ceil(remainingTime)));
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

    const reportData = {
      rideId: this.rideDetails().id,
      description: this.reportForm.value.description,
      timestamp: new Date(),
      vehiclePosition: this.vehiclePosition()
    };

    // Simulate API call
    setTimeout(() => {
      console.log('Submitting route inconsistency report:', reportData);

      // In real app, call report service:
      // this.reportService.submitInconsistency(reportData).subscribe(...)

      this.isSubmittingReport.set(false);
      this.reportSubmitted.set(true);
      this.showReportForm.set(false);
      this.reportForm.reset();

      // Hide success message after 5 seconds
      setTimeout(() => {
        this.reportSubmitted.set(false);
      }, 5000);
    }, 1500);
  }

  /**
   * Triggers panic alert - sends emergency notification to central dispatch
   * and updates vehicle marker to red with pulsing animation
   */
  triggerPanic(): void {
    if (this.panicTriggered()) {
      return; // Already triggered
    }

    this.panicTriggered.set(true);

    const panicData = {
      rideId: this.rideDetails().id,
      passengerId: 'current-user-id', // TODO: Get from auth service
      driverId: 'driver-id', // TODO: Get from ride details
      timestamp: new Date(),
      location: this.vehiclePosition(),
      vehicleInfo: {
        type: this.rideDetails().vehicleType,
        licensePlate: this.rideDetails().licensePlate,
        driverName: this.rideDetails().driverName
      }
    };

    console.log('🚨 PANIC ALERT TRIGGERED:', panicData);

    // Update vehicle marker to emergency state (red with pulse)
    if (this.vehicleMarker && this.map) {
      const panicIcon = this.createVehicleIcon(true);
      this.vehicleMarker.setIcon(panicIcon);

      // Update popup to show emergency state
      this.vehicleMarker.setPopupContent(
        `<b style="color: #dc2626;">⚠️ EMERGENCY ALERT</b><br>${this.rideDetails().driverName}`
      );
      this.vehicleMarker.openPopup();
    }

    // TODO: Send panic alert to backend
    // this.emergencyService.triggerPanic(panicData).subscribe({
    //   next: (response) => {
    //     console.log('Panic alert sent successfully:', response);
    //   },
    //   error: (error) => {
    //     console.error('Failed to send panic alert:', error);
    //     // Show error notification to user
    //   }
    // });

    // TODO: Establish WebSocket connection for real-time emergency tracking
    // this.websocketService.sendEmergencyAlert(panicData);

    // TODO: Trigger sound/visual alerts on admin dashboard
    // This will be handled by the backend sending notifications to all admins
  }

  handleBack(): void {
    this.router.navigate(['/passenger/home']);
  }
}
