import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouteSelector } from '../../../shared/components/ride-booking/route-selector/route-selector';
import { RideTrackersSelector } from '../../../shared/components/ride-booking/ride-trackers-selector/ride-trackers-selector';
import { PreferencesSelector } from '../../../shared/components/ride-booking/preferences-selector/preferences-selector';
import { FavoriteRouteSelector } from '../../../shared/components/ride-booking/favorite-route-selector/favorite-route-selector';
import { FavoriteRoute, Route } from '../../../shared/model/route';

interface Stop {
  id: number;
  location: string;
}

@Component({
  selector: 'app-home-passenger',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule,

    RouteSelector,
    RideTrackersSelector,
    PreferencesSelector,
    FavoriteRouteSelector
  ],
  templateUrl: './passenger-home.html',
  styleUrl: './passenger-home.css'
})
export class HomePassenger {
  currentRouteSignal = signal<Route | undefined>(undefined);

  /* ----------------------------- */
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

  /* ------------------------------ */
  populateFavoriteRoute(route: FavoriteRoute) {
    this.currentRouteSignal.set({
      from: route.from,
      stops: route.stops,
      to: route.to
    });
  }
}