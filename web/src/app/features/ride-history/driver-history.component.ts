import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { NgIcon } from '@ng-icons/core';

interface Ride {
  id: number;
  route: string;
  departure: string;
  destination: string;
  startDate: string;
  endDate: string;
  price: number;
  status: 'Completed' | 'Cancelled' | 'PANIC';
  passengers: string[];
  cancelledBy?: string;
}

@Component({
  selector: 'app-driver-history',
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    NgIcon
  ],
  templateUrl: './driver-history.component.html',
  styleUrl: './driver-history.component.css'
})
export class DriverHistory {
  rides: Ride[] = [];
  filteredRides: Ride[] = [];
  dateRange: string = '';

  constructor() {
    this.loadRides();
  }

  loadRides(): void {
    // TODO: Replace with actual API call to fetch driver's ride history
    this.rides = [
      {
        id: 1,
        route: 'Kraljevo park → Jarmenačka 23b',
        departure: 'Kraljevo park',
        destination: 'Jarmenačka 23b',
        startDate: 'Dec 14, 2024 14:30',
        endDate: 'Dec 14, 2024 14:45',
        price: 8.50,
        status: 'Completed',
        passengers: ['John Doe', 'Jane Smith']
      },
      {
        id: 2,
        route: 'Airport → City Center',
        departure: 'Airport',
        destination: 'City Center',
        startDate: 'Dec 13, 2024 09:15',
        endDate: 'Dec 13, 2024 09:45',
        price: 15.30,
        status: 'Completed',
        passengers: ['Mike Johnson']
      },
      {
        id: 3,
        route: 'Main Square → Train Station',
        departure: 'Main Square',
        destination: 'Train Station',
        startDate: 'Dec 12, 2024 18:20',
        endDate: 'Dec 12, 2024 18:35',
        price: 6.60,
        status: 'Cancelled',
        passengers: ['Sarah Williams'],
        cancelledBy: 'Passenger'
      },
      {
        id: 4,
        route: 'University → Shopping Mall',
        departure: 'University',
        destination: 'Shopping Mall',
        startDate: 'Dec 11, 2024 16:00',
        endDate: 'Dec 11, 2024 16:20',
        price: 12.00,
        status: 'PANIC',
        passengers: ['Emma Davis', 'Tom Brown']
      },
      {
        id: 5,
        route: 'Hotel Plaza → Conference Center',
        departure: 'Hotel Plaza',
        destination: 'Conference Center',
        startDate: 'Dec 10, 2024 08:45',
        endDate: 'Dec 10, 2024 09:10',
        price: 9.30,
        status: 'Completed',
        passengers: ['Robert Wilson']
      }
    ];

    this.filteredRides = [...this.rides];
  }

  filterByDate(): void {
    // TODO: Implement date filtering logic
    // This should parse the dateRange input and filter rides accordingly
    if (!this.dateRange) {
      this.filteredRides = [...this.rides];
      return;
    }

    // Placeholder for filtering logic
    console.log('Filtering by date range:', this.dateRange);
    // You'll need to integrate a date picker library and implement proper filtering
  }

  viewRideDetails(rideId: number): void {
    // TODO: Navigate to ride details page or open modal with full ride information
    const ride = this.rides.find(r => r.id === rideId);
    console.log('View details for ride:', ride);
    // You could use router.navigate() or open a modal here
  }

  getStatusClass(status: string): string {
    switch(status) {
      case 'Completed':
        return 'bg-green-100 text-green-800';
      case 'Cancelled':
        return 'bg-red-100 text-red-800';
      case 'PANIC':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }
}