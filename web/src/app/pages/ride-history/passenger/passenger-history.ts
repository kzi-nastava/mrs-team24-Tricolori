import { Component, OnInit, ChangeDetectorRef, signal} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { heroEye, heroXMark, heroStar } from '@ng-icons/heroicons/outline';
import { heroHeartSolid, heroStarSolid } from '@ng-icons/heroicons/solid';
import { finalize } from 'rxjs/operators';
import * as L from 'leaflet';
import { RideService } from '../../../services/ride.service';
import { MapService } from '../../../services/map.service';
import { PassengerRideHistoryResponse } from '../../../model/ride-history';
import { FavoriteRoutesService } from '../../../services/favorite-routes.service';
import { FavoriteRoute } from '../../../model/route';

interface PassengerRide {
  id: number;
  routeId: number;
  route: string;
  startDate: string;
  endDate: string;
  price: number;
  status: 'Completed' | 'Cancelled' | 'Scheduled' | 'In Progress';
  startTime: string;
  endTime: string;
  duration: string;
  driverName: string;
  driverPhone: string;
  vehicleType: string;
  licensePlate: string;
  distance: number;
  paymentMethod: string;
  notes?: string;
  rating?: {
    driverRating: number;
    vehicleRating: number;
    comment: string;
    ratedAt: string;
  };
  completedAt: Date;
  canRate: boolean;
  ratingExpired: boolean;
  driverRating?: number | null;
  vehicleRating?: number | null;
  // Map data
  pickupLat?: number;
  pickupLng?: number;
  dropoffLat?: number;
  dropoffLng?: number;
}

@Component({
  selector: 'app-passenger-history',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NgIconComponent
  ],
  providers: [
    provideIcons({ heroEye, heroXMark, heroStar, heroStarSolid, heroHeartSolid })
  ],
  templateUrl: './passenger-history.html',
  styleUrls: ['./passenger-history.css']
})
export class PassengerHistory implements OnInit {
  // Date filter properties
  startDate: string = '';
  endDate: string = '';

  // Modal property
  selectedRide: PassengerRide | null = null;

  // All rides and filtered rides
  allRides: PassengerRide[] = [];
  filteredRides: PassengerRide[] = [];

  // Loading and error states
  isLoading = false;
  errorMessage = '';

  // Filter options
  statusFilter: string = 'all';
  searchQuery: string = '';

  favoriteRoutes = signal<FavoriteRoute[]>([]);

  constructor(
    private router: Router,
    private rideService: RideService,
    private favoriteRouteService: FavoriteRoutesService,
    private mapService: MapService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadRideHistory();
    this.loadFavoriteRoutes();
  }

  // ================= favorite route =================

  loadFavoriteRoutes(): void {
    this.favoriteRouteService.getFavoriteRoutes().subscribe({
      next: (routes) => this.favoriteRoutes.set(routes),
      error: (err) => console.error("Greška pri učitavanju favorita:", err)
    });
  }

  isFavorite(ride: PassengerRide): boolean {
    if (!ride) return false;
    return this.favoriteRoutes().some(fav => fav.routeId === ride.routeId);
  }

  toggleFavorite(ride: PassengerRide): void {
    if (!ride) return;

    if (this.isFavorite(ride)) {
      const fav = this.favoriteRoutes().find(f => f.routeId === ride.routeId);
      if (fav) {
        this.favoriteRouteService.removeFavoriteRoute(fav.routeId).subscribe({
          next: () => this.loadFavoriteRoutes(),
          error: (err) => console.error("Greška pri brisanju:", err)
        });
      }
    } else {
      this.favoriteRouteService.addFavoriteRoute(ride.routeId, "My Favorite Ride").subscribe({
        next: () => this.loadFavoriteRoutes(),
        error: (err) => console.error("Greška pri dodavanju:", err)
      });
    }
  }

  // ================= load history =================

  loadRideHistory(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.cdr.detectChanges();

    this.rideService
      .getPassengerHistory(undefined, undefined)
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
        },
        error: (error) => {
          console.error('Failed to load ride history:', error);
          this.errorMessage = 'Failed to load ride history.';
          this.allRides = [];
          this.filteredRides = [];
        }
      });
  }

  // ================= filtering =================

  applyFilters(): void {
    let result = [...this.allRides];

    // ===== DATE FILTER =====
    if (this.startDate) {
      const start = new Date(this.startDate);
      result = result.filter(ride => {
        const rideDate = new Date(ride.startDate);
        return rideDate >= start;
      });
    }

    if (this.endDate) {
      const end = new Date(this.endDate);
      end.setHours(23, 59, 59, 999);
      result = result.filter(ride => {
        const rideDate = new Date(ride.startDate);
        return rideDate <= end;
      });
    }

    // ===== STATUS FILTER =====
    if (this.statusFilter !== 'all') {
      result = result.filter(ride =>
        ride.status.toLowerCase() === this.statusFilter.toLowerCase()
      );
    }

    // ===== SEARCH FILTER =====
    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      result = result.filter(ride =>
        ride.driverName.toLowerCase().includes(query) ||
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
    this.startDate = '';
    this.endDate = '';
    this.applyFilters();
  }

  filterByDate(): void {
    this.applyFilters();
  }

  // ================= mapping =================

  private mapBackendRidesToUI(backendRides: PassengerRideHistoryResponse[]): PassengerRide[] {
    return backendRides.map((ride) => {
      const start = ride.createdAt ? new Date(ride.createdAt) : new Date();
      const end = ride.endDate ? new Date(ride.endDate) : null;
      const completedAt = end || start;

      // Check if ride can be rated (completed within 72 hours and not yet rated)
      const canRate = this.canRideBeRated(ride, completedAt);
      const ratingExpired = this.isRatingExpired(completedAt);

      return {
        id: ride.id,
        routeId: ride.routeId,
        route: this.formatRoute(ride.pickupAddress, ride.destinationAddress),
        startDate: start.toISOString().split('T')[0],
        endDate: end ? end.toISOString().split('T')[0] : start.toISOString().split('T')[0],
        price: ride.totalPrice ?? 0,
        status: this.mapRideStatus(ride.status),
        startTime: start.toLocaleTimeString('en-US', {
          hour: '2-digit',
          minute: '2-digit'
        }),
        endTime: end ? end.toLocaleTimeString('en-US', {
          hour: '2-digit',
          minute: '2-digit'
        }) : 'N/A',
        duration: this.formatDuration(ride.duration),
        driverName: ride.driverName ?? 'N/A',
        driverPhone: 'N/A',
        vehicleType: 'N/A',
        licensePlate: 'N/A',
        distance: ride.distance ?? 0,
        paymentMethod: 'N/A',
        completedAt: completedAt,
        canRate: canRate,
        ratingExpired: ratingExpired,
        driverRating: ride.driverRating ?? null,
        vehicleRating: ride.vehicleRating ?? null,
        // If ratings exist, create rating object
        rating: (ride.driverRating || ride.vehicleRating) ? {
          driverRating: ride.driverRating || 0,
          vehicleRating: ride.vehicleRating || 0,
          comment: '',
          ratedAt: end ? end.toLocaleDateString() : start.toLocaleDateString()
        } : undefined
      };
    });
  }

  private canRideBeRated(ride: PassengerRideHistoryResponse, completedAt: Date): boolean {
    // Can rate if: status is completed, no rating exists yet, and within 72 hours
    const isCompleted = this.mapRideStatus(ride.status) === 'Completed';
    const hasNoRating = !ride.driverRating && !ride.vehicleRating;
    const within72Hours = !this.isRatingExpired(completedAt);

    return isCompleted && hasNoRating && within72Hours;
  }

  private isRatingExpired(completedAt: Date): boolean {
    const now = new Date();
    const hoursSinceCompleted = (now.getTime() - completedAt.getTime()) / (1000 * 60 * 60);
    return hoursSinceCompleted > 72;
  }

  // ================= details =================

  viewRideDetails(rideId: number): void {
    this.isLoading = true;
    this.cdr.detectChanges();

    this.rideService
      .getPassengerRideDetail(rideId)
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
        error: (error) => {
          console.error('Failed to load ride details:', error);
          this.errorMessage = 'Failed to load ride details. Please try again.';
          this.cdr.detectChanges();
        }
      });
  }

  private mapDetailToUI(detail: any): PassengerRide {
    const created = new Date(detail.createdAt);
    const started = detail.startedAt ? new Date(detail.startedAt) : null;
    const completed = detail.completedAt ? new Date(detail.completedAt) : null;
    const completedAt = completed || started || created;

    const canRate = this.canRideBeRatedFromDetail(detail, completedAt);
    const ratingExpired = this.isRatingExpired(completedAt);

    return {
      id: detail.id,
      routeId: detail.routeId,
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
      driverName: detail.driverName ?? 'N/A',
      driverPhone: detail.driverPhone ?? 'N/A',
      vehicleType: detail.vehicleModel ?? 'N/A',
      licensePlate: detail.vehicleLicensePlate ?? 'N/A',
      distance: detail.distance ?? 0,
      paymentMethod: 'N/A',
      notes: detail.ratingComment ?? undefined,
      completedAt: completedAt,
      canRate: canRate,
      ratingExpired: ratingExpired,
      driverRating: detail.driverRating ?? null,
      vehicleRating: detail.vehicleRating ?? null,
      rating: (detail.driverRating || detail.vehicleRating) ? {
        driverRating: detail.driverRating || 0,
        vehicleRating: detail.vehicleRating || 0,
        comment: detail.ratingComment || '',
        ratedAt: completed ? completed.toLocaleDateString() : created.toLocaleDateString()
      } : undefined,
      // Map coordinates
      pickupLat: detail.pickupLatitude,
      pickupLng: detail.pickupLongitude,
      dropoffLat: detail.dropoffLatitude,
      dropoffLng: detail.dropoffLongitude
    };
  }

  private canRideBeRatedFromDetail(detail: any, completedAt: Date): boolean {
    const isCompleted = this.mapRideStatus(detail.status) === 'Completed';
    const hasNoRating = !detail.driverRating && !detail.vehicleRating;
    const within72Hours = !this.isRatingExpired(completedAt);

    return isCompleted && hasNoRating && within72Hours;
  }

  // ================= map =================

  private async initMap(): Promise<void> {
    if (!this.selectedRide || !this.selectedRide.pickupLat || !this.selectedRide.pickupLng) {
      return;
    }

    const pickupCoords: [number, number] = [
      this.selectedRide.pickupLat,
      this.selectedRide.pickupLng
    ];

    const dropoffCoords: [number, number] = [
      this.selectedRide.dropoffLat || this.selectedRide.pickupLat,
      this.selectedRide.dropoffLng || this.selectedRide.pickupLng
    ];

    this.mapService.initMap('ride-map', pickupCoords, 13);

    try {
      // Fetch route from OSRM
      const url = `https://router.project-osrm.org/route/v1/driving/${pickupCoords[1]},${pickupCoords[0]};${dropoffCoords[1]},${dropoffCoords[0]}?overview=full&geometries=geojson`;

      const response = await fetch(url);
      const data = await response.json();

      if (data.routes && data.routes.length > 0) {
        const coordinates = data.routes[0].geometry.coordinates;

        // Convert to Leaflet LatLng format
        const routeCoordinates = coordinates.map((coord: number[]) =>
          L.latLng(coord[1], coord[0])
        );

        this.mapService.drawRoute([]);
      } else {
        // Fallback: draw without route line if OSRM fails
        this.mapService.drawRoute([]);
      }
    } catch (error) {
      console.error('Failed to fetch route from OSRM:', error);

      // Fallback: draw without route line
      this.mapService.drawRoute([]);
    }
  }

  // ================= modal =================

  closeModal(): void {
    // Destroy map before closing modal
    if (this.selectedRide) {
      this.mapService.destroyMap();
    }
    this.selectedRide = null;
    this.cdr.detectChanges();
  }

  // ================= navigation =================

  navigateToRating(rideId: number): void {
    this.router.navigate(['/passenger/ride-rating', rideId]);
  }

  navigateToTracking(rideId: number): void {
    this.closeModal();
    this.router.navigate(['/passenger/ride-tracking', rideId]);
  }

  // ================= helpers =================

  getHoursRemaining(ride: PassengerRide): number {
    const now = new Date();
    const completedAt = new Date(ride.completedAt);
    const hoursSinceCompleted = (now.getTime() - completedAt.getTime()) / (1000 * 60 * 60);
    return Math.max(0, 72 - Math.floor(hoursSinceCompleted));
  }

  isRatingDeadlineNear(ride: PassengerRide): boolean {
    const hoursRemaining = this.getHoursRemaining(ride);
    return hoursRemaining > 0 && hoursRemaining <= 24;
  }

  getStatusClass(status: string): string {
    switch (status.toLowerCase()) {
      case 'completed':
        return 'bg-green-100 text-green-800';
      case 'cancelled':
        return 'bg-red-100 text-red-800';
      case 'scheduled':
        return 'bg-yellow-100 text-yellow-800';
      case 'in progress':
        return 'bg-blue-100 text-blue-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getRatingStars(rating: number): number[] {
    return Array(5).fill(0).map((_, i) => i + 1);
  }

  isStarFilled(starNumber: number, rating: number): boolean {
    return starNumber <= rating;
  }

  private formatDuration(seconds: number | null | undefined): string {
    if (!seconds) return 'N/A';
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    return hours > 0 ? `${hours}h ${minutes % 60}min` : `${minutes}min`;
  }

  private formatRoute(pickup: string, destination: string): string {
    return `${pickup || 'Unknown'} → ${destination || 'Unknown'}`;
  }

  private mapRideStatus(status: string): 'Completed' | 'Cancelled' | 'Scheduled' | 'In Progress' {
    switch (status) {
      case 'COMPLETED':
      case 'FINISHED':
        return 'Completed';
      case 'CANCELLED':
      case 'CANCELLED_BY_DRIVER':
      case 'CANCELLED_BY_PASSENGER':
        return 'Cancelled';
      case 'IN_PROGRESS':
      case 'ONGOING':
        return 'In Progress';
      default:
        return 'Scheduled';
    }
  }
}
