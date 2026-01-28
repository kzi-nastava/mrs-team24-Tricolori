import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import {
  heroMapPin,
  heroClock,
  heroUser,
  heroXMark,
  heroCheckCircle,
  heroPhone
} from '@ng-icons/heroicons/outline';

interface RideOffer {
  id: number;
  pickupAddress: string;
  destinationAddress: string;
  passengerName: string;
  passengerPhone: string;
  estimatedDistance: number;
  estimatedDuration: number;
  estimatedPrice: number;
  pickupCoords: [number, number];
  destinationCoords: [number, number];
}

@Component({
  selector: 'app-driver-waiting',
  standalone: true,
  imports: [CommonModule, NgIconComponent],
  providers: [
    provideIcons({
      heroMapPin,
      heroClock,
      heroUser,
      heroXMark,
      heroCheckCircle,
      heroPhone
    })
  ],
  templateUrl: './driver-waiting.html'
})
export class DriverWaitingComponent implements OnInit {
  showRideOfferModal = signal<boolean>(false);
  
  // Mock ride offer - this will be replaced with real data from your service
  rideOffer = signal<RideOffer>({
    id: 1,
    pickupAddress: 'Trg Slobode 1, Novi Sad',
    destinationAddress: 'Kisačka 71, Novi Sad',
    passengerName: 'Ana Jovanović',
    passengerPhone: '+381 64 123 4567',
    estimatedDistance: 2.3,
    estimatedDuration: 8,
    estimatedPrice: 334,
    pickupCoords: [45.2671, 19.8335],
    destinationCoords: [45.2550, 19.8450]
  });

  // Countdown timer for accepting ride (in seconds)
  countdownSeconds = signal<number>(30);
  private countdownInterval: any = null;

  constructor(private router: Router) {}

  ngOnInit(): void {
    // Simulate ride offer coming in after 3 seconds
    setTimeout(() => {
      this.showRideOffer();
    }, 3000);
  }

  ngOnDestroy(): void {
    this.stopCountdown();
  }

  private showRideOffer(): void {
    this.showRideOfferModal.set(true);
    this.startCountdown();
  }

  private startCountdown(): void {
    this.countdownSeconds.set(30);
    
    this.countdownInterval = setInterval(() => {
      const current = this.countdownSeconds();
      if (current <= 1) {
        // Time's up - auto decline
        this.declineRide();
      } else {
        this.countdownSeconds.set(current - 1);
      }
    }, 1000);
  }

  private stopCountdown(): void {
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
      this.countdownInterval = null;
    }
  }

  acceptRide(): void {
    this.stopCountdown();
    this.showRideOfferModal.set(false);
    
    // Navigate to ride tracking page
    // const rideId = this.rideOffer().id;
    this.router.navigate(['/driver/ride-tracking', 6]); // Using 6 as a placeholder ride ID
  }

  declineRide(): void {
    this.stopCountdown();
    this.showRideOfferModal.set(false);
    
    // In a real app, you would notify the backend that the ride was declined
    console.log('Ride declined');
  }
}