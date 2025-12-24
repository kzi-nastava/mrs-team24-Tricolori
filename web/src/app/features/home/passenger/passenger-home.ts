import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Stop {
  id: number;
  location: string;
}

@Component({
  selector: 'app-home-passenger',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './passenger-home.html',
  styleUrl: './passenger-home.css'
})
export class HomePassenger {
  pickupLocation = '';
  stops: Stop[] = [];
  destination = '';
  nextStopId = 1;
  
  vehicleType = 'economy';
  babySeat = false;
  petFriendly = false;
  scheduleForLater = false;
  maxHoursAdvance = 5;

  vehicleTypes = [
    { value: 'economy', label: 'Economy', icon: '🚗' },
    { value: 'comfort', label: 'Comfort', icon: '🚙' },
    { value: 'premium', label: 'Premium', icon: '🚕' },
    { value: 'van', label: 'Van', icon: '🚐' }
  ];

  addStop() {
    if (this.stops.length < 5) {
      this.stops.push({ id: this.nextStopId++, location: '' });
    }
  }

  removeStop(id: number) {
    this.stops = this.stops.filter(stop => stop.id !== id);
  }

  bookRide() {
    if (!this.pickupLocation || !this.destination) {
      alert('Please enter pickup location and destination');
      return;
    }

    const rideData = {
      pickupLocation: this.pickupLocation,
      stops: this.stops.filter(s => s.location.trim()),
      destination: this.destination,
      vehicleType: this.vehicleType,
      babySeat: this.babySeat,
      petFriendly: this.petFriendly,
      scheduleForLater: this.scheduleForLater,
      maxHoursAdvance: this.scheduleForLater ? this.maxHoursAdvance : null
    };

    console.log('Booking ride:', rideData);
    alert('Searching for available drivers...');
    // TODO: Send to backend
  }
}