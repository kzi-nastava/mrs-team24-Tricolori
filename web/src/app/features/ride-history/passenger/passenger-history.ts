import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { heroEye, heroXMark, heroStar } from '@ng-icons/heroicons/outline';
import { heroStarSolid } from '@ng-icons/heroicons/solid';

interface PassengerRide {
  id: number;
  route: string;
  startDate: string;
  endDate: string;
  price: number;
  status: 'Completed' | 'Cancelled' | 'Pending';
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
export class PassengerHistory {
  // Date filter properties
  startDate: string = '';
  endDate: string = '';
  
  // Modal property
  selectedRide: PassengerRide | null = null;
  
  // All rides data
  allRides: PassengerRide[] = [
    {
      id: 1,
      route: 'Kraljev park → Kosovska 21',
      startDate: '2024-12-15',
      endDate: '2024-12-15',
      price: 232.70,
      status: 'Completed',
      startTime: '09:30 AM',
      endTime: '09:35 AM',
      duration: '5min',
      driverName: 'Marko Petrović',
      driverPhone: '+381 64 123 4567',
      vehicleType: 'Economy - Toyota Corolla',
      licensePlate: 'NS-123-AB',
      distance: 1.9,
      paymentMethod: 'Credit Card',
      notes: 'Pleasant ride, driver was very professional.',
      rating: {
        driverRating: 5,
        vehicleRating: 5,
        comment: 'Excellent service!',
        ratedAt: '2024-12-15'
      },
      completedAt: new Date('2024-12-15T09:35:00'),
      canRate: false,
      ratingExpired: false
    },
    {
      id: 2,
      route: 'Bulevar Oslobođenja 30 → Trg Slobode',
      startDate: '2024-12-14',
      endDate: '2024-12-14',
      price: 325.80,
      status: 'Completed',
      startTime: '14:00 PM',
      endTime: '14:05 PM',
      duration: '5min',
      driverName: 'Ana Jovanović',
      driverPhone: '+381 63 987 6543',
      vehicleType: 'Comfort - Honda Accord',
      licensePlate: 'BG-456-CD',
      distance: 2.5,
      paymentMethod: 'Cash',
      completedAt: new Date(Date.now() - 24 * 60 * 60 * 1000), // 1 day ago
      canRate: true,
      ratingExpired: false
    },
    {
      id: 3,
      route: 'Železnička stanica → Limanski park',
      startDate: '2024-12-13',
      endDate: '2024-12-13',
      price: 512.00,
      status: 'Completed',
      startTime: '08:00 AM',
      endTime: '08:04 AM',
      duration: '4min',
      driverName: 'Stefan Nikolić',
      driverPhone: '+381 65 555 1234',
      vehicleType: 'Premium - Mercedes E-Class',
      licensePlate: 'NS-789-EF',
      distance: 4.2,
      paymentMethod: 'Credit Card',
      completedAt: new Date(Date.now() - 48 * 60 * 60 * 1000), // 2 days ago
      canRate: true,
      ratingExpired: false
    },
    {
      id: 4,
      route: 'Spens → Petrovaradinska tvrđava',
      startDate: '2024-12-12',
      endDate: '2024-12-12',
      price: 395.60,
      status: 'Cancelled',
      startTime: '16:00 PM',
      endTime: '16:00 PM',
      duration: '0min',
      driverName: 'Jelena Đorđević',
      driverPhone: '+381 64 222 3333',
      vehicleType: 'XL - Toyota Sienna',
      licensePlate: 'BG-321-GH',
      distance: 0,
      paymentMethod: 'N/A',
      notes: 'Ride cancelled by me due to change of plans.',
      completedAt: new Date('2024-12-12T16:00:00'),
      canRate: false,
      ratingExpired: false
    },
    {
      id: 5,
      route: 'Grbavica → Spens',
      startDate: '2024-12-08',
      endDate: '2024-12-08',
      price: 250.00,
      status: 'Completed',
      startTime: '11:00 AM',
      endTime: '11:05 AM',
      duration: '5min',
      driverName: 'Milan Stojanović',
      driverPhone: '+381 66 777 8888',
      vehicleType: 'Economy - Volkswagen Golf',
      licensePlate: 'NS-654-IJ',
      distance: 2.1,
      paymentMethod: 'Cash',
      completedAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000), // 5 days ago
      canRate: false,
      ratingExpired: true
    }
  ];
  
  // Filtered rides (initially show all)
  filteredRides: PassengerRide[] = [...this.allRides];

  constructor(private router: Router) {}

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

  // Navigate to rating page
  navigateToRating(rideId: number): void {
    this.router.navigate(['/rate-ride', rideId]);
  }

  // Get hours remaining for rating
  getHoursRemaining(ride: PassengerRide): number {
    const now = new Date();
    const completedAt = new Date(ride.completedAt);
    const hoursSinceCompleted = (now.getTime() - completedAt.getTime()) / (1000 * 60 * 60);
    return Math.max(0, 72 - Math.floor(hoursSinceCompleted));
  }

  // Check if rating deadline is approaching (less than 24 hours)
  isRatingDeadlineNear(ride: PassengerRide): boolean {
    const hoursRemaining = this.getHoursRemaining(ride);
    return hoursRemaining > 0 && hoursRemaining <= 24;
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

  // Get rating stars array for display
  getRatingStars(rating: number): number[] {
    return Array(5).fill(0).map((_, i) => i + 1);
  }

  // Check if star should be filled
  isStarFilled(starNumber: number, rating: number): boolean {
    return starNumber <= rating;
  }
}