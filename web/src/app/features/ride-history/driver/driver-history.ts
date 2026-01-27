import { Component, OnInit, ChangeDetectorRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { heroEye, heroXMark } from '@ng-icons/heroicons/outline';
import { finalize } from 'rxjs/operators';
import * as L from 'leaflet';

import {
  RideService,
  RideHistoryResponse,
  RideDetailResponse
} from '../../../core/services/ride.service';

interface Ride {
  id: number;
  route: string;
  startDate: string;
  endDate: string;
  price: number;
  status: 'Completed' | 'Cancelled' | 'Pending' | 'In Progress';
  startTime: string;
  endTime: string;
  duration: string;
  passengerName: string;
  passengerPhone: string;
  distance: number;
  paymentMethod: string;
  notes?: string;
  driverRating?: number | null;
  vehicleRating?: number | null;
  pickupLat?: number;
  pickupLng?: number;
  dropoffLat?: number;
  dropoffLng?: number;
}

@Component({
  selector: 'app-driver-history',
  standalone: true,
  imports: [CommonModule, FormsModule, NgIconComponent],
  providers: [provideIcons({ heroEye, heroXMark })],
  templateUrl: './driver-history.html',
  styleUrls: ['./driver-history.css']
})
export class DriverHistory implements OnInit {
  startDate = '';
  endDate = '';
  selectedRide: Ride | null = null;
  allRides: Ride[] = [];
  filteredRides: Ride[] = [];
  isLoading = false;
  errorMessage = '';
  
  // Filter options
  statusFilter: string = 'all';
  searchQuery: string = '';
  
  // Map
  private map: L.Map | null = null;

  constructor(
    private rideService: RideService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadRideHistory();
  }

  // ================= load history =================

  loadRideHistory(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.cdr.detectChanges();

    this.rideService
      .getDriverHistory(
        this.startDate || undefined,
        this.endDate || undefined
      )
      .pipe(
        finalize(() => {
          this.isLoading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (rides) => {
          this.allRides = this.mapBackendRidesToUI(rides);
          this.applyFilters();
          this.cdr.detectChanges();
        },
        error: () => {
          this.errorMessage = 'Failed to load ride history. Please try again.';
          this.allRides = [];
          this.filteredRides = [];
          this.cdr.detectChanges();
        }
      });
  }

  // ================= filtering =================

  applyFilters(): void {
    let result = [...this.allRides];

    // Status filter
    if (this.statusFilter !== 'all') {
      result = result.filter(ride => 
        ride.status.toLowerCase() === this.statusFilter.toLowerCase()
      );
    }

    // Search filter
    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      result = result.filter(ride => 
        ride.passengerName.toLowerCase().includes(query) ||
        ride.route.toLowerCase().includes(query)
      );
    }

    this.filteredRides = result;
    this.cdr.detectChanges();
  }

  onStatusFilterChange(): void {
    this.applyFilters();
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  clearFilters(): void {
    this.statusFilter = 'all';
    this.searchQuery = '';
    this.applyFilters();
  }

  // ================= mapping =================

  private mapBackendRidesToUI(backendRides: RideHistoryResponse[]): Ride[] {
    return backendRides.map((ride) => {
      const start = ride.startDate ? new Date(ride.startDate) : new Date();
      const end = ride.endDate ? new Date(ride.endDate) : null;

      return {
        id: ride.id,
        route: this.formatRoute(ride.pickupAddress, ride.destinationAddress),
        startDate: start.toISOString().split('T')[0],
        endDate: end ? end.toISOString().split('T')[0] : start.toISOString().split('T')[0],
        price: ride.price ?? 0,
        status: this.mapRideStatus(ride.status),
        startTime: start.toLocaleTimeString('en-US', {
          hour: '2-digit',
          minute: '2-digit'
        }),
        endTime: end ? end.toLocaleTimeString('en-US', {
          hour: '2-digit',
          minute: '2-digit'
        }) : 'N/A',
        duration: 'N/A',
        passengerName: ride.passengerName ?? 'N/A',
        passengerPhone: 'N/A',
        distance: ride.distance ?? 0,
        paymentMethod: 'N/A',
        driverRating: ride.driverRating ?? null,
        vehicleRating: ride.vehicleRating ?? null
      };
    });
  }

  // ================= details =================

  viewRideDetails(rideId: number): void {
    this.isLoading = true;
    this.cdr.detectChanges();

    this.rideService
      .getDriverRideDetail(rideId)
      .pipe(
        finalize(() => {
          this.isLoading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (detail) => {
          this.selectedRide = this.mapDetailToUI(detail);
          this.cdr.detectChanges();
          
          // Initialize map after modal opens
          setTimeout(() => this.initMap(), 100);
        },
        error: () => {
          this.errorMessage = 'Failed to load ride details. Please try again.';
          this.cdr.detectChanges();
        }
      });
  }

  private mapDetailToUI(detail: RideDetailResponse): Ride {
    const created = new Date(detail.createdAt);
    const started = detail.startedAt ? new Date(detail.startedAt) : null;
    const completed = detail.completedAt ? new Date(detail.completedAt) : null;

    return {
      id: detail.id,
      route: this.formatRoute(detail.pickupAddress, detail.dropoffAddress),
      startDate: (started || created).toISOString().split('T')[0],
      endDate: (completed || created).toISOString().split('T')[0],
      price: detail.totalPrice ?? 0,
      status: this.mapRideStatus(detail.status),
      startTime: (started || created).toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit'
      }),
      endTime: completed ? completed.toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit'
      }) : 'N/A',
      duration: this.formatDuration(detail.duration),
      passengerName: detail.passengerName ?? 'N/A',
      passengerPhone: detail.passengerPhone ?? 'N/A',
      distance: detail.distance ?? 0,
      paymentMethod: 'N/A',
      notes: detail.ratingComment ?? undefined,
      driverRating: detail.driverRating ?? null,
      vehicleRating: detail.vehicleRating ?? null,
      pickupLat: detail.pickupLatitude,
      pickupLng: detail.pickupLongitude,
      dropoffLat: detail.dropoffLatitude,
      dropoffLng: detail.dropoffLongitude
    };
  }

  // ================= map =================

  private initMap(): void {
    if (!this.selectedRide || !this.selectedRide.pickupLat || !this.selectedRide.pickupLng) {
      return;
    }

    // Destroy existing map
    if (this.map) {
      this.map.remove();
    }

    const pickupLatLng: L.LatLngExpression = [
      this.selectedRide.pickupLat,
      this.selectedRide.pickupLng
    ];
    
    const dropoffLatLng: L.LatLngExpression = [
      this.selectedRide.dropoffLat || this.selectedRide.pickupLat,
      this.selectedRide.dropoffLng || this.selectedRide.pickupLng
    ];

    // Initialize map
    this.map = L.map('ride-map').setView(pickupLatLng, 13);

    // Add tile layer
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap'
    }).addTo(this.map);

    // Custom icons
    const pickupIcon = L.icon({
      iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
      shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      shadowSize: [41, 41]
    });

    const dropoffIcon = L.icon({
      iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
      shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      shadowSize: [41, 41]
    });

    // Add markers
    L.marker(pickupLatLng, { icon: pickupIcon })
      .addTo(this.map)
      .bindPopup('<b>Pickup</b><br>' + this.selectedRide.route.split(' → ')[0]);

    L.marker(dropoffLatLng, { icon: dropoffIcon })
      .addTo(this.map)
      .bindPopup('<b>Dropoff</b><br>' + this.selectedRide.route.split(' → ')[1]);

    // Draw line between markers
    L.polyline([pickupLatLng, dropoffLatLng], {
      color: '#00acc1',
      weight: 3,
      opacity: 0.7,
      dashArray: '10, 10'
    }).addTo(this.map);

    // Fit bounds to show both markers
    const bounds = L.latLngBounds([pickupLatLng, dropoffLatLng]);
    this.map.fitBounds(bounds, { padding: [50, 50] });
  }

  // ================= helpers =================

  filterByDate(): void {
    this.loadRideHistory();
  }

  closeModal(): void {
    if (this.map) {
      this.map.remove();
      this.map = null;
    }
    this.selectedRide = null;
    this.cdr.detectChanges();
  }

  private formatDuration(seconds: number | null): string {
    if (!seconds) return 'N/A';
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    return hours > 0 ? `${hours}h ${minutes % 60}min` : `${minutes}min`;
  }

  private formatRoute(pickup: string, destination: string): string {
    return `${pickup || 'Unknown'} → ${destination || 'Unknown'}`;
  }

  private mapRideStatus(status: string): 'Completed' | 'Cancelled' | 'Pending' | 'In Progress' {
    switch (status) {
      case 'COMPLETED':
      case 'FINISHED':
        return 'Completed';
      case 'CANCELLED':
        return 'Cancelled';
      case 'IN_PROGRESS':
        return 'In Progress';
      default:
        return 'Pending';
    }
  }

  getStatusClass(status: string): string {
    switch (status.toLowerCase()) {
      case 'completed':
        return 'bg-green-100 text-green-800';
      case 'cancelled':
        return 'bg-red-100 text-red-800';
      case 'pending':
        return 'bg-yellow-100 text-yellow-800';
      case 'in progress':
        return 'bg-blue-100 text-blue-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }
}