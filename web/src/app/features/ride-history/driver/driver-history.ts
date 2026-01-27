import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { heroEye, heroXMark } from '@ng-icons/heroicons/outline';
import { finalize } from 'rxjs/operators';

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
  filteredRides: Ride[] = [];
  isLoading = false;
  errorMessage = '';

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
          this.filteredRides = this.mapBackendRidesToUI(rides);
          this.cdr.detectChanges();
        },
        error: () => {
          this.errorMessage = 'Failed to load ride history. Please try again.';
          this.filteredRides = [];
          this.cdr.detectChanges();
        }
      });
  }

  // ================= mapping =================

  private mapBackendRidesToUI(backendRides: RideHistoryResponse[]): Ride[] {
    return backendRides.map((ride) => {
      const start = ride.startDate
        ? new Date(ride.startDate)
        : new Date();

      const end = ride.endDate
        ? new Date(ride.endDate)
        : null;

      return {
        id: ride.id,
        route: this.formatRoute(
          ride.pickupAddress,
          ride.destinationAddress
        ),
        startDate: start.toISOString().split('T')[0],
        endDate: end
          ? end.toISOString().split('T')[0]
          : start.toISOString().split('T')[0],
        price: ride.price ?? 0,
        status: this.mapRideStatus(ride.status),
        startTime: start.toLocaleTimeString('en-US', {
          hour: '2-digit',
          minute: '2-digit'
        }),
        endTime: end
          ? end.toLocaleTimeString('en-US', {
              hour: '2-digit',
              minute: '2-digit'
            })
          : 'N/A',
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
        },
        error: () => {
          this.errorMessage = 'Failed to load ride details. Please try again.';
          this.cdr.detectChanges();
        }
      });
  }

  private mapDetailToUI(detail: RideDetailResponse): Ride {
    const created = new Date(detail.createdAt);

    const started = detail.startedAt
      ? new Date(detail.startedAt)
      : null;

    const completed = detail.completedAt
      ? new Date(detail.completedAt)
      : null;

    return {
      id: detail.id,
      route: this.formatRoute(
        detail.pickupAddress,
        detail.dropoffAddress
      ),
      startDate: (started || created).toISOString().split('T')[0],
      endDate: (completed || created).toISOString().split('T')[0],
      price: detail.totalPrice ?? 0,
      status: this.mapRideStatus(detail.status),
      startTime: (started || created).toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit'
      }),
      endTime: completed
        ? completed.toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit'
          })
        : 'N/A',
      duration: this.formatDuration(detail.duration),
      passengerName: detail.passengerName ?? 'N/A',
      passengerPhone: detail.passengerPhone ?? 'N/A',
      distance: detail.distance ?? 0,
      paymentMethod: 'N/A',
      notes: detail.ratingComment ?? undefined,
      driverRating: detail.driverRating ?? null,
      vehicleRating: detail.vehicleRating ?? null
    };
  }

  // ================= helpers =================

  filterByDate(): void {
    this.loadRideHistory();
  }

  closeModal(): void {
    this.selectedRide = null;
    this.cdr.detectChanges();
  }

  private formatDuration(seconds: number | null): string {
    if (!seconds) return 'N/A';

    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);

    return hours > 0
      ? `${hours}h ${minutes % 60}min`
      : `${minutes}min`;
  }

  private formatRoute(pickup: string, destination: string): string {
    return `${pickup || 'Unknown'} → ${destination || 'Unknown'}`;
  }

  private mapRideStatus(
    status: string
  ): 'Completed' | 'Cancelled' | 'Pending' | 'In Progress' {
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