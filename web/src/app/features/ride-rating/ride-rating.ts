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
import { RideService, RideDetailResponse } from '../../core/services/ride.service';
import { catchError, of, forkJoin } from 'rxjs';

interface RideDetails {
  id: number;
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
  isLoading = signal<boolean>(true);
  hoursRemaining = signal<number>(72);
  errorMessage = signal<string>('');

  rideDetails = signal<RideDetails | null>(null);

  private map: L.Map | null = null;
  private routeControl: any = null;
  private rideId: number = 0;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private ratingService: RatingService,
    private rideService: RideService
  ) {
    this.ratingForm = this.fb.group({
      driverRating: [0, [Validators.required, Validators.min(1), Validators.max(5)]],
      vehicleRating: [0, [Validators.required, Validators.min(1), Validators.max(5)]],
      comment: ['', [Validators.maxLength(500)]]
    });
  }

  ngOnInit(): void {
    // Get ride ID from route params
    const rideIdParam = this.route.snapshot.paramMap.get('id');
    
    if (!rideIdParam) {
      console.error('No ride ID provided');
      this.router.navigate(['/passenger/history']);
      return;
    }

    this.rideId = parseInt(rideIdParam, 10);

    if (isNaN(this.rideId)) {
      console.error('Invalid ride ID');
      this.router.navigate(['/passenger/history']);
      return;
    }

    // Load both rating status and ride details
    this.loadRideData();
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

  private loadRideData(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    // Load both rating status and ride details in parallel
    forkJoin({
      ratingStatus: this.ratingService.getRatingStatus(this.rideId).pipe(
        catchError(error => {
          console.error('Error loading rating status:', error);
          return of(null);
        })
      ),
      rideDetails: this.rideService.getPassengerRideDetail(this.rideId).pipe(
        catchError(error => {
          console.error('Error loading ride details:', error);
          return of(null);
        })
      )
    }).subscribe(({ ratingStatus, rideDetails }) => {
      this.isLoading.set(false);

      // Handle rating status
      if (ratingStatus) {
        this.isSubmitted.set(ratingStatus.alreadyRated);
        this.isExpired.set(ratingStatus.deadlinePassed);
        
        if (!ratingStatus.canRate && !ratingStatus.alreadyRated) {
          this.isExpired.set(true);
        }

        // Calculate hours remaining
        if (ratingStatus.deadline) {
          const deadline = new Date(ratingStatus.deadline);
          const now = new Date();
          const hoursLeft = Math.max(0, Math.floor((deadline.getTime() - now.getTime()) / (1000 * 60 * 60)));
          this.hoursRemaining.set(hoursLeft);
        }
      }

      // Handle ride details
      if (rideDetails) {
        this.setRideDetails(rideDetails);
        
        // Initialize map after ride details are loaded
        setTimeout(() => this.initMap(), 100);
      } else {
        this.errorMessage.set('Failed to load ride information. Please try again.');
      }

      // If both failed, show error and redirect
      if (!ratingStatus && !rideDetails) {
        this.errorMessage.set('Failed to load ride information');
        setTimeout(() => {
          this.router.navigate(['/passenger/history']);
        }, 3000);
      }
    });
  }

  private setRideDetails(details: RideDetailResponse): void {
    const rideData: RideDetails = {
      id: details.id,
      pickup: details.pickupAddress,
      destination: details.dropoffAddress,
      pickupCoords: [details.pickupLatitude, details.pickupLongitude],
      destinationCoords: [details.dropoffLatitude, details.dropoffLongitude],
      date: new Date(details.startedAt || details.createdAt),
      driverName: details.driverName,
      vehicleType: `${details.vehicleModel}`,
      distance: details.distance,
      duration: details.duration,
      price: details.totalPrice,
      completedAt: new Date(details.completedAt)
    };

    this.rideDetails.set(rideData);
  }

  private initMap(): void {
    const mapElement = document.getElementById('ratingMap');
    if (!mapElement) return;

    const ride = this.rideDetails();
    if (!ride) return;
    
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

    // Optionally update ride details when route is calculated
    // (Only if you want to override backend distance/duration with map calculations)
    this.routeControl.on('routesfound', (e: any) => {
      const summary = e.routes[0].summary;
      const distanceKm = summary.totalDistance / 1000;
      const durationMin = Math.round(summary.totalTime / 60);

      // You can choose to update with calculated values or keep backend values
      // Uncomment below if you want to use map-calculated values:
      /*
      this.rideDetails.update(details => {
        if (!details) return details;
        return {
          ...details,
          distance: parseFloat(distanceKm.toFixed(1)),
          duration: durationMin
        };
      });
      */
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
          console.log('Working good');
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