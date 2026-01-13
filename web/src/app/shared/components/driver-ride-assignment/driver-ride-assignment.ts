import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIcon, provideIcons } from '@ng-icons/core';
import {
  heroMapPin,
  heroFlag,
  heroClock,
  heroCheck,
  heroXMark,
  heroArrowLeft
} from '@ng-icons/heroicons/outline';

@Component({
  selector: 'app-driver-ride-assignment',
  standalone: true,
  imports: [CommonModule, NgIcon],
  templateUrl: './driver-ride-assignment.html',
  viewProviders: [provideIcons({
    heroMapPin, heroFlag, heroClock, heroCheck, heroXMark, heroArrowLeft
  })]
})
export class DriverRideAssignment {
  // Ovo bi u realnosti stizalo preko @Input-a ili servisa
  activeRide = signal({
    pickup: 'Kraljev park',
    destination: 'Jevrejska 23b',
    eta: 5
  });

  handleBack() {
    console.log('Navigating back...');
  }

  handleCancel() {
    console.log('Opening cancellation modal...');
  }

  handleStartRide() {
    console.log('Starting the ride...');
  }
}
