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

  drivers = signal<Driver[]>([
    {
      id: 1,
      name: 'Marko Petrović',
      phone: '+381 64 123 4567',
      vehicleType: 'Economy - Toyota Corolla',
      licensePlate: 'NS-123-AB',
      status: 'active',
      currentPosition: [45.2650, 19.8370],
      currentRide: {
        id: 12345,
        passengerName: 'Ana Jovanović',
        pickup: 'Trg Slobode 1',
        destination: 'Kisačka 71',
        pickupCoords: [45.2671, 19.8335],
        destinationCoords: [45.2550, 19.8450],
        departureTime: '14:30',
        estimatedArrival: '14:38',
        distance: 2.3,
        price: 232.70,
        progress: 45
      }
    },
    {
      id: 2,
      name: 'Stefan Nikolić',
      phone: '+381 65 555 1234',
      vehicleType: 'Premium - Mercedes E-Class',
      licensePlate: 'NS-789-EF',
      status: 'active',
      currentPosition: [45.2580, 19.8420],
      currentRide: {
        id: 12346,
        passengerName: 'Milica Stojanović',
        pickup: 'Bulevar Oslobođenja 45',
        destination: 'Novi Sad Fair',
        pickupCoords: [45.2600, 19.8400],
        destinationCoords: [45.2450, 19.8500],
        departureTime: '14:25',
        estimatedArrival: '14:40',
        distance: 3.5,
        price: 350.00,
        progress: 60
      }
    },
    {
      id: 3,
      name: 'Ana Jovanović',
      phone: '+381 63 987 6543',
      vehicleType: 'Comfort - Honda Accord',
      licensePlate: 'BG-456-CD',
      status: 'active',
      currentPosition: [45.2620, 19.8300],
      currentRide: {
        id: 12347,
        passengerName: 'Petar Jović',
        pickup: 'Železnička stanica',
        destination: 'Limanski park',
        pickupCoords: [45.2640, 19.8280],
        destinationCoords: [45.2500, 19.8350],
        departureTime: '14:20',
        estimatedArrival: '14:35',
        distance: 2.8,
        price: 280.00,
        progress: 75
      }
    },
    {
      id: 4,
      name: 'Jelena Đorđević',
      phone: '+381 64 222 3333',
      vehicleType: 'XL - Toyota Sienna',
      licensePlate: 'BG-321-GH',
      status: 'idle',
      currentPosition: [45.2700, 19.8400]
    },
    {
      id: 5,
      name: 'Milan Stojanović',
      phone: '+381 66 777 8888',
      vehicleType: 'Economy - Volkswagen Golf',
      licensePlate: 'NS-654-IJ',
      status: 'idle',
      currentPosition: [45.2550, 19.8300]
    }
  ]);

  filteredDrivers = computed(() => {
    if (!this.searchQuery.trim()) {
      return this.drivers().filter(d => d.status === 'active');
    }
    return this.drivers().filter(d =>
      d.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
      d.licensePlate.toLowerCase().includes(this.searchQuery.toLowerCase())
    );
  });

  ngOnInit(): void {
    setTimeout(() => this.initMap(), 100);
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

  private initMap(): void {
    const mapElement = document.getElementById('supervisionMap');
    if (!mapElement) return;

    // Center on Novi Sad
    this.map = L.map('supervisionMap').setView([45.2671, 19.8335], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    // Add all active drivers to map
    this.updateDriverMarkers();
  }

  private updateDriverMarkers(): void {
    if (!this.map) return;

    // Clear existing markers
    this.driverMarkers.forEach(marker => marker.remove());
    this.driverMarkers.clear();

    // Add markers for all drivers
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
      // Simulate position updates
      this.drivers.update(drivers =>
        drivers.map(driver => {
          if (driver.status === 'active' && driver.currentRide) {
            // Move driver slightly towards destination
            const progress = Math.min(driver.currentRide.progress + 2, 100);
            return {
              ...driver,
              currentRide: {
                ...driver.currentRide,
                progress
              }
            };
          }
          return driver;
        })
      );

      this.updateDriverMarkers();
    }, 5000);
  }

  private stopLiveUpdates(): void {
    if (this.updateInterval) {
      clearInterval(this.updateInterval);
    }
  }

  onSearchChange(): void {
    // Trigger filtering
    this.filteredDrivers();
  }

  selectDriver(driver: Driver): void {
    this.selectedDriver.set(driver);

    if (this.map && driver.currentRide) {
      // Center map on driver
      this.map.setView(driver.currentPosition, 14);

      // Draw route
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

    // Remove existing route
    if (this.routeControl) {
      this.map.removeControl(this.routeControl);
    }

    // Create icons
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

    // Create route
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