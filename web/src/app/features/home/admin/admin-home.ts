import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { 
  heroUsers, 
  heroStar, 
  heroMapPin,
  heroChartBar,
  heroClock
} from '@ng-icons/heroicons/outline';

interface Driver {
  id: number;
  name: string;
  status: 'active' | 'inactive' | 'on-ride';
  rating: number;
  totalRides: number;
  location: { lat: number; lng: number };
  lastUpdate: string;
}

interface Statistics {
  totalDrivers: number;
  activeDrivers: number;
  averageRating: number;
  ongoingRides: number;
}

@Component({
  selector: 'app-admin-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    NgIcon
  ],
  providers: [provideIcons({ 
    heroUsers, 
    heroStar, 
    heroMapPin,
    heroChartBar,
    heroClock
  })],
  templateUrl: './admin-home.html',
  styleUrl: './admin-home.css'
})
export class AdminHome {
  statistics: Statistics = {
    totalDrivers: 0,
    activeDrivers: 0,
    averageRating: 0,
    ongoingRides: 0
  };

  drivers: Driver[] = [];
  selectedDriver: Driver | null = null;

  constructor() {
    this.loadStatistics();
    this.loadDrivers();
  }

  loadStatistics(): void {
    // TODO: Replace with actual API call
    this.statistics = {
      totalDrivers: 2500,
      activeDrivers: 850,
      averageRating: 4.9,
      ongoingRides: 127
    };
  }

  loadDrivers(): void {
    // TODO: Replace with actual API call to fetch driver data
    this.drivers = [
      {
        id: 1,
        name: 'Marko Petrović',
        status: 'active',
        rating: 4.8,
        totalRides: 342,
        location: { lat: 44.7866, lng: 20.4489 },
        lastUpdate: '2 min ago'
      },
      {
        id: 2,
        name: 'Ana Jovanović',
        status: 'on-ride',
        rating: 4.9,
        totalRides: 567,
        location: { lat: 44.8125, lng: 20.4612 },
        lastUpdate: 'Just now'
      },
      {
        id: 3,
        name: 'Stefan Nikolić',
        status: 'active',
        rating: 4.7,
        totalRides: 289,
        location: { lat: 44.8047, lng: 20.4633 },
        lastUpdate: '5 min ago'
      },
      {
        id: 4,
        name: 'Jelena Đorđević',
        status: 'inactive',
        rating: 4.6,
        totalRides: 156,
        location: { lat: 44.8200, lng: 20.4500 },
        lastUpdate: '1 hour ago'
      }
    ];
  }

  selectDriver(driver: Driver): void {
    this.selectedDriver = driver;
    console.log('Selected driver:', driver);
    // TODO: Update map to focus on selected driver
  }

  getStatusClass(status: string): string {
    switch(status) {
      case 'active':
        return 'bg-green-100 text-green-800';
      case 'on-ride':
        return 'bg-blue-100 text-blue-800';
      case 'inactive':
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getStatusText(status: string): string {
    switch(status) {
      case 'active':
        return 'Available';
      case 'on-ride':
        return 'On Ride';
      case 'inactive':
        return 'Offline';
      default:
        return status;
    }
  }
}