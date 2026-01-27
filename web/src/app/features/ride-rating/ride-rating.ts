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
import { RatingService, RideRatingRequest } from '../../core/services/rating.service'; 
import { catchError, of } from 'rxjs';

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
  errorMessage = signal<string>('');

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
  private rideId: string = '';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private ratingService: RatingService
  ) {
    this.ratingForm = this.fb.group({
      driverRating: [0, [Validators.required, Validators.min(1), Validators.max(5)]],
      vehicleRating: [0, [Validators.required, Validators.min(1), Validators.max(5)]],
      comment: ['', [Validators.maxLength(500)]]
    });
  }

  ngOnInit(): void {
    // Get ride ID from route params
    this.rideId = this.route.snapshot.paramMap.get('id') || '';
    
    if (!this.rideId) {
      console.error('No ride ID provided');
      this.router.navigate(['/passenger/history']);
      return;
    }

    // Load rating status from backend
    this.loadRatingStatus();
    
    // Initialize map after view is ready
    setTimeout(() => this.initMap(), 100);
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

  private loadRatingStatus(): void {
    this.ratingService.getRatingStatus(this.rideId)
      .pipe(
        catchError(error => {
          console.error('Error loading rating status:', error);
          this.errorMessage.set('Failed to load rating information');
          return of(null);
        })
      )
      .subscribe(status => {
        if (status) {
          this.isSubmitted.set(status.alreadyRated);
          this.isExpired.set(status.deadlinePassed);
          
          if (!status.canRate && !status.alreadyRated) {
            this.isExpired.set(true);
          }

          // Calculate hours remaining
          if (status.deadline) {
            const deadline = new Date(status.deadline);
            const now = new Date();
            const hoursLeft = Math.max(0, Math.floor((deadline.getTime() - now.getTime()) / (1000 * 60 * 60)));
            this.hoursRemaining.set(hoursLeft);
          }
        }
      });
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
    this.errorMessage.set('');

    const ratingData: RideRatingRequest = {
      driverRating: this.ratingForm.value.driverRating,
      vehicleRating: this.ratingForm.value.vehicleRating,
      comment: this.ratingForm.value.comment || undefined
    };

    this.ratingService.submitRating(this.rideId, ratingData)
      .pipe(
        catchError(error => {
          console.error('Error submitting rating:', error);
          
          // Handle different error scenarios
          if (error.status === 403) {
            this.errorMessage.set('You are not authorized to rate this ride');
          } else if (error.status === 404) {
            this.errorMessage.set('Ride not found');
          } else if (error.status === 400) {
            // Could be already rated or ride cannot be reviewed
            this.errorMessage.set(error.error?.message || 'This ride cannot be rated');
          } else {
            this.errorMessage.set('Failed to submit rating. Please try again.');
          }
          
          this.isSubmitting.set(false);
          return of(null);
        })
      )
      .subscribe(response => {
        if (response !== null) {
          // Success
          this.isSubmitting.set(false);
          this.isSubmitted.set(true);

          // Redirect after 2 seconds to ride history
          setTimeout(() => {
            this.router.navigate(['/passenger/history']);
          }, 2000);
        }
      });
  }

  skipRating(): void {
    this.router.navigate(['/passenger/history']);
  }

  handleBack(): void {
    this.router.navigate(['/passenger/history']);
  }
}