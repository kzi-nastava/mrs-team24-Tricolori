import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { heroEye, heroXMark } from '@ng-icons/heroicons/outline';
import { RideService, RideHistoryResponse, RideDetailResponse } from '../../../core/services/ride.service';

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
  imports: [
    CommonModule,
    FormsModule,
    NgIconComponent 
  ],
  providers: [
    provideIcons({ heroEye, heroXMark }),
    RideService
  ],
  templateUrl: './driver-history.html',
  styleUrls: ['./driver-history.css']
})
export class DriverHistory implements OnInit {
  startDate: string = '';
  endDate: string = '';
  selectedRide: Ride | null = null;
  filteredRides: Ride[] = [];
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(private rideService: RideService) {}

  ngOnInit(): void {
    this.loadRideHistory();
  }

  /**
   * Load ride history from backend
   */
  loadRideHistory(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.rideService.getDriverHistory(
      this.startDate || undefined,
      this.endDate || undefined
    ).subscribe({
      next: (rides) => {
        this.filteredRides = this.mapBackendRidesToUI(rides);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading ride history:', error);
        this.errorMessage = 'Failed to load ride history. Please try again.';
        this.isLoading = false;
        this.filteredRides = [];
      }
    });
  }

  /**
   * Map backend response to UI format
   */
  private mapBackendRidesToUI(backendRides: RideHistoryResponse[]): Ride[] {
    return backendRides.map(ride => {
      const createdDateTime = new Date(ride.createdAt);
      const completedDateTime = ride.completedAt ? new Date(ride.completedAt) : null;
      
      // Calculate duration from duration field (in seconds)
      const duration = this.formatDuration(ride.duration);

      // Format route from addresses
      const route = this.formatRoute(ride.pickupAddress, ride.dropoffAddress);

      return {
        id: ride.id,
        route: route,
        startDate: createdDateTime.toISOString().split('T')[0],
        endDate: completedDateTime ? completedDateTime.toISOString().split('T')[0] : createdDateTime.toISOString().split('T')[0],
        price: ride.totalPrice,
        status: this.mapRideStatus(ride.status),
        startTime: createdDateTime.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' }),
        endTime: completedDateTime ? completedDateTime.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' }) : 'N/A',
        duration: duration,
        passengerName: ride.passengerName,
        passengerPhone: 'N/A', // Will be loaded in detail view
        distance: ride.distance,
        paymentMethod: 'N/A', // Not available in history response
        driverRating: ride.driverRating,
        vehicleRating: ride.vehicleRating
      };
    });
  }

  /**
   * Format duration from seconds to readable string
   */
  private formatDuration(seconds: number | null): string {
    if (!seconds) return 'N/A';
    
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    
    if (hours > 0) {
      return `${hours}h ${minutes}min`;
    }
    return `${minutes}min`;
  }

  /**
   * Format route from pickup and destination addresses
   */
  private formatRoute(pickup: string, destination: string): string {
    const pickupStr = pickup || 'Unknown';
    const destStr = destination || 'Unknown';
    return `${pickupStr} → ${destStr}`;
  }

  /**
   * Map backend ride status to UI status
   */
  private mapRideStatus(status: string): 'Completed' | 'Cancelled' | 'Pending' | 'In Progress' {
    switch (status) {
      case 'COMPLETED':
        return 'Completed';
      case 'CANCELLED':
        return 'Cancelled';
      case 'IN_PROGRESS':
        return 'In Progress';
      case 'PENDING':
      case 'ACCEPTED':
        return 'Pending';
      default:
        return 'Pending';
    }
  }

  /**
   * Filter rides by date range
   */
  filterByDate(): void {
    this.loadRideHistory();
  }

  /**
   * View ride details - load from backend
   */
  viewRideDetails(rideId: number): void {
    this.isLoading = true;
    
    this.rideService.getDriverRideDetail(rideId).subscribe({
      next: (detail) => {
        this.selectedRide = this.mapDetailToUI(detail);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading ride details:', error);
        this.errorMessage = 'Failed to load ride details. Please try again.';
        this.isLoading = false;
      }
    });
  }

  /**
   * Map detailed backend response to UI format
   */
  private mapDetailToUI(detail: RideDetailResponse): Ride {
    const createdDateTime = new Date(detail.createdAt);
    const startedDateTime = detail.startedAt ? new Date(detail.startedAt) : null;
    const completedDateTime = detail.completedAt ? new Date(detail.completedAt) : null;
    
    // Use the duration from backend (in seconds)
    const duration = this.formatDuration(detail.duration);

    return {
      id: detail.id,
      route: this.formatRoute(detail.pickupAddress, detail.dropoffAddress),
      startDate: (startedDateTime || createdDateTime).toISOString().split('T')[0],
      endDate: (completedDateTime || createdDateTime).toISOString().split('T')[0],
      price: detail.totalPrice,
      status: this.mapRideStatus(detail.status),
      startTime: (startedDateTime || createdDateTime).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' }),
      endTime: completedDateTime ? completedDateTime.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' }) : 'N/A',
      duration: duration,
      passengerName: detail.passengerName || 'N/A',
      passengerPhone: detail.passengerPhone || 'N/A',
      distance: detail.distance || 0,
      paymentMethod: 'N/A', // Not available in backend
      notes: detail.ratingComment || undefined,
      driverRating: detail.driverRating,
      vehicleRating: detail.vehicleRating
    };
  }

  /**
   * Close modal
   */
  closeModal(): void {
    this.selectedRide = null;
  }

  /**
   * Get CSS class for status badge
   */
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