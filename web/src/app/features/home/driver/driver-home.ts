import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

interface RideRequest {
  id: string;
  pickupLocation: string;
  destination: string;
  timeToPickup: number;
  passengerName?: string;
  estimatedFare?: number;
}

@Component({
  selector: 'app-home-driver',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './driver-home.html',
  styleUrl: './driver-home.css'
})
export class HomeDriver implements OnInit {
  currentRequest: RideRequest | null = null;
  showAcceptDialog = false;

  ngOnInit() {
    // Simulate receiving a ride request
    this.simulateRideRequest();
  }

  simulateRideRequest() {
    // Simulate a new ride request
    setTimeout(() => {
      this.currentRequest = {
        id: '1',
        pickupLocation: 'Krajev park',
        destination: 'Jevrejska 23b',
        timeToPickup: 5,
        passengerName: 'John Doe',
        estimatedFare: 450
      };
      this.showAcceptDialog = true;
    }, 1000);
  }

  acceptRide() {
    console.log('Ride accepted:', this.currentRequest);
    this.showAcceptDialog = false;
    // TODO: Navigate to active ride page
    alert('Ride accepted! Navigation to pickup location...');
  }

  declineRide() {
    console.log('Ride declined:', this.currentRequest);
    this.showAcceptDialog = false;
    this.currentRequest = null;
    // Simulate next request
    setTimeout(() => this.simulateRideRequest(), 3000);
  }
}