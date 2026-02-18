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
import * as L from 'leaflet';

import { RatingService } from '../../services/rating.service';
import { RideService } from '../../services/ride.service';
import { catchError, of, forkJoin } from 'rxjs';

@Component({
  selector: 'app-ride-rating',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgIconComponent],
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

  ratingForm: FormGroup;
  driverRating = signal<number>(0);
  vehicleRating = signal<number>(0);
  isSubmitting = signal<boolean>(false);
  isSubmitted = signal<boolean>(false);
  isExpired = signal<boolean>(false);
  isLoading = signal<boolean>(true);
  errorMessage = signal<string>('');
  rideDetails = signal<any>(null);

  private map: L.Map | null = null;
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
    }).subscribe(({ ratingStatus, rideDetails }) => {
      this.isLoading.set(false);

      if (ratingStatus) {
        this.isSubmitted.set(ratingStatus.alreadyRated);
        this.isExpired.set(ratingStatus.deadlinePassed);
      }

      if (rideDetails) {
        this.rideDetails.set(rideDetails);
        setTimeout(() => this.initMap(rideDetails), 100);
      }
    });
  }

  private initMap(detail: any): void {
    if (!detail.pickupLatitude || !detail.pickupLongitude) return;

    if (this.map) {
      this.map.remove();
    }

    const pickupLatLng: L.LatLngExpression = [detail.pickupLatitude, detail.pickupLongitude];
    const dropoffLatLng: L.LatLngExpression = [
      detail.dropoffLatitude || detail.pickupLatitude,
      detail.dropoffLongitude || detail.pickupLongitude
    ];

    this.map = L.map('ride-map').setView(pickupLatLng, 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap'
    }).addTo(this.map);

    const pickupIcon = L.icon({
      iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
      shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      shadowSize: [41, 41]
    });

    const dropoffIcon = L.icon({
      iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
      shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      shadowSize: [41, 41]
    });

    L.marker(pickupLatLng, { icon: pickupIcon })
      .addTo(this.map)
      .bindPopup('<b>Pickup</b><br>' + (detail.pickupAddress || ''));

    L.marker(dropoffLatLng, { icon: dropoffIcon })
      .addTo(this.map)
      .bindPopup('<b>Dropoff</b><br>' + (detail.dropoffAddress || ''));

    const bounds = L.latLngBounds([pickupLatLng, dropoffLatLng]);
    this.map.fitBounds(bounds, { padding: [50, 50] });

    const url = `https://router.project-osrm.org/route/v1/driving/${detail.pickupLongitude},${detail.pickupLatitude};${detail.dropoffLongitude},${detail.dropoffLatitude}?overview=full&geometries=geojson`;

    fetch(url)
      .then(res => res.json())
      .then(data => {
        if (data.routes?.[0]) {
          const coords = data.routes[0].geometry.coordinates.map((c: any) => L.latLng(c[1], c[0]));
          L.polyline(coords, {
            color: '#00acc1',
            weight: 4,
            opacity: 0.8
          }).addTo(this.map!);
        }
      })
      .catch(err => console.error('Error fetching route:', err));
  }

  setDriverRating(r: number): void { this.driverRating.set(r); this.ratingForm.patchValue({ driverRating: r }); }
  setVehicleRating(r: number): void { this.vehicleRating.set(r); this.ratingForm.patchValue({ vehicleRating: r }); }
  getRatingText(r: number): string { return ['', 'Poor', 'Fair', 'Good', 'Very Good', 'Excellent'][r] || ''; }

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