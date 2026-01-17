import {AfterViewInit, Component, computed, inject, OnDestroy, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {NgIcon, provideIcons} from '@ng-icons/core';
import {heroArrowLeft, heroArrowPath, heroCalculator, heroClock,
  heroCurrencyDollar, heroInformationCircle, heroMapPin, heroSparkles, heroStar
} from '@ng-icons/heroicons/outline';

import * as L from 'leaflet';

import {
  RideEstimationInitial
} from '../../../shared/components/ride-estimation/ride-estimation-initial/ride-estimation-initial';
import {RideEstimationForm} from '../../../shared/components/ride-estimation/ride-estimation-form/ride-estimation-form';
import {
  RideEstimationResult
} from '../../../shared/components/ride-estimation/ride-estimation-result/ride-estimation-result';

import {EstimateResults, EstimationState, Vehicle} from '../../../shared/model/ride-estimation';
import {MapService} from '../../../core/services/map.service';
import {GeocodingService} from '../../../core/services/geocoding.service';
import {EstimationService} from '../../../core/services/estimation.service';

@Component({
  selector: 'app-unregistered-home',
  standalone: true,
  imports: [CommonModule, NgIcon, RideEstimationInitial, RideEstimationForm, RideEstimationResult],
  templateUrl: './unregistered-home.html',
  styleUrl: './unregistered-home.css',
  viewProviders: [provideIcons({
    heroArrowLeft, heroMapPin, heroCalculator, heroStar,
    heroSparkles, heroInformationCircle, heroCurrencyDollar,
    heroClock, heroArrowPath
  })]
})
export class UnregisteredHome implements OnInit, AfterViewInit, OnDestroy {
  private mapService = inject(MapService);
  private geocodingService = inject(GeocodingService);
  private estimationService = inject(EstimationService);

  // State
  currentState = signal<EstimationState>('INITIAL');
  errorMessage = signal<string>('');
  estimateResults = signal<EstimateResults | null>(null);

  vehicles = signal<Vehicle[]>([
    { id: 1, lat: 45.2705, lng: 19.8250, status: 'available' },
    { id: 2, lat: 45.2640, lng: 19.8402, status: 'occupied' },
    { id: 3, lat: 45.2608, lng: 19.8301, status: 'available' },
    { id: 4, lat: 45.2732, lng: 19.8454, status: 'available' },
    { id: 5, lat: 45.2689, lng: 19.8206, status: 'occupied' },
    { id: 6, lat: 45.2650, lng: 19.8380, status: 'available' },
    { id: 7, lat: 45.2720, lng: 19.8290, status: 'occupied' },
    { id: 8, lat: 45.2595, lng: 19.8350, status: 'available' },
  ]);

  availableDrivers = computed(() => this.vehicles().filter(v => v.status === 'available').length);
  averageRating = signal(4.9);

  ngOnInit(): void {
    // Set default Leaflet icon
    L.Marker.prototype.options.icon = L.icon({
      iconUrl: 'https://unpkg.com/leaflet@1.6.0/dist/images/marker-icon.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.6.0/dist/images/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      shadowSize: [41, 41]
    });
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.mapService.initMap('map');
      this.addVehicleMarkers();
    }, 100);
  }

  ngOnDestroy(): void {
    this.mapService.destroyMap();
  }

  // Navigation
  handleStart(): void {
    this.currentState.set('FORM');
  }

  handleBack(): void {
    if (this.currentState() === 'RESULT') {
      this.mapService.clearMap();
      this.addVehicleMarkers();
      this.currentState.set('FORM');
    } else {
      this.currentState.set('INITIAL');
      this.errorMessage.set('');
    }
  }

  handleReset(): void {
    this.mapService.clearMap();
    this.estimateResults.set(null);
    this.errorMessage.set('');
    this.currentState.set('INITIAL');
    this.mapService.centerMap([45.2671, 19.8335], 13);
    this.addVehicleMarkers();
  }

  // Vehicle markers
  private addVehicleMarkers(): void {
    const map = this.mapService.getMap();
    this.vehicles().forEach(vehicle => {
      const iconColor = vehicle.status === 'available' ? '#10b981' : '#ef4444';
      const marker = L.circleMarker([vehicle.lat, vehicle.lng], {
        radius: 8,
        fillColor: iconColor,
        color: '#ffffff',
        weight: 2,
        fillOpacity: 0.8
      }).addTo(map);

      marker.bindPopup(`<strong>Vehicle #${vehicle.id}</strong><br>Status: ${vehicle.status}`);
    });
  }

  // Estimation
  async onEstimate(data: { pickup: string, destination: string }): Promise<void> {
    this.errorMessage.set('');

    try {
      // Geocode addresses
      const pickupResult = await this.geocodingService.geocodeAddress(data.pickup);
      const destResult = await this.geocodingService.geocodeAddress(data.destination);

      if (!pickupResult) {
        this.errorMessage.set('Could not find pickup location.');
        return;
      }
      if (!destResult) {
        this.errorMessage.set('Could not find destination.');
        return;
      }

      // Calculate route
      const estimation = await this.estimationService.calculateRoute(
        { lat: pickupResult.lat, lng: pickupResult.lng },
        { lat: destResult.lat, lng: destResult.lng },
        data.pickup,
        data.destination
      );

      if (!estimation) {
        this.errorMessage.set('Could not calculate route. Try different addresses.');
        return;
      }

      // Draw route on map
      this.mapService.drawRoute(
        '',
        [pickupResult.lat, pickupResult.lng],
        [destResult.lat, destResult.lng],
        estimation.routeCoordinates
      );

      // Set results
      this.estimateResults.set({
        pickup: data.pickup,
        destination: data.destination,
        distance: estimation.distance,
        duration: estimation.duration
      });

      this.currentState.set('RESULT');

    } catch (error) {
      this.errorMessage.set('Error calculating route. Please try again.');
      console.error('Estimation error:', error);
    }
  }
}
