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
import { PanicRideRequest } from '../../../model/ride-tracking';


@Component({
  selector: 'app-driver-ride-tracking',
  standalone: true,
  imports: [CommonModule, NgIconComponent],
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
  panicTriggered = signal<boolean>(false);

  estimatedArrival = signal<number>(8);
  remainingDistance = signal<number>(2.3);

  // Modal state
  showCompletionModal = signal<boolean>(false);
  completedRideInfo = signal<{
    distance: number;
    duration: number;
    price: number;
  } | null>(null);

  // Ride details - will be loaded from backend
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

  // Current vehicle position (simulated)
  vehicleLocation = signal<Location>({
    lat: 45.2671,
    lng: 19.8335,
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
  private rideId: number | null = null;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private rideService: RideService
  ) {}

  ngOnInit(): void {
    // Get ride ID from route params, default to 6 if not provided
    this.route.params.subscribe(params => {
      console.log('🚀 Component initialized');
      console.log('📋 Route params:', params);
      this.rideId = +params['id'] || 6;
      console.log('🎯 Using ride ID:', this.rideId);
      this.loadRideData(this.rideId);
    });
  }

  /**
   * Load ride data from backend
   */
  private loadRideData(rideId: number): void {
    console.log('🔍 Loading ride data for ride ID:', rideId);
    
    this.rideService.trackRide(rideId).subscribe({
      next: (response) => {
        console.log('✅ Received ride tracking response:', response);
        console.log('📍 Response details:');
        console.log('  - rideId:', response.rideId);
        console.log('  - status:', response.status);
        console.log('  - route:', response.route);
        console.log('  - currentLocation:', response.currentLocation);
        console.log('  - driver:', response.driver);
        console.log('  - passengers:', response.passengers);
        console.log('  - estimatedTimeMinutes:', response.estimatedTimeMinutes);
        console.log('  - price:', response.price);
        
        // Extract pickup and destination from route DTO
        const route = response.route;
        console.log('🗺️ Route data:', {
          pickupAddress: route?.pickupAddress,
          destinationAddress: route?.destinationAddress,
          distanceKm: route?.distanceKm,
          estimatedTimeSeconds: route?.estimatedTimeSeconds
        });

        // If route is null (finished ride), load full details instead
        if (!route && (response.status === 'FINISHED' || response.status === 'CANCELLED_BY_DRIVER' || response.status === 'CANCELLED_BY_PASSENGER')) {
          console.log('⚠️ Route is null, loading full ride details instead...');
          this.loadFinishedRideDetails(rideId, response);
          return;
        }

        // Update ride details with real data from RideTrackingResponse
        const rideDetails = {
          id: response.rideId,
          pickup: route?.pickupAddress || 'Pickup location',
          destination: route?.destinationAddress || 'Destination',
          pickupCoords: route 
            ? [route.pickupLatitude, route.pickupLongitude] 
            : [45.2671, 19.8335],
          destinationCoords: route 
            ? [route.destinationLatitude, route.destinationLongitude] 
            : [45.2550, 19.8450],
          driverName: response.driver 
            ? `${response.driver.firstName} ${response.driver.lastName}` 
            : '',
          vehicleType: response.currentLocation?.model || '',
          licensePlate: response.currentLocation?.plateNum || '',
          totalDistance: route?.distanceKm || 0,
          estimatedDuration: route ? Math.round(route.estimatedTimeSeconds / 60) : 0,
          passengers: response.passengers?.map(p => ({
            id: p.id,
            name: `${p.firstName} ${p.lastName}`,
            phone: p.phoneNumber,
            email: p.email || ''
          })) || []
        };
        
        console.log('🚗 Mapped ride details:', rideDetails);
        this.rideDetails.set(rideDetails);

        // Set initial values
        const remainingDist = route?.distanceKm || 0;
        const estimatedArr = response.estimatedTimeMinutes || 0;
        
        console.log('⏱️ Setting initial values:');
        console.log('  - remainingDistance:', remainingDist);
        console.log('  - estimatedArrival:', estimatedArr);
        
        this.remainingDistance.set(remainingDist);
        this.estimatedArrival.set(estimatedArr);

        // Update current vehicle location if available
        if (response.currentLocation) {
          const vehicleLoc = {
            lat: response.currentLocation.latitude,
            lng: response.currentLocation.longitude
          };
          console.log('📍 Setting vehicle location:', vehicleLoc);
          this.vehicleLocation.set(vehicleLoc);
        } else {
          console.warn('⚠️ No current location in response');
        }

        // Initialize map after data is loaded
        console.log('🗺️ Initializing map...');
        setTimeout(() => this.initMap(), 100);
      },
      error: (err) => {
        console.error('❌ Failed to load ride data:', err);
        console.error('Error details:', {
          status: err.status,
          message: err.message,
          error: err.error
        });
        // Initialize map with default/mock data as fallback
        setTimeout(() => this.initMap(), 100);
      }
    });
  }

  /**
   * Load full ride details for finished rides
   */
  private loadFinishedRideDetails(rideId: number, trackingResponse: any): void {
    console.log('📦 Loading full ride details for finished ride...');
    
    this.rideService.getDriverRideDetail(rideId).subscribe({
      next: (detail) => {
        console.log('✅ Received full ride details:', detail);
        
        const rideDetails = {
          id: detail.id,
          pickup: detail.pickupAddress,
          destination: detail.dropoffAddress,
          pickupCoords: [detail.pickupLatitude, detail.pickupLongitude],
          destinationCoords: [detail.dropoffLatitude, detail.dropoffLongitude],
          driverName: detail.driverName,
          vehicleType: detail.vehicleModel,
          licensePlate: detail.vehicleLicensePlate,
          totalDistance: detail.distance,
          estimatedDuration: detail.duration,
          passengers: [{
            id: 0,
            name: detail.passengerName,
            phone: detail.passengerPhone,
            email: ''
          }]
        };
        
        console.log('🚗 Mapped finished ride details:', rideDetails);
        this.rideDetails.set(rideDetails);

        // For finished rides, set remaining values to 0
        this.remainingDistance.set(0);
        this.estimatedArrival.set(0);

        // Use current location from tracking response if available
        if (trackingResponse.currentLocation) {
          this.vehicleLocation.set({
            lat: trackingResponse.currentLocation.latitude,
            lng: trackingResponse.currentLocation.longitude
          });
        }

        // Initialize map
        console.log('🗺️ Initializing map for finished ride...');
        setTimeout(() => this.initMap(), 100);
      },
      error: (err) => {
        console.error('❌ Failed to load finished ride details:', err);
        setTimeout(() => this.initMap(), 100);
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
          this.vehicleLocation.set({
            lat: startPoint.lat,
            lng: startPoint.lng,
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
    // Simulate vehicle movement every 1 second for faster testing
    this.updateInterval = setInterval(() => {
      this.updateVehiclePosition();
    }, 3000);
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

    // Calculate how many points to skip for faster testing
    // Skip more points per update for much faster movement (completes in ~15 seconds)
    const pointsToSkip = 15;
    this.currentPointIndex = Math.min(
      this.currentPointIndex + pointsToSkip,
      this.routePoints.length - 1
    );

    if (this.currentPointIndex >= this.routePoints.length - 1) {
      // Reached destination
      this.stopTracking();
      this.remainingDistance.set(0);
      this.estimatedArrival.set(0);
      
      // Show completion modal
      this.showRideCompletionModal();
      return;
    }

    const nextPoint = this.routePoints[this.currentPointIndex];

    // Update position
    this.vehicleLocation.set({
      lat: nextPoint.lat,
      lng: nextPoint.lng
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

  /**
   * Shows the ride completion modal with ride details and sends completion to backend
   */
  private showRideCompletionModal(): void {
    if (!this.rideId) {
      console.error('No ride ID available');
      return;
    }

    const ride = this.rideDetails();
    
    // Calculate final price (you can adjust this logic based on your pricing model)
    const basePrice = 150; // Base price in RSD
    const pricePerKm = 80;
    const finalPrice = basePrice + (ride.totalDistance * pricePerKm);

    // Set modal data
    this.completedRideInfo.set({
      distance: ride.totalDistance,
      duration: ride.estimatedDuration,
      price: Math.round(finalPrice)
    });

    // Call backend to complete the ride
    this.rideService.completeRide(this.rideId).subscribe({
      next: () => {
        console.log('✅ Ride completed successfully on backend');
        this.showCompletionModal.set(true);
      },
      error: (err) => {
        console.error('❌ Failed to complete ride on backend:', err);
        // Still show the modal even if backend call fails
        this.showCompletionModal.set(true);
      }
    });
  }

  /**
   * Closes the completion modal
   */
  closeCompletionModal(): void {
    this.showCompletionModal.set(false);
  }

  /**
   * Closes modal and navigates to home
   */
  closeAndNavigateHome(): void {
    this.closeCompletionModal();
    this.handleBack();
  }

  /**
   * Triggers panic alert - sends emergency notification to central dispatch
   * and updates vehicle marker to red with pulsing animation
   */
  triggerPanic(): void {
    if (this.panicTriggered()) {
      return; // Already triggered
    }

    const panicRequest: PanicRideRequest = { 
      vehicleLocation: {
        lat: this.vehicleLocation().lat,
        lng: this.vehicleLocation().lng
      }
    };

    this.rideService.ridePanic(this.rideDetails().id, panicRequest).subscribe({
      next: () => {
        this.panicTriggered.set(true);
        this.stopTracking();
        this.updateVehicleMarker();
        console.log('🚨 Panic request triggered!');
      },
      error: (err) => {
        console.error(err);
      }
    });
  }

  // Update vehicle marker to emergency state (red with pulse)
  private updateVehicleMarker() {
    if (this.vehicleMarker && this.map) {
      const panicIcon = this.createVehicleIcon(true);
      this.vehicleMarker.setIcon(panicIcon);

      // Update popup to show emergency state
      this.vehicleMarker.setPopupContent(
        `<b style="color: #dc2626;">⚠️ EMERGENCY ALERT</b><br>${this.rideDetails().driverName}`
      );
      this.vehicleMarker.openPopup();
    }
  }

  stopTriggered = signal<boolean>(false);

  triggerStop(): void {
    if (this.stopTriggered()) {
      return; // stop already triggered
    }

    const stopRideRequest: StopRideRequest = { 
      location: this.vehicleLocation() 
    };

    this.rideService.stopRide(this.rideDetails().id, stopRideRequest).subscribe({
      next: (response) => {
        this.handleStop(response);
        this.handleBack();
      },
      error: (err) => {
        console.error("Failed stopping the ride: ", err);
      }
    });
  }

  private handleStop(response: StopRideResponse): void {
    this.stopTriggered.set(true);
    this.stopTracking();
    this.estimatedArrival.set(0);
    this.remainingDistance.set(0);
    console.log('Successfully stopped the ride. Updated price: ', response.updatedPrice);
  }

  handleBack(): void {
    this.router.navigate(['/driver/home']);
  }
}