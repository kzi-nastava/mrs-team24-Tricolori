import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import * as L from 'leaflet';

interface RideDetails {
  id: string;
  pickup: string;
  destination: string;
  pickupCoords: [number, number];
  destinationCoords: [number, number];
  date: Date;
  driverName: string;
  vehicleType: string;
  distance: number;
  duration: number;
  price: number;
  completedAt: Date;
}

@Component({
  selector: 'app-ride-rating',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './ride-rating.component.html'
})
export class RideRatingComponent implements OnInit, OnDestroy {
  ratingForm: FormGroup;
  driverRating = signal<number>(0);
  vehicleRating = signal<number>(0);
  isSubmitting = signal<boolean>(false);
  isSubmitted = signal<boolean>(false);
  isExpired = signal<boolean>(false);
  hoursRemaining = signal<number>(72);

  // Mock ride details - in real app, fetch from service based on route param
  rideDetails = signal<RideDetails>({
    id: 'ride-123',
    pickup: 'Kraljev park',
    destination: 'Kosovska 21',
    pickupCoords: [44.7866, 20.4489],
    destinationCoords: [44.8125, 20.4612],
    date: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000), // 2 days ago
    driverName: 'Marko Petrović',
    vehicleType: 'Economy - Toyota Corolla',
    distance: 1.9,
    duration: 5,
    price: 232.7,
    completedAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000)
  });

  private map: L.Map | null = null;
  private routeLine: L.Polyline | null = null;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.ratingForm = this.fb.group({
      driverRating: [0, [Validators.required, Validators.min(1), Validators.max(5)]],
      vehicleRating: [0, [Validators.required, Validators.min(1), Validators.max(5)]],
      comment: ['', [Validators.maxLength(500)]]
    });
  }

  ngOnInit(): void {
    // Check if rating period has expired (3 days = 72 hours)
    this.checkRatingExpiry();
    
    // Initialize map after view is ready
    setTimeout(() => this.initMap(), 100);

    // Load existing rating if any
    this.loadExistingRating();
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
      this.map = null;
    }
  }

  private checkRatingExpiry(): void {
    const ride = this.rideDetails();
    const now = new Date();
    const completedAt = new Date(ride.completedAt);
    const hoursSinceCompleted = (now.getTime() - completedAt.getTime()) / (1000 * 60 * 60);
    
    this.hoursRemaining.set(Math.max(0, 72 - Math.floor(hoursSinceCompleted)));
    
    if (hoursSinceCompleted > 72) {
      this.isExpired.set(true);
    }
  }

  private loadExistingRating(): void {
    // In real app, check if user already rated this ride
    // For now, simulate this check
    const hasRated = false; // Replace with actual API call
    if (hasRated) {
      this.isSubmitted.set(true);
    }
  }

  private initMap(): void {
    const mapElement = document.getElementById('ratingMap');
    if (!mapElement) return;

    const ride = this.rideDetails();
    
    // Calculate center point
    const centerLat = (ride.pickupCoords[0] + ride.destinationCoords[0]) / 2;
    const centerLng = (ride.pickupCoords[1] + ride.destinationCoords[1]) / 2;

    this.map = L.map('ratingMap').setView([centerLat, centerLng], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    // Add pickup marker
    const pickupIcon = L.divIcon({
      className: 'custom-marker',
      html: '<div class="w-8 h-8 bg-blue-500 rounded-full border-4 border-white shadow-lg flex items-center justify-center"><div class="w-3 h-3 bg-white rounded-full"></div></div>',
      iconSize: [32, 32],
      iconAnchor: [16, 16]
    });

    L.marker(ride.pickupCoords, { icon: pickupIcon })
      .addTo(this.map)
      .bindPopup(`<b>Pickup:</b> ${ride.pickup}`);

    // Add destination marker
    const destIcon = L.divIcon({
      className: 'custom-marker',
      html: '<div class="w-8 h-8 bg-red-500 rounded-full border-4 border-white shadow-lg flex items-center justify-center"><div class="w-3 h-3 bg-white rounded-full"></div></div>',
      iconSize: [32, 32],
      iconAnchor: [16, 16]
    });

    L.marker(ride.destinationCoords, { icon: destIcon })
      .addTo(this.map)
      .bindPopup(`<b>Destination:</b> ${ride.destination}`);

    // Draw route line
    this.routeLine = L.polyline([ride.pickupCoords, ride.destinationCoords], {
      color: '#14b8a6',
      weight: 4,
      opacity: 0.7,
      dashArray: '10, 10'
    }).addTo(this.map);

    // Fit map to show both markers
    const bounds = L.latLngBounds([ride.pickupCoords, ride.destinationCoords]);
    this.map.fitBounds(bounds, { padding: [50, 50] });
  }

  setDriverRating(rating: number): void {
    if (this.isExpired() || this.isSubmitted()) return;
    
    this.driverRating.set(rating);
    this.ratingForm.patchValue({ driverRating: rating });
  }

  setVehicleRating(rating: number): void {
    if (this.isExpired() || this.isSubmitted()) return;
    
    this.vehicleRating.set(rating);
    this.ratingForm.patchValue({ vehicleRating: rating });
  }

  getRatingText(rating: number): string {
    const texts: { [key: number]: string } = {
      1: 'Poor',
      2: 'Fair',
      3: 'Good',
      4: 'Very Good',
      5: 'Excellent'
    };
    return texts[rating] || '';
  }

  submitRating(): void {
    if (this.ratingForm.invalid || this.isSubmitting() || this.isExpired()) {
      return;
    }

    this.isSubmitting.set(true);

    const ratingData = {
      rideId: this.rideDetails().id,
      driverRating: this.ratingForm.value.driverRating,
      vehicleRating: this.ratingForm.value.vehicleRating,
      comment: this.ratingForm.value.comment || ''
    };

    // Simulate API call
    setTimeout(() => {
      console.log('Submitting rating:', ratingData);
      
      // In real app, call rating service:
      // this.ratingService.submitRating(ratingData).subscribe(...)
      
      this.isSubmitting.set(false);
      this.isSubmitted.set(true);

      // Redirect after 2 seconds
      setTimeout(() => {
        this.router.navigate(['/ride-history']);
      }, 2000);
    }, 1500);
  }

  skipRating(): void {
    this.router.navigate(['/ride-history']);
  }

  handleBack(): void {
    this.router.navigate(['/ride-history']);
  }
}