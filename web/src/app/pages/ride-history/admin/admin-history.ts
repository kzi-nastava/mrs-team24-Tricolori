import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { 
  heroEye, 
  heroXMark, 
  heroExclamationTriangle, 
  heroMapPin, 
  heroFlag,
  heroChevronUp,
  heroChevronDown
} from '@ng-icons/heroicons/outline';
import { heroStarSolid } from '@ng-icons/heroicons/solid';

interface Passenger {
  id: number;
  name: string;
  email: string;
  phone: string;
}

interface Report {
  id: number;
  reportedBy: string;
  timestamp: string;
  description: string;
}

interface Rating {
  id: number;
  passengerName: string;
  rating: number;
  comment?: string;
}

interface AdminRide {
  id: number;
  route: string;
  pickupLocation: string;
  destinationLocation: string;
  startDate: string;
  endDate: string;
  startTime: string;
  endTime: string;
  price: number;
  status: 'Completed' | 'Cancelled' | 'Active';
  duration: string;
  driverName: string;
  driverPhone: string;
  vehicleType: string;
  licensePlate: string;
  distance: number;
  passengerCount: number;
  passengers?: Passenger[];
  cancelledBy?: string;
  hasPanic: boolean;
  reportCount: number;
  reports?: Report[];
  averageRating?: number;
  ratings?: Rating[];
}

type SortField = 'route' | 'startDate' | 'price' | 'status';
type SortDirection = 'asc' | 'desc';

@Component({
  selector: 'app-admin-ride-history',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NgIconComponent
  ],
  providers: [
    provideIcons({ 
      heroEye, 
      heroXMark, 
      heroExclamationTriangle, 
      heroMapPin, 
      heroFlag,
      heroStarSolid,
      heroChevronUp,
      heroChevronDown
    })
  ],
  templateUrl: './admin-history.html'
})
export class AdminRideHistoryComponent {
  // Filter properties
  startDate: string = '';
  endDate: string = '';
  selectedUserType: string = 'all';
  searchUser: string = '';
  selectedStatus: string = 'all';
  
  // Sorting properties
  sortField: SortField = 'startDate';
  sortDirection: SortDirection = 'desc';
  
  // Modal property
  selectedRide: AdminRide | null = null;
  
  // All rides data
  allRides: AdminRide[] = [
    {
      id: 1,
      route: 'Trg Slobode 1 → Kisačka 71',
      pickupLocation: 'Trg Slobode 1, Novi Sad',
      destinationLocation: 'Kisačka 71, Novi Sad',
      startDate: '2025-01-17',
      endDate: '2025-01-17',
      startTime: '14:30',
      endTime: '14:38',
      price: 232.70,
      status: 'Completed',
      duration: '8min',
      driverName: 'Marko Petrović',
      driverPhone: '+381 64 123 4567',
      vehicleType: 'Economy - Toyota Corolla',
      licensePlate: 'NS-123-AB',
      distance: 2.3,
      passengerCount: 1,
      passengers: [
        { id: 1, name: 'Ana Jovanović', email: 'ana.jovanovic@example.com', phone: '+381 63 111 2222' }
      ],
      hasPanic: false,
      reportCount: 0,
      averageRating: 5.0,
      ratings: [
        { id: 1, passengerName: 'Ana Jovanović', rating: 5, comment: 'Excellent service, very professional driver!' }
      ]
    },
    {
      id: 2,
      route: 'Bulevar Oslobođenja 45 → Novi Sad Fair',
      pickupLocation: 'Bulevar Oslobođenja 45, Novi Sad',
      destinationLocation: 'Hajduk Veljkova 11, Novi Sad',
      startDate: '2025-01-17',
      endDate: '2025-01-17',
      startTime: '10:15',
      endTime: '10:15',
      price: 450.00,
      status: 'Cancelled',
      duration: '0min',
      driverName: 'Stefan Nikolić',
      driverPhone: '+381 65 555 1234',
      vehicleType: 'Premium - Mercedes E-Class',
      licensePlate: 'NS-789-EF',
      distance: 0,
      passengerCount: 2,
      passengers: [
        { id: 2, name: 'Milica Stojanović', email: 'milica@example.com', phone: '+381 64 222 3333' },
        { id: 3, name: 'Petar Nikolić', email: 'petar@example.com', phone: '+381 65 444 5555' }
      ],
      cancelledBy: 'Driver',
      hasPanic: false,
      reportCount: 0
    },
    {
      id: 3,
      route: 'Železnička stanica → Limanski park',
      pickupLocation: 'Železnička stanica, Novi Sad',
      destinationLocation: 'Limanski park, Novi Sad',
      startDate: '2025-01-16',
      endDate: '2025-01-16',
      startTime: '16:45',
      endTime: '16:53',
      price: 325.50,
      status: 'Completed',
      duration: '8min',
      driverName: 'Jelena Đorđević',
      driverPhone: '+381 64 222 3333',
      vehicleType: 'Comfort - Honda Accord',
      licensePlate: 'BG-456-CD',
      distance: 3.1,
      passengerCount: 1,
      passengers: [
        { id: 4, name: 'Nikola Jovanović', email: 'nikola@example.com', phone: '+381 66 777 8888' }
      ],
      hasPanic: true,
      reportCount: 1,
      reports: [
        {
          id: 1,
          reportedBy: 'Nikola Jovanović',
          timestamp: '2025-01-16 16:50',
          description: 'Driver took an unusual detour through a residential area instead of main road.'
        }
      ],
      averageRating: 2.5,
      ratings: [
        { id: 2, passengerName: 'Nikola Jovanović', rating: 2.5, comment: 'Uncomfortable ride, driver seemed distracted.' }
      ]
    },
    {
      id: 4,
      route: 'Spens → Petrovaradinska tvrđava',
      pickupLocation: 'Spens, Novi Sad',
      destinationLocation: 'Petrovaradinska tvrđava, Novi Sad',
      startDate: '2025-01-15',
      endDate: '2025-01-15',
      startTime: '12:00',
      endTime: '12:12',
      price: 280.00,
      status: 'Completed',
      duration: '12min',
      driverName: 'Milan Stojanović',
      driverPhone: '+381 66 777 8888',
      vehicleType: 'Economy - Volkswagen Golf',
      licensePlate: 'NS-654-IJ',
      distance: 4.5,
      passengerCount: 3,
      passengers: [
        { id: 5, name: 'Marija Petrović', email: 'marija@example.com', phone: '+381 63 111 2222' },
        { id: 6, name: 'Jovana Nikolić', email: 'jovana@example.com', phone: '+381 64 333 4444' },
        { id: 7, name: 'Aleksandar Jović', email: 'aleksandar@example.com', phone: '+381 65 555 6666' }
      ],
      hasPanic: false,
      reportCount: 0,
      averageRating: 4.3,
      ratings: [
        { id: 3, passengerName: 'Marija Petrović', rating: 4, comment: 'Good ride, but a bit crowded.' },
        { id: 4, passengerName: 'Jovana Nikolić', rating: 5, comment: 'Perfect!' },
        { id: 5, passengerName: 'Aleksandar Jović', rating: 4, comment: 'Nice driver, clean car.' }
      ]
    },
    {
      id: 5,
      route: 'Grbavica → Novosadski sajam',
      pickupLocation: 'Grbavica, Novi Sad',
      destinationLocation: 'Hajduk Veljkova 11, Novi Sad',
      startDate: '2025-01-14',
      endDate: '2025-01-14',
      startTime: '09:20',
      endTime: '09:28',
      price: 195.00,
      status: 'Completed',
      duration: '8min',
      driverName: 'Marko Petrović',
      driverPhone: '+381 64 123 4567',
      vehicleType: 'Economy - Toyota Corolla',
      licensePlate: 'NS-123-AB',
      distance: 2.8,
      passengerCount: 1,
      passengers: [
        { id: 8, name: 'Ivan Milić', email: 'ivan@example.com', phone: '+381 62 888 9999' }
      ],
      hasPanic: false,
      reportCount: 2,
      reports: [
        {
          id: 2,
          reportedBy: 'Ivan Milić',
          timestamp: '2025-01-14 09:25',
          description: 'Driver was speeding and ran a red light.'
        }
      ],
      averageRating: 3.0,
      ratings: [
        { id: 6, passengerName: 'Ivan Milić', rating: 3, comment: 'Fast but unsafe driving.' }
      ]
    },
    {
      id: 6,
      route: 'Novi Sad Airport → City Center',
      pickupLocation: 'Aerodrom, Novi Sad',
      destinationLocation: 'Trg Slobode, Novi Sad',
      startDate: '2025-01-13',
      endDate: '2025-01-13',
      startTime: '18:30',
      endTime: '18:55',
      price: 850.00,
      status: 'Completed',
      duration: '25min',
      driverName: 'Ana Jovanović',
      driverPhone: '+381 63 987 6543',
      vehicleType: 'Premium - BMW 5 Series',
      licensePlate: 'BG-111-AA',
      distance: 12.5,
      passengerCount: 1,
      passengers: [
        { id: 9, name: 'David Smith', email: 'david.smith@example.com', phone: '+1 555 123 4567' }
      ],
      hasPanic: false,
      reportCount: 0,
      averageRating: 5.0,
      ratings: [
        { id: 7, passengerName: 'David Smith', rating: 5, comment: 'Perfect airport transfer, very professional!' }
      ]
    }
  ];
  
  // Filtered rides (initially show all, sorted by date desc)
  filteredRides: AdminRide[] = [...this.allRides];

  constructor(private router: Router) {
    this.sortRides();
  }

  // Apply all filters
  applyFilters(): void {
    this.filteredRides = this.allRides.filter(ride => {
      // Date filter
      if (this.startDate || this.endDate) {
        const rideDate = new Date(ride.startDate);
        const start = this.startDate ? new Date(this.startDate) : null;
        const end = this.endDate ? new Date(this.endDate) : null;

        if (start && end) {
          if (rideDate < start || rideDate > end) return false;
        } else if (start) {
          if (rideDate < start) return false;
        } else if (end) {
          if (rideDate > end) return false;
        }
      }

      // Status filter
      if (this.selectedStatus !== 'all' && ride.status !== this.selectedStatus) {
        return false;
      }

      // User search filter
      if (this.searchUser.trim() !== '') {
        const searchLower = this.searchUser.toLowerCase();
        const driverMatch = ride.driverName.toLowerCase().includes(searchLower);
        const passengerMatch = ride.passengers?.some(p => 
          p.name.toLowerCase().includes(searchLower) || 
          p.email.toLowerCase().includes(searchLower)
        );
        if (!driverMatch && !passengerMatch) return false;
      }

      return true;
    });

    this.sortRides();
  }

  // Reset all filters
  resetFilters(): void {
    this.startDate = '';
    this.endDate = '';
    this.selectedUserType = 'all';
    this.searchUser = '';
    this.selectedStatus = 'all';
    this.filteredRides = [...this.allRides];
    this.sortRides();
  }

  // Sort rides
  sortBy(field: SortField): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'desc';
    }
    this.sortRides();
  }

  private sortRides(): void {
    this.filteredRides.sort((a, b) => {
      let aValue: any;
      let bValue: any;

      switch (this.sortField) {
        case 'route':
          aValue = a.route.toLowerCase();
          bValue = b.route.toLowerCase();
          break;
        case 'startDate':
          aValue = new Date(a.startDate + ' ' + a.startTime);
          bValue = new Date(b.startDate + ' ' + b.startTime);
          break;
        case 'price':
          aValue = a.price;
          bValue = b.price;
          break;
        case 'status':
          aValue = a.status;
          bValue = b.status;
          break;
        default:
          return 0;
      }

      if (aValue < bValue) return this.sortDirection === 'asc' ? -1 : 1;
      if (aValue > bValue) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
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

  // View ride on map
  viewOnMap(ride: AdminRide): void {
    // Navigate to map view with ride details
    this.router.navigate(['/admin/map-view'], { 
      queryParams: { 
        rideId: ride.id,
        pickup: ride.pickupLocation,
        destination: ride.destinationLocation
      }
    });
  }

  // Status class helper
  getStatusClass(status: string): string {
    switch (status.toLowerCase()) {
      case 'completed':
        return 'bg-green-100 text-green-800';
      case 'cancelled':
        return 'bg-red-100 text-red-800';
      case 'active':
        return 'bg-blue-100 text-blue-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }
}