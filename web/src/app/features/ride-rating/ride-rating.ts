import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { 
  heroArrowLeft, 
  heroExclamationCircle, 
  heroCheckCircle,
  heroMapPin,
  heroCalendar,
  heroStar,
  heroClock,
  heroArrowsRightLeft,
  heroCurrencyDollar
} from '@ng-icons/heroicons/outline';
import { heroStarSolid as heroStarSolidFill } from '@ng-icons/heroicons/solid';
import * as L from 'leaflet';
import 'leaflet-routing-machine';

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
  imports: [CommonModule, ReactiveFormsModule, NgIconComponent],
  providers: [
    provideIcons({
      heroArrowLeft,
      heroExclamationCircle,
      heroCheckCircle,
      heroMapPin,
      heroCalendar,
      heroStar,
      heroStarSolid: heroStarSolidFill,
      heroClock,
      heroArrowsRightLeft,
      heroCurrencyDollar
    })
  ],
  templateUrl: './ride-rating.html'
})
export class RideRatingComponent implements OnInit, OnDestroy {
  ratingForm: FormGroup;
  driverRating = signal<number>(0);
  vehicleRating = signal<number>(0);
  isSubmitting = signal<boolean>(false);
  isSubmitted = signal<boolean>(false);
  isExpired = signal<boolean>(false);
  hoursRemaining = signal<number>(72);

  // Mock ride details - using Novi Sad locations
  rideDetails = signal<RideDetails>({
    id: 'ride-123',
    pickup: 'Trg Slobode 1',
    destination: 'Kisačka 71',
    pickupCoords: [45.2671, 19.8335], // Trg Slobode
    destinationCoords: [45.2550, 19.8450], // Kisačka 71
    date: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000), // 2 days ago
    driverName: 'Marko Petrović',
    vehicleType: 'Economy - Toyota Corolla',
    distance: 2.3,
    duration: 8,
    price: 276,
    completedAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000)
  });

  private map: L.Map | null = null;
  private routeControl: any = null;

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
    if (this.routeControl && this.map) {
      this.map.removeControl(this.routeControl);
      this.routeControl = null;
    }
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

    // Create custom icons for pickup and destination
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

    // Create routing control with actual route
    this.routeControl = L.Routing.control({
      waypoints: [
        L.latLng(ride.pickupCoords[0], ride.pickupCoords[1]),
        L.latLng(ride.destinationCoords[0], ride.destinationCoords[1])
      ],
      router: L.Routing.osrmv1({
        serviceUrl: 'https://router.project-osrm.org/route/v1'
      }),
      lineOptions: {
        styles: [{ color: '#00acc1', opacity: 0.7, weight: 4 }],
        extendToWaypoints: false,
        missingRouteTolerance: 0
      },
      show: false,
      addWaypoints: false,
      fitSelectedRoutes: true,
      createMarker: (i: number, waypoint: any, n: number) => {
        const icon = i === 0 ? pickupIcon : destinationIcon;
        return L.marker(waypoint.latLng, { icon });
      }
    } as any).addTo(this.map);

    // Update ride details when route is calculated
    this.routeControl.on('routesfound', (e: any) => {
      const summary = e.routes[0].summary;
      const distanceKm = summary.totalDistance / 1000;
      const durationMin = Math.round(summary.totalTime / 60);

      // Update the ride details with calculated values if needed
      this.rideDetails.update(details => ({
        ...details,
        distance: parseFloat(distanceKm.toFixed(1)),
        duration: durationMin
      }));
    });
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

      // Redirect after 2 seconds to ride history
      setTimeout(() => {
        this.router.navigate(['/passenger/history']);
      }, 2000);
    }, 1500);
  }

  skipRating(): void {
    this.router.navigate(['/passenger/history']);
  }

  handleBack(): void {
    this.router.navigate(['/passenger/history']);
  }
}