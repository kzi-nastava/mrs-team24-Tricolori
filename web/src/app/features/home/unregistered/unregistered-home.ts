import { Component, OnInit, AfterViewInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { NgIcon } from '@ng-icons/core';
import * as L from 'leaflet';
import 'leaflet-routing-machine';

interface EstimateResults {
  pickup: string;
  destination: string;
  distance: number;
  duration: number;
}

interface RideOption {
  type: string;
  icon: string;
  eta: string;
  price: string;
  seats: string;
}

interface Vehicle {
  id: number;
  lat: number;
  lng: number;
  status: 'available' | 'occupied';
}

@Component({
  selector: 'app-unregistered-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    ReactiveFormsModule,
    NgIcon
  ],
  templateUrl: './unregistered-home.html',
  styleUrl: './unregistered-home.css',
})
export class UnregisteredHome implements OnInit, AfterViewInit, OnDestroy {
  private map: any;
  private routeControl: any;
  private pickupMarker: any;
  private destinationMarker: any;
  private vehicleMarkers: any[] = [];
  
  showEstimateForm: boolean = false;
  estimateResults: EstimateResults | null = null;
  errorMessage: string = '';
  
  availableDrivers: number = 0;
  averageRating: number = 4.9;

  // Mock vehicle data - Novi Sad locations - replace with actual API call from your backend
  vehicles: Vehicle[] = [
    { id: 1, lat: 45.2705, lng: 19.8250, status: 'available' },
    { id: 2, lat: 45.2640, lng: 19.8402, status: 'occupied' },
    { id: 3, lat: 45.2608, lng: 19.8301, status: 'available' },
    { id: 4, lat: 45.2732, lng: 19.8454, status: 'available' },
    { id: 5, lat: 45.2689, lng: 19.8206, status: 'occupied' },
    { id: 6, lat: 45.2650, lng: 19.8380, status: 'available' },
    { id: 7, lat: 45.2720, lng: 19.8290, status: 'occupied' },
    { id: 8, lat: 45.2595, lng: 19.8350, status: 'available' },
  ];

  rideOptions: RideOption[] = [
    {
      type: 'Economy',
      icon: '🚗',
      eta: '3 min away',
      price: '125.0 RSD',
      seats: '4 seats'
    },
    {
      type: 'Comfort',
      icon: '🚙',
      eta: '5 min away',
      price: '180.0 RSD',
      seats: '4 seats'
    },
    {
      type: 'Premium',
      icon: '🚕',
      eta: '4 min away',
      price: '280.0 RSD',
      seats: '4 seats'
    },
    {
      type: 'XL',
      icon: '🚐',
      eta: '6 min away',
      price: '220.0 RSD',
      seats: '6 seats'
    }
  ];

  estimateForm = new FormGroup({
    pickup: new FormControl('', [Validators.required]),
    destination: new FormControl('', [Validators.required])
  });

  constructor(private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.updateAvailableDrivers();
    this.loadVehicles();
  }

  ngAfterViewInit(): void {
    // Register default marker icon
    let DefaultIcon = L.icon({
      iconUrl: 'https://unpkg.com/leaflet@1.6.0/dist/images/marker-icon.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.6.0/dist/images/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      shadowSize: [41, 41]
    });

    L.Marker.prototype.options.icon = DefaultIcon;

    // Initialize map after view is ready
    setTimeout(() => {
      this.initMap();
    }, 100);
  }

  ngOnDestroy(): void {
    // Clean up map instance
    if (this.map) {
      this.map.remove();
    }
  }

  private initMap(): void {
    try {
      // Initialize Leaflet map centered on Novi Sad
      this.map = L.map('map', {
        center: [45.2671, 19.8335],
        zoom: 13,
      });

      // Add OpenStreetMap tiles
      const tiles = L.tileLayer(
        'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
        {
          maxZoom: 18,
          minZoom: 3,
          attribution:
            '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
        }
      );
      tiles.addTo(this.map);

      // Add vehicle markers to the map
      this.addVehicleMarkers();
    } catch (error) {
      console.error('Error initializing map:', error);
      this.errorMessage = 'Failed to load map. Please refresh the page.';
    }
  }

  private addVehicleMarkers(): void {
    // Clear existing markers
    this.vehicleMarkers.forEach(marker => this.map.removeLayer(marker));
    this.vehicleMarkers = [];

    this.vehicles.forEach(vehicle => {
      const iconColor = vehicle.status === 'available' ? '#10b981' : '#ef4444';
      
      const marker = L.circleMarker([vehicle.lat, vehicle.lng], {
        radius: 8,
        fillColor: iconColor,
        color: '#ffffff',
        weight: 2,
        opacity: 1,
        fillOpacity: 0.8
      }).addTo(this.map);

      marker.bindPopup(`
        <div style="font-size: 12px; padding: 4px;">
          <strong>Vehicle #${vehicle.id}</strong><br>
          Status: <span style="text-transform: capitalize; color: ${iconColor};">${vehicle.status}</span>
        </div>
      `);

      this.vehicleMarkers.push(marker);
    });
  }

  private updateAvailableDrivers(): void {
    this.availableDrivers = this.vehicles.filter(v => v.status === 'available').length;
  }

  private loadVehicles(): void {
    // TODO: Replace with actual API call to fetch vehicle data
    // Example:
    // this.vehicleService.getActiveVehicles().subscribe({
    //   next: (vehicles) => {
    //     this.vehicles = vehicles;
    //     this.updateAvailableDrivers();
    //     if (this.map) {
    //       this.addVehicleMarkers();
    //     }
    //   },
    //   error: (error) => {
    //     console.error('Error loading vehicles:', error);
    //   }
    // });
  }

  onEstimate(): void {
    this.errorMessage = '';

    if (this.estimateForm.invalid) {
      this.errorMessage = this.getErrorMessage();
      return;
    }

    const pickup = this.estimateForm.value.pickup?.trim() || '';
    const destination = this.estimateForm.value.destination?.trim() || '';

    if (!pickup || !destination) {
      this.errorMessage = 'Please enter both pickup location and destination!';
      return;
    }

    // TODO: Replace with actual geocoding service
    // For now, we'll use Nominatim API directly to geocode addresses
    // In production, you should create a MapService with proper methods
    
    // Geocode pickup address
    this.geocodeAddress(pickup).then(pickupCoords => {
      if (!pickupCoords) {
        this.errorMessage = 'Could not find pickup location. Please try a more specific address.';
        return;
      }

      // Geocode destination address
      this.geocodeAddress(destination).then(destCoords => {
        if (!destCoords) {
          this.errorMessage = 'Could not find destination. Please try a more specific address.';
          return;
        }

        // Calculate route
        this.calculateRoute(pickupCoords, destCoords, pickup, destination);
      });
    }).catch(error => {
      console.error('Geocoding error:', error);
      this.errorMessage = 'Error finding addresses. Please try again.';
    });
  }

  private async geocodeAddress(address: string): Promise<L.LatLng | null> {
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}, Novi Sad`
      );
      const results = await response.json();
      
      if (results && results.length > 0) {
        return L.latLng(parseFloat(results[0].lat), parseFloat(results[0].lon));
      }
      return null;
    } catch (error) {
      console.error('Geocoding error:', error);
      return null;
    }
  }

  private calculateRoute(
    pickupCoords: L.LatLng, 
    destCoords: L.LatLng, 
    pickup: string, 
    destination: string
  ): void {
    // Remove existing route if any
    if (this.routeControl) {
      this.map.removeControl(this.routeControl);
    }
    if (this.pickupMarker) {
      this.map.removeLayer(this.pickupMarker);
    }
    if (this.destinationMarker) {
      this.map.removeLayer(this.destinationMarker);
    }

    // Create custom markers for pickup and destination
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

    // Create route using Leaflet Routing Machine
    // TODO: Replace 'YOUR_MAPBOX_API_KEY' with your actual MapBox API key
    this.routeControl = L.Routing.control({
      waypoints: [pickupCoords, destCoords],
      router: L.Routing.osrmv1({
        serviceUrl: 'https://router.project-osrm.org/route/v1'
      }),
      lineOptions: {
        styles: [{ color: '#00acc1', opacity: 0.7, weight: 4 }],
        extendToWaypoints: false,
        missingRouteTolerance: 0
      },
      showAlternatives: false,
      addWaypoints: false,
      routeWhileDragging: false,
      fitSelectedRoutes: true,
      show: false, // Hide the instructions panel
    }).addTo(this.map);

    // Handle route found event
    this.routeControl.on('routesfound', (e: any) => {
      const routes = e.routes;
      const summary = routes[0].summary;
      
      // Calculate distance in km and duration in minutes
      const distanceKm = (summary.totalDistance / 1000).toFixed(1);
      const durationMin = Math.round(summary.totalTime / 60);

      // Update estimate results
      this.estimateResults = {
        pickup,
        destination,
        distance: parseFloat(distanceKm),
        duration: durationMin
      };

      // Update ride options with calculated prices based on distance
      this.updateRideOptions(parseFloat(distanceKm), durationMin);

      // Manually trigger change detection to update the UI
      this.cdr.detectChanges();
    });

    // Handle routing errors
    this.routeControl.on('routingerror', (e: any) => {
      console.error('Routing error:', e);
      this.errorMessage = 'Could not calculate route. Please try different addresses.';
    });
  }

  private updateRideOptions(distanceKm: number, durationMin: number): void {
    const pricePerKm = 120; // 120 RSD per km
    const etaMin = Math.max(3, Math.round(durationMin * 0.2)); // Estimated time to pickup
    
    // Calculate base price from distance
    const basePrice = distanceKm * pricePerKm;
    
    this.rideOptions = [
      {
        type: 'Economy',
        icon: '🚗',
        eta: `${etaMin} min away`,
        price: `${basePrice.toFixed(1)} RSD`,
        seats: '4 seats'
      },
      {
        type: 'Comfort',
        icon: '🚙',
        eta: `${etaMin + 2} min away`,
        price: `${(basePrice * 1.4).toFixed(1)} RSD`,
        seats: '4 seats'
      },
      {
        type: 'Premium',
        icon: '🚕',
        eta: `${etaMin + 1} min away`,
        price: `${(basePrice * 2.2).toFixed(1)} RSD`,
        seats: '4 seats'
      },
      {
        type: 'XL',
        icon: '🚐',
        eta: `${etaMin + 3} min away`,
        price: `${(basePrice * 1.7).toFixed(1)} RSD`,
        seats: '6 seats'
      }
    ];
  }

  resetEstimate(): void {
    this.estimateResults = null;
    this.estimateForm.reset();
    this.errorMessage = '';
    this.showEstimateForm = false;
    
    // Remove route and markers from map
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

    // Reset map view to original position (Novi Sad center)
    this.map.setView([45.2671, 19.8335], 13);
  }

  goBack(): void {
    if (this.estimateResults) {
      // If on results page, go back to form
      this.estimateResults = null;
      this.errorMessage = '';
      
      // Remove route and markers from map
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

      // Reset map view
      this.map.setView([45.2671, 19.8335], 13);
    } else if (this.showEstimateForm) {
      // If on form page, go back to initial view
      this.showEstimateForm = false;
      this.estimateForm.reset();
      this.errorMessage = '';
    }
  }

  private getErrorMessage(): string {
    const pickupControl = this.estimateForm.get('pickup');
    const destinationControl = this.estimateForm.get('destination');

    if (pickupControl?.errors?.['required']) {
      return 'Pickup location is required!';
    }

    if (destinationControl?.errors?.['required']) {
      return 'Destination is required!';
    }

    return 'Please fill in all fields correctly!';
  }
}