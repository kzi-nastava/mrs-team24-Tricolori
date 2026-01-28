import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIcon, provideIcons } from '@ng-icons/core';
import {
  heroArrowLeft, heroArrowPath, heroCalculator, heroClock,
  heroCurrencyDollar, heroInformationCircle, heroMapPin, heroSparkles, heroStar
} from '@ng-icons/heroicons/outline';

import { RideEstimationInitial } from './components/ride-estimation/ride-estimation-initial/ride-estimation-initial';
import { RideEstimationForm } from './components/ride-estimation/ride-estimation-form/ride-estimation-form';
import { RideEstimationResult } from './components/ride-estimation/ride-estimation-result/ride-estimation-result';

import { EstimateResults, EstimationState } from '../../../model/ride-estimation';
import { Vehicle } from '../../../model/vehicle.model';
import { MapService } from '../../../services/map.service';
import { GeocodingService } from '../../../services/geocoding.service';
import { EstimationService } from '../../../services/estimation.service';
import { VehicleService } from '../../../services/vehicle.service';
import { Map } from '../../../components/map/map';

@Component({
  selector: 'app-unregistered-home',
  standalone: true,
  imports: [CommonModule, NgIcon, RideEstimationInitial, RideEstimationForm, RideEstimationResult, Map],
  templateUrl: './unregistered-home.html',
  styleUrl: './unregistered-home.css',
  viewProviders: [provideIcons({
    heroArrowLeft, heroMapPin, heroCalculator, heroStar,
    heroSparkles, heroInformationCircle, heroCurrencyDollar,
    heroClock, heroArrowPath
  })]
})
export class UnregisteredHome implements OnInit {
  private mapService = inject(MapService);
  private geocodingService = inject(GeocodingService);
  private estimationService = inject(EstimationService);
  private vehicleService = inject(VehicleService);

  // State signals
  currentState = signal<EstimationState>('INITIAL');
  errorMessage = signal<string>('');
  estimateResults = signal<EstimateResults | null>(null);
  isLoadingVehicles = signal<boolean>(false);

  // Data signals - These are passed to the <app-map> component via [vehicles]="vehicles()"
  vehicles = signal<Vehicle[]>([]);

  // Computed state
  availableDrivers = computed(() => this.vehicles().filter(v => v.available).length);
  averageRating = signal(4.9);

  ngOnInit(): void {
    this.loadVehicles();
  }

  /**
   * Fetches active vehicles from the backend.
   * Updating this signal automatically updates the map via the <app-map> input effect.
   */
  loadVehicles(): void {
  this.isLoadingVehicles.set(true);

  this.vehicleService.getActiveVehicles().subscribe({
    next: (vehicles) => {
      this.vehicles.set(vehicles);

      // FORCE redraw AFTER map is already created
      this.mapService.updateVehicleMarkers(vehicles);

      this.isLoadingVehicles.set(false);
    },
    error: (error) => {
      console.error('Error loading vehicles:', error);
      this.isLoadingVehicles.set(false);
      this.errorMessage.set('Failed to load vehicles. Please try again.');
    }
  });
}

  // Navigation Logic
  handleStart(): void {
    this.currentState.set('FORM');
  }

  handleBack(): void {
    if (this.currentState() === 'RESULT') {
      // Clear the route from the map, vehicles stay because they are bound to the signal
      this.mapService.clearRouteAndMarkers();
      this.currentState.set('FORM');
    } else {
      this.currentState.set('INITIAL');
      this.errorMessage.set('');
    }
  }

  handleReset(): void {
    this.mapService.clearRouteAndMarkers();
    this.estimateResults.set(null);
    this.errorMessage.set('');
    this.currentState.set('INITIAL');

    // Reset map view to default city center
    this.mapService.centerMap([45.2671, 19.8335], 13);
  }

  /**
   * Handles the estimation logic: Geocoding -> Route Calculation -> Map Drawing
   */
  async onEstimate(data: { pickup: string, destination: string }): Promise<void> {
    this.errorMessage.set('');

    try {
      const pickupResult = await this.geocodingService.geocodeAddress(data.pickup);
      const destResult = await this.geocodingService.geocodeAddress(data.destination);

      if (!pickupResult || !destResult) {
        this.errorMessage.set('Could not find one of the locations.');
        return;
      }

      const estimation = await this.estimationService.calculateRoute(
        { lat: pickupResult.lat, lng: pickupResult.lng },
        { lat: destResult.lat, lng: destResult.lng },
        data.pickup,
        data.destination
      );

      if (!estimation) {
        this.errorMessage.set('Could not calculate route.');
        return;
      }

      // Delegate route drawing to the service
      this.mapService.drawRoute(
        '',
        [pickupResult.lat, pickupResult.lng],
        [destResult.lat, destResult.lng],
        estimation.routeCoordinates
      );

      this.estimateResults.set({
        pickup: data.pickup,
        destination: data.destination,
        distance: estimation.distance,
        duration: estimation.duration
      });

      this.currentState.set('RESULT');

    } catch (error) {
      this.errorMessage.set('An error occurred during estimation.');
      console.error('Estimation error:', error);
    }
  }
}
