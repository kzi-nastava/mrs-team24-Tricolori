import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { heroEye, heroXMark } from '@ng-icons/heroicons/outline';

interface Ride {
  id: number;
  route: string;
  startDate: string;
  endDate: string;
  price: number;
  status: 'Completed' | 'Cancelled' | 'Pending';
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
    NgIconComponent
  ],
  providers: [
    provideIcons({ heroEye, heroXMark })
  ],
  templateUrl: './driver-history.html',
  styleUrls: ['./driver-history.css']
})
export class DriverHistory {
  // Date filter properties
  startDate: string = '';
  endDate: string = '';
  
  // Modal property
  selectedRide: Ride | null = null;
  
  // All rides data
  allRides: Ride[] = [
    {
      id: 1,
      route: 'Narodnog fronta 23 → Dunavski park',
      startDate: '2024-12-15',
      endDate: '2024-12-15',
      price: 45.50,
      status: 'Completed',
      startTime: '09:30 AM',
      endTime: '11:15 AM',
      duration: '1h 45min',
      passengerName: 'Marko Petrović',
      passengerPhone: '+381 64 123 4567',
      distance: 89.5,
      paymentMethod: 'Credit Card',
      notes: 'Pleasant ride, passenger was on time.'
    },
    {
      id: 2,
      route: 'Bulevar Oslobođenja 30 → Trg Slobode',
      startDate: '2024-12-14',
      endDate: '2024-12-14',
      price: 32.00,
      status: 'Completed',
      startTime: '14:00 PM',
      endTime: '15:30 PM',
      duration: '1h 30min',
      passengerName: 'Ana Jovanović',
      passengerPhone: '+381 63 987 6543',
      distance: 72.3,
      paymentMethod: 'Cash',
      notes: 'Smooth journey, no issues.'
    },
    {
      id: 3,
      route: 'Železnička stanica Novi Sad → Limanski park',
      startDate: '2024-12-13',
      endDate: '2024-12-13',
      price: 85.00,
      status: 'Completed',
      startTime: '08:00 AM',
      endTime: '11:45 AM',
      duration: '3h 45min',
      passengerName: 'Stefan Nikolić',
      passengerPhone: '+381 65 555 1234',
      distance: 237.8,
      paymentMethod: 'Credit Card',
      notes: 'Long distance trip, passenger requested one rest stop.'
    },
    {
      id: 4,
      route: 'Spens (Bulevar cara Lazara) → Petrovaradinska tvrđava',
      startDate: '2024-12-12',
      endDate: '2024-12-12',
      price: 38.50,
      status: 'Cancelled',
      startTime: '16:00 PM',
      endTime: '17:30 PM',
      duration: '1h 30min',
      passengerName: 'Jelena Đorđević',
      passengerPhone: '+381 64 222 3333',
      distance: 115.2,
      paymentMethod: 'N/A',
      notes: 'Ride cancelled by passenger 30 minutes before scheduled time.'
    },
    {
      id: 5,
      route: 'Grbavica (Danila Kiša 18) → Spens',
      startDate: '2024-12-11',
      endDate: '2024-12-11',
      price: 25.00,
      status: 'Completed',
      startTime: '11:00 AM',
      endTime: '12:00 PM',
      duration: '1h',
      passengerName: 'Milan Stojanović',
      passengerPhone: '+381 66 777 8888',
      distance: 46.5,
      paymentMethod: 'Cash'
    }
  ];
  
  // Filtered rides (initially show all)
  filteredRides: Ride[] = [...this.allRides];

  // Filter method
  filterByDate(): void {
    if (!this.startDate && !this.endDate) {
      // If no dates selected, show all rides
      this.filteredRides = [...this.allRides];
      return;
    }

    this.filteredRides = this.allRides.filter(ride => {
      const rideDate = new Date(ride.startDate);
      const start = this.startDate ? new Date(this.startDate) : null;
      const end = this.endDate ? new Date(this.endDate) : null;

      if (start && end) {
        return rideDate >= start && rideDate <= end;
      } else if (start) {
        return rideDate >= start;
      } else if (end) {
        return rideDate <= end;
      }
      
      return true;
    });
  }

  // View ride details
  viewRideDetails(rideId: number): void {
    this.selectedRide = this.allRides.find(ride => ride.id === rideId) || null;
  }

  // Close modal
  closeModal(): void {
    this.selectedRide = null;
  }

  // Status class helper
  getStatusClass(status: string): string {
    switch (status.toLowerCase()) {
      case 'completed':
        return 'bg-green-100 text-green-800';
      case 'cancelled':
        return 'bg-red-100 text-red-800';
      case 'pending':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }
}