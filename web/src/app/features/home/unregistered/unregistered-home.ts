import { Component, OnInit, AfterViewInit, OnDestroy, signal, computed, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIcon, provideIcons } from '@ng-icons/core';
import {
  heroArrowLeft, heroMapPin, heroCalculator, heroStar,
  heroSparkles, heroInformationCircle, heroCurrencyDollar,
  heroClock, heroArrowPath
} from '@ng-icons/heroicons/outline';

import * as L from 'leaflet';
import 'leaflet-routing-machine';

import { RideEstimationInitial } from '../../../shared/components/ride-estimation/ride-estimation-initial/ride-estimation-initial';
import { RideEstimationForm } from '../../../shared/components/ride-estimation/ride-estimation-form/ride-estimation-form';
import { RideEstimationResult } from '../../../shared/components/ride-estimation/ride-estimation-result/ride-estimation-result';

// Model
import { EstimateResults, Vehicle, EstimationState } from '../../../shared/model/ride-estimation';

@Component({
  selector: 'app-unregistered-home',
  standalone: true,
  imports: [
    CommonModule,
    NgIcon,
    RideEstimationInitial,
    RideEstimationForm,
    RideEstimationResult
  ],
  templateUrl: './unregistered-home.html',
  styleUrl: './unregistered-home.css',
  viewProviders: [provideIcons({
    heroArrowLeft, heroMapPin, heroCalculator, heroStar,
    heroSparkles, heroInformationCircle, heroCurrencyDollar,
    heroClock, heroArrowPath
  })]
})
export class UnregisteredHome implements OnInit, AfterViewInit, OnDestroy {
  private cdr = inject(ChangeDetectorRef);

  // --- SIGNAL (State Management) ---
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

  // --- MAP PROPERTIES ---
  private map!: L.Map;
  private routeControl: any;
  private pickupMarker: any;
  private destinationMarker: any;
  private vehicleMarkers: L.Layer[] = [];

  ngOnInit(): void {
    const DefaultIcon = L.icon({
      iconUrl: 'https://unpkg.com/leaflet@1.6.0/dist/images/marker-icon.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.6.0/dist/images/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      shadowSize: [41, 41]
    });
    L.Marker.prototype.options.icon = DefaultIcon;
  }

  ngAfterViewInit(): void {
    // Timeout osigurava da je DOM spreman za Leaflet
    setTimeout(() => this.initMap(), 100);
  }

  ngOnDestroy(): void {
    if (this.map) this.map.remove();
  }

  // --- NAVIGATION ---

  handleStart(): void {
    this.currentState.set('FORM');
  }

  handleBack(): void {
    if (this.currentState() === 'RESULT') {
      this.clearRouteMarkers();
      this.currentState.set('FORM');
    } else {
      this.currentState.set('INITIAL');
      this.errorMessage.set('');
    }
  }

  handleReset(): void {
    this.clearRouteMarkers();
    this.estimateResults.set(null);
    this.errorMessage.set('');
    this.currentState.set('INITIAL');
    this.map.setView([45.2671, 19.8335], 13);
  }

  // --- MAP & ROUTING ---

  private initMap(): void {
    try {
      this.map = L.map('map', {
        center: [45.2671, 19.8335],
        zoom: 13,
      });

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 18,
        minZoom: 3,
        attribution: '&copy; OpenStreetMap contributors',
      }).addTo(this.map);

      this.addVehicleMarkers();
    } catch (error) {
      this.errorMessage.set('Failed to load map. Please refresh the page.');
    }
  }

  private addVehicleMarkers(): void {
    this.vehicles().forEach(vehicle => {
      const iconColor = vehicle.status === 'available' ? '#10b981' : '#ef4444';
      const marker = L.circleMarker([vehicle.lat, vehicle.lng], {
        radius: 8,
        fillColor: iconColor,
        color: '#ffffff',
        weight: 2,
        fillOpacity: 0.8
      }).addTo(this.map);

      marker.bindPopup(`<strong>Vehicle #${vehicle.id}</strong><br>Status: ${vehicle.status}`);
      this.vehicleMarkers.push(marker);
    });
  }

  async onEstimate(data: { pickup: string, destination: string }) {
    this.errorMessage.set('');

    try {
      const pickupCoords = await this.geocodeAddress(data.pickup);
      const destCoords = await this.geocodeAddress(data.destination);

      if (!pickupCoords) {
        this.errorMessage.set('Could not find pickup location.');
        return;
      }
      if (!destCoords) {
        this.errorMessage.set('Could not find destination.');
        return;
      }

      this.calculateRoute(pickupCoords, destCoords, data.pickup, data.destination);
    } catch (error) {
      this.errorMessage.set('Error finding addresses. Please try again.');
    }
  }

  private async geocodeAddress(address: string): Promise<L.LatLng | null> {
    const response = await fetch(
      `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}, Novi Sad`
    );
    const results = await response.json();
    return results.length > 0 ? L.latLng(parseFloat(results[0].lat), parseFloat(results[0].lon)) : null;
  }

  private calculateRoute(pickupCoords: L.LatLng, destCoords: L.LatLng, pickup: string, destination: string): void {
    this.clearRouteMarkers();

    const pickupIcon = L.divIcon({
      className: 'custom-marker-icon',
      html: `<div style="background: #00acc1; width: 16px; height: 16px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
      iconSize: [22, 22],
      iconAnchor: [11, 11]
    });

    const destinationIcon = L.divIcon({
      className: 'custom-marker-icon',
      html: `<div style="background: #ec407a; width: 16px; height: 16px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
      iconSize: [22, 22],
      iconAnchor: [11, 11]
    });

    this.pickupMarker = L.marker(pickupCoords, { icon: pickupIcon }).addTo(this.map);
    this.destinationMarker = L.marker(destCoords, { icon: destinationIcon }).addTo(this.map);

    this.routeControl = L.Routing.control({
      waypoints: [pickupCoords, destCoords],
      router: L.Routing.osrmv1({ serviceUrl: 'https://router.project-osrm.org/route/v1' }),
      lineOptions: {
        styles: [{ color: '#00acc1', opacity: 0.7, weight: 4 }],
        extendToWaypoints: false,
        missingRouteTolerance: 0
      },
      show: false,
      addWaypoints: false,
      fitSelectedRoutes: true
    }).addTo(this.map);

    this.routeControl.on('routesfound', (e: any) => {
      const summary = e.routes[0].summary;
      const distanceKm = summary.totalDistance / 1000;
      const durationMin = Math.round(summary.totalTime / 60);

      this.estimateResults.set({
        pickup,
        destination,
        distance: parseFloat(distanceKm.toFixed(1)),
        duration: durationMin
      });

      this.currentState.set('RESULT');
      this.cdr.detectChanges();
    });

    this.routeControl.on('routingerror', () => {
      this.errorMessage.set('Could not calculate route. Try different addresses.');
      this.cdr.detectChanges();
    });
  }

  private clearRouteMarkers(): void {
    if (this.routeControl) {
      this.map.removeControl(this.routeControl);
      this.routeControl = null;
    }
    if (this.pickupMarker) {
      this.map.removeLayer(this.pickupMarker);
      this.pickupMarker = null;
    }
    if (this.destinationMarker) {
      this.map.removeLayer(this.destinationMarker);
      this.destinationMarker = null;
    }
  }
}
