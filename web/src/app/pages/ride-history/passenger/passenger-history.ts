import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { heroEye, heroXMark, heroStar } from '@ng-icons/heroicons/outline';
import { heroStarSolid } from '@ng-icons/heroicons/solid';
import { finalize } from 'rxjs/operators';

import {
  RideService,
  RideDetailResponse
} from '../../../services/ride.service';

interface PassengerRide {
  id: number;
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
}

// Backend response interface for passenger history
interface PassengerRideHistoryResponse {
  id: number;
  driverName?: string;
  pickupAddress: string;
  destinationAddress: string;
  status: string;
  price: number;
  distance?: number;
  duration?: number;
  startDate: string;
  endDate: string | null;
  driverRating?: number | null;
  vehicleRating?: number | null;
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
    provideIcons({ heroEye, heroXMark, heroStar, heroStarSolid })
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

  constructor(
    private router: Router,
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
      const start = ride.startDate ? new Date(ride.startDate) : new Date();
      const end = ride.endDate ? new Date(ride.endDate) : null;
      const completedAt = end || start;

      // Check if ride can be rated (completed within 72 hours and not yet rated)
      const canRate = this.canRideBeRated(ride, completedAt);
      const ratingExpired = this.isRatingExpired(completedAt);

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
      } : undefined
    };
  }

  private canRideBeRatedFromDetail(detail: any, completedAt: Date): boolean {
    const isCompleted = this.mapRideStatus(detail.status) === 'Completed';
    const hasNoRating = !detail.driverRating && !detail.vehicleRating;
    const within72Hours = !this.isRatingExpired(completedAt);
    
    return isCompleted && hasNoRating && within72Hours;
  }

  // ================= modal =================

  closeModal(): void {
    this.selectedRide = null;
    this.cdr.detectChanges();
  }

  // ================= navigation =================

  navigateToRating(rideId: number): void {
    this.router.navigate(['/passenger/ride-rating', rideId]);
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

  // DELETE AFTER TESTING
  startTestRide() {
    this.router.navigate(['/passenger/ride-tracking', 7]);
  }
}