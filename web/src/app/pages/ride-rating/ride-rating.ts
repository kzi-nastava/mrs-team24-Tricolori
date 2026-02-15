import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import {
  heroArrowLeft, heroExclamationCircle, heroCheckCircle, heroMapPin,
  heroCalendar, heroStar, heroClock, heroArrowsRightLeft, heroCurrencyDollar
} from '@ng-icons/heroicons/outline';
import { heroStarSolid as heroStarSolidFill } from '@ng-icons/heroicons/solid';

import { RatingService } from '../../services/rating.service';
import { RideService } from '../../services/ride.service';
import { MapService } from '../../services/map.service';
import { EstimationService } from '../../services/estimation.service';
import { catchError, of, forkJoin } from 'rxjs';
import {Map} from '../../components/map/map';

@Component({
  selector: 'app-ride-rating',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgIconComponent, Map],
  providers: [
    provideIcons({
      heroArrowLeft, heroExclamationCircle, heroCheckCircle, heroMapPin,
      heroCalendar, heroStar, heroStarSolid: heroStarSolidFill,
      heroClock, heroArrowsRightLeft, heroCurrencyDollar
    })
  ],
  templateUrl: './ride-rating.html'
})
export class RideRatingComponent implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private ratingService = inject(RatingService);
  private rideService = inject(RideService);
  private mapService = inject(MapService);
  private estimationService = inject(EstimationService);

  ratingForm: FormGroup;
  driverRating = signal<number>(0);
  vehicleRating = signal<number>(0);
  isSubmitting = signal<boolean>(false);
  isSubmitted = signal<boolean>(false);
  isExpired = signal<boolean>(false);
  isLoading = signal<boolean>(true);
  errorMessage = signal<string>('');
  rideDetails = signal<any>(null);

  private rideId: number = 0;

  constructor() {
    this.ratingForm = this.fb.group({
      driverRating: [0, [Validators.required, Validators.min(1), Validators.max(5)]],
      vehicleRating: [0, [Validators.required, Validators.min(1), Validators.max(5)]],
      comment: ['', [Validators.maxLength(500)]]
    });
  }

  ngOnInit(): void {
    const rideIdParam = this.route.snapshot.paramMap.get('id');
    if (!rideIdParam) {
      this.router.navigate(['/passenger/history']);
      return;
    }
    this.rideId = Number(rideIdParam);
    this.loadRideData();
  }

  private loadRideData(): void {
    this.isLoading.set(true);

    forkJoin({
      ratingStatus: this.ratingService.getRatingStatus(this.rideId).pipe(catchError(() => of(null))),
      rideDetails: this.rideService.getPassengerRideDetail(this.rideId).pipe(catchError(() => of(null)))
    }).subscribe(async ({ ratingStatus, rideDetails }) => {
      this.isLoading.set(false);

      if (ratingStatus) {
        this.isSubmitted.set(ratingStatus.alreadyRated);
        this.isExpired.set(ratingStatus.deadlinePassed);
      }

      if (rideDetails) {
        this.rideDetails.set(rideDetails);

        this.estimationService.calculateRouteFromAddress(
          rideDetails.pickupAddress,
          rideDetails.dropoffAddress
        ).subscribe({
          next: (estimation) => {
            if (!estimation || !estimation.routeGeometry) return;
            this.mapService.drawRoute(estimation.routeGeometry);
          },
          error: () => {
          }
        });
      }
    });
  }

  setDriverRating(r: number): void { this.driverRating.set(r); this.ratingForm.patchValue({ driverRating: r }); }
  setVehicleRating(r: number): void { this.vehicleRating.set(r); this.ratingForm.patchValue({ vehicleRating: r }); }
  getRatingText(r: number): string { return ['','Poor','Fair','Good','Very Good','Excellent'][r] || ''; }

  submitRating(): void {
    if (this.ratingForm.invalid || this.isSubmitting()) return;
    this.isSubmitting.set(true);

    this.ratingService.submitRating(this.rideId, this.ratingForm.value).subscribe({
      next: () => {
        this.isSubmitted.set(true);
        setTimeout(() => {
          this.isSubmitting.set(false);
          this.router.navigate(['/passenger/history']);
        }, 2000);
      },
      error: () => {
        this.errorMessage.set('Failed to submit rating.');
        this.isSubmitting.set(false);
      }
    });
  }

  handleBack(): void { this.router.navigate(['/passenger/history']); }
  skipRating(): void { this.router.navigate(['/passenger/history']); }
}