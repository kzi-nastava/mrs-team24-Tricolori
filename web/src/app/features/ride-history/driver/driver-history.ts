import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
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
}

@Component({
  selector: 'app-driver-history',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NgIconComponent,
    HttpClientModule
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
      const startDateTime = new Date(ride.startTime);
      const endDateTime = new Date(ride.endTime);
      
      // Calculate duration
      const durationMs = endDateTime.getTime() - startDateTime.getTime();
      const hours = Math.floor(durationMs / (1000 * 60 * 60));
      const minutes = Math.floor((durationMs % (1000 * 60 * 60)) / (1000 * 60));
      const duration = `${hours}h ${minutes}min`;

      // Format route from addresses
      const route = this.formatRoute(ride.pickupAddress, ride.destinationAddress);

      return {
        id: ride.rideId,
        route: route,
        startDate: startDateTime.toISOString().split('T')[0],
        endDate: endDateTime.toISOString().split('T')[0],
        price: ride.price,
        status: this.mapRideStatus(ride.rideStatus),
        startTime: startDateTime.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' }),
        endTime: endDateTime.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' }),
        duration: duration,
        passengerName: 'N/A', // Will be loaded in detail view
        passengerPhone: 'N/A',
        distance: 0, // Will be loaded in detail view
        paymentMethod: 'N/A'
      };
    });
  }

  /**
   * Format route from pickup and destination addresses
   */
  private formatRoute(pickup: any, destination: any): string {
    const pickupStr = pickup?.street || pickup?.city || 'Unknown';
    const destStr = destination?.street || destination?.city || 'Unknown';
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
    const startDateTime = new Date(detail.startTime);
    const endDateTime = new Date(detail.endTime);
    
    const durationMs = endDateTime.getTime() - startDateTime.getTime();
    const hours = Math.floor(durationMs / (1000 * 60 * 60));
    const minutes = Math.floor((durationMs % (1000 * 60 * 60)) / (1000 * 60));
    const duration = detail.duration || `${hours}h ${minutes}min`;

    return {
      id: detail.rideId,
      route: this.formatRoute(detail.pickupAddress, detail.destinationAddress),
      startDate: startDateTime.toISOString().split('T')[0],
      endDate: endDateTime.toISOString().split('T')[0],
      price: detail.price,
      status: this.mapRideStatus(detail.rideStatus),
      startTime: startDateTime.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' }),
      endTime: endDateTime.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' }),
      duration: duration,
      passengerName: detail.passengerName || 'N/A',
      passengerPhone: detail.passengerPhone || 'N/A',
      distance: detail.distance || 0,
      paymentMethod: detail.paymentMethod || 'N/A',
      notes: detail.notes
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