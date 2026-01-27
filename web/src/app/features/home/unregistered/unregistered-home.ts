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

import {EstimateResults, EstimationState} from '../../../shared/model/ride-estimation';
import {Vehicle} from '../../../shared/model/vehicle.model';
import {MapService} from '../../../core/services/map.service';
import {GeocodingService} from '../../../core/services/geocoding.service';
import {EstimationService} from '../../../core/services/estimation.service';
import {VehicleService} from '../../../core/services/vehicle.service';

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
  private vehicleService = inject(VehicleService);

  // State
  currentState = signal<EstimationState>('INITIAL');
  errorMessage = signal<string>('');
  estimateResults = signal<EstimateResults | null>(null);
  isLoadingVehicles = signal<boolean>(false);

  vehicles = signal<Vehicle[]>([]);

  availableDrivers = computed(() => this.vehicles().filter(v => v.available).length);
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

    // Load vehicles on init
    this.loadVehicles();
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

  // Load vehicles from backend
loadVehicles(): void {
  console.log('🚗 Starting to load vehicles...');
  this.isLoadingVehicles.set(true);
  this.vehicleService.getActiveVehicles().subscribe({
    next: (vehicles) => {
      console.log('✅ Vehicles loaded successfully:', vehicles);
      console.log('📊 Number of vehicles:', vehicles.length);
      console.log('🔍 First vehicle details:', vehicles[0]);
      
      this.vehicles.set(vehicles);
      this.isLoadingVehicles.set(false);
      
      console.log('🗺️ Map initialized?', !!this.mapService.getMap());
      
      // Refresh markers if map is already initialized
      if (this.mapService.getMap()) {
        console.log('🔄 Refreshing vehicle markers...');
        this.refreshVehicleMarkers();
      }
    },
    error: (error) => {
      console.error('❌ Error loading vehicles:', error);
      console.error('📋 Error details:', JSON.stringify(error, null, 2));
      this.isLoadingVehicles.set(false);
      this.errorMessage.set('Failed to load vehicles. Please try again.');
    }
  });
}

  // Refresh vehicle markers
  refreshVehicleMarkers(): void {
    this.mapService.clearMap();
    this.addVehicleMarkers();
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
  console.log('📍 Adding vehicle markers...');
  const map = this.mapService.getMap();
  console.log('🗺️ Map object:', map);
  console.log('🚗 Total vehicles to add:', this.vehicles().length);
  
  this.vehicles().forEach((vehicle, index) => {
    console.log(`🚙 Adding marker ${index + 1}:`, {
      id: vehicle.vehicleId,
      model: vehicle.model,
      lat: vehicle.latitude,
      lng: vehicle.longitude,
      available: vehicle.available
    });
    
    const iconColor = vehicle.available ? '#10b981' : '#ef4444';
    const marker = L.circleMarker([vehicle.latitude, vehicle.longitude], {
      radius: 8,
      fillColor: iconColor,
      color: '#ffffff',
      weight: 2,
      fillOpacity: 0.8
    }).addTo(map);

    const statusText = vehicle.available ? 'Available' : 'Occupied';
    marker.bindPopup(`
      <strong>${vehicle.model}</strong><br>
      Plate: ${vehicle.plateNum}<br>
      Status: ${statusText}
    `);
  });
  
  console.log('✅ All markers added successfully!');
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
