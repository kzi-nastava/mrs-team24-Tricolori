import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import {
  heroEye,
  heroMagnifyingGlass,
  heroTruck,
  heroUserGroup,
  heroMapPin,
  heroPhone,
  heroChatBubbleLeft,
  heroUser,
  heroXMark,
  heroArrowLeft
} from '@ng-icons/heroicons/outline';
import * as L from 'leaflet';
import 'leaflet-routing-machine';
import { RideService } from '../../services/ride.service';
import { RideTrackingResponse } from '../../model/ride-tracking';

interface CurrentRide {
  id: number;
  passengerName: string;
  pickup: string;
  destination: string;
  pickupCoords: [number, number];
  destinationCoords: [number, number];
  departureTime: string;
  estimatedArrival: string;
  distance: number;
  price: number;
  progress: number;
}

interface Driver {
  id: number;
  name: string;
  phone: string;
  vehicleType: string;
  licensePlate: string;
  status: 'active' | 'idle';
  currentPosition: [number, number];
  currentRide?: CurrentRide;
}

@Component({
  selector: 'app-admin-ride-supervision',
  standalone: true,
  imports: [CommonModule, FormsModule, NgIconComponent],
  providers: [
    provideIcons({
      heroEye,
      heroMagnifyingGlass,
      heroTruck,
      heroUserGroup,
      heroMapPin,
      heroPhone,
      heroChatBubbleLeft,
      heroUser,
      heroXMark,
      heroArrowLeft
    })
  ],
  templateUrl: './admin-ride-supervision.html'
})
export class AdminRideSupervisionComponent implements OnInit, OnDestroy {
  searchQuery = '';
  selectedDriver = signal<Driver | null>(null);

  private map: L.Map | null = null;
  private driverMarkers: Map<number, L.Marker> = new Map();
  private routeControl: any = null;
  private updateInterval: any = null;

  drivers = signal<Driver[]>([]);

  filteredDrivers = computed(() => {
    if (!this.searchQuery.trim()) {
      return this.drivers().filter(d => d.status === 'active');
    }
    return this.drivers().filter(d =>
      d.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
      d.licensePlate.toLowerCase().includes(this.searchQuery.toLowerCase())
    );
  });

  constructor(private rideService: RideService) {}

  ngOnInit(): void {
    setTimeout(() => this.initMap(), 100);
    this.loadOngoingRides();
    this.startLiveUpdates();
  }

  ngOnDestroy(): void {
    this.stopLiveUpdates();
    if (this.routeControl && this.map) {
      this.map.removeControl(this.routeControl);
    }
    if (this.map) {
      this.map.remove();
    }
  }

  private loadOngoingRides(): void {
    this.rideService.getAllOngoingRides().subscribe({
      next: (rides: RideTrackingResponse[]) => {
        const mappedDrivers = rides.map(ride => this.mapRideToDriver(ride));
        this.drivers.set(mappedDrivers);
        this.updateDriverMarkers();
      },
      error: (error) => {
        console.error('Error loading ongoing rides:', error);
      }
    });
  }

  private mapRideToDriver(ride: RideTrackingResponse): Driver {
    // Extract driver information
    const driverName = ride.driver 
      ? `${ride.driver.firstName} ${ride.driver.lastName}`
      : 'Unknown Driver';
    
    const vehicleType = ride.currentLocation
      ? `${ride.currentLocation.model}`
      : 'Unknown Vehicle';
    
    const licensePlate = ride.currentLocation?.plateNum || 'N/A';
    
    // Use vehicle current location if available, otherwise pickup location
    const currentPosition: [number, number] = ride.currentLocation
      ? [ride.currentLocation.latitude, ride.currentLocation.longitude]
      : ride.route 
        ? [ride.route.pickupLatitude, ride.route.pickupLongitude]
        : [45.2671, 19.8335]; // Default to Novi Sad center

    // Get main passenger name
    const mainPassenger = ride.passengers && ride.passengers.length > 0 
      ? ride.passengers[0]
      : null;
    const passengerName = mainPassenger 
      ? `${mainPassenger.firstName} ${mainPassenger.lastName}`
      : 'Unknown Passenger';

    // Calculate progress based on time
    let progress = 0;
    if (ride.startTime && ride.estimatedTimeMinutes) {
      const startTime = new Date(ride.startTime).getTime();
      const now = Date.now();
      const totalSeconds = ride.estimatedTimeMinutes * 60;
      const elapsedSeconds = (now - startTime) / 1000;
      progress = Math.min(100, Math.max(0, Math.round((elapsedSeconds / totalSeconds) * 100)));
    }

    return {
      id: ride.driver?.id || 0,
      name: driverName,
      phone: ride.driver?.phoneNumber || 'N/A',
      vehicleType: vehicleType,
      licensePlate: licensePlate,
      status: 'active',
      currentPosition: currentPosition,
      currentRide: {
        id: ride.rideId,
        passengerName: passengerName,
        pickup: ride.route?.pickupAddress || 'Unknown',
        destination: ride.route?.destinationAddress || 'Unknown',
        pickupCoords: ride.route 
          ? [ride.route.pickupLatitude, ride.route.pickupLongitude]
          : [45.2671, 19.8335],
        destinationCoords: ride.route
          ? [ride.route.destinationLatitude, ride.route.destinationLongitude]
          : [45.2671, 19.8335],
        departureTime: this.formatTime(ride.startTime),
        estimatedArrival: this.formatTime(ride.estimatedArrival),
        distance: ride.route?.distanceKm ? Math.round(ride.route.distanceKm * 10) / 10 : 0,
        price: ride.price || 0,
        progress: progress
      }
    };
  }

  private formatTime(dateTimeString: string | null): string {
    if (!dateTimeString) return 'N/A';
    const date = new Date(dateTimeString);
    return date.toLocaleTimeString('sr-RS', { hour: '2-digit', minute: '2-digit' });
  }

  private initMap(): void {
    const mapElement = document.getElementById('supervisionMap');
    if (!mapElement) return;

    this.map = L.map('supervisionMap').setView([45.2671, 19.8335], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    this.updateDriverMarkers();
  }

  private updateDriverMarkers(): void {
    if (!this.map) return;

    this.driverMarkers.forEach(marker => marker.remove());
    this.driverMarkers.clear();

    this.drivers().forEach(driver => {
      const icon = L.divIcon({
        className: 'driver-marker',
        html: `<div style="background: ${driver.status === 'active' ? '#10b981' : '#9ca3af'}; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white; box-shadow: 0 3px 6px rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center;">
          <svg xmlns="http://www.w3.org/2000/svg" fill="white" viewBox="0 0 24 24" width="12" height="12">
            <path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z"/>
          </svg>
        </div>`,
        iconSize: [26, 26],
        iconAnchor: [13, 13]
      });

      const marker = L.marker(driver.currentPosition, { icon }).addTo(this.map!);
      marker.bindPopup(`<b>${driver.name}</b><br>${driver.vehicleType}<br>${driver.licensePlate}`);
      
      this.driverMarkers.set(driver.id, marker);
    });
  }

  private startLiveUpdates(): void {
    this.updateInterval = setInterval(() => {
      this.loadOngoingRides();
    }, 5000);
  }

  private stopLiveUpdates(): void {
    if (this.updateInterval) {
      clearInterval(this.updateInterval);
    }
  }

  onSearchChange(): void {
    this.filteredDrivers();
  }

  selectDriver(driver: Driver): void {
    this.selectedDriver.set(driver);

    if (this.map && driver.currentRide) {
      this.map.setView(driver.currentPosition, 14);
      this.drawRoute(driver.currentRide);
    }
  }

  deselectDriver(): void {
    this.selectedDriver.set(null);
    if (this.routeControl && this.map) {
      this.map.removeControl(this.routeControl);
      this.routeControl = null;
    }
  }

  private drawRoute(ride: CurrentRide): void {
    if (!this.map) return;

    if (this.routeControl) {
      this.map.removeControl(this.routeControl);
    }

    const pickupIcon = L.divIcon({
      className: 'custom-marker-icon',
      html: `<div style="background: #14b8a6; width: 16px; height: 16px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
      iconSize: [22, 22],
      iconAnchor: [11, 11]
    });

    const destinationIcon = L.divIcon({
      className: 'custom-marker-icon',
      html: `<div style="background: #ef4444; width: 16px; height: 16px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
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
        styles: [{ color: '#14b8a6', opacity: 0.7, weight: 4 }],
        extendToWaypoints: false,
        missingRouteTolerance: 0
      },
      show: false,
      addWaypoints: false,
      fitSelectedRoutes: true,
      createMarker: (i: number, waypoint: any) => {
        const icon = i === 0 ? pickupIcon : destinationIcon;
        return L.marker(waypoint.latLng, { icon });
      }
    } as any).addTo(this.map);
  }

  getActiveRides(): Driver[] {
    return this.drivers().filter(d => d.status === 'active' && d.currentRide);
  }
}