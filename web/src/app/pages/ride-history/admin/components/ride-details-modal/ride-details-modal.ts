import { Component, inject, input, OnInit, output, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { heroXMark, heroMapPin, heroClock, heroUser, heroExclamationTriangle, heroStar, heroCurrencyDollar, heroInformationCircle } from '@ng-icons/heroicons/outline';
import { heroStarSolid } from '@ng-icons/heroicons/solid';
import { finalize } from 'rxjs/operators';
import * as L from 'leaflet';

import { RideService } from '../../../../../services/ride.service';
import { MapService } from '../../../../../services/map.service';
import { getStatusClass } from '../../../../../model/ride';
import { RideDetailResponse } from '../../../../../model/ride-history';

@Component({
  selector: 'app-ride-details-modal',
  standalone: true,
  imports: [DatePipe, DecimalPipe, NgIcon],
  providers: [
    provideIcons({
      heroXMark, heroMapPin, heroClock, heroUser,
      heroExclamationTriangle, heroStar, heroStarSolid,
      heroCurrencyDollar, heroInformationCircle
    })
  ],
  templateUrl: './ride-details-modal.html'
})
export class RideDetailsModal implements OnInit {
  rideId = input.required<number>();
  onClose = output<void>();

  private rideService = inject(RideService);
  private mapService = inject(MapService);

  rideDetail = signal<RideDetailResponse | null>(null);
  isLoading = signal(true);

  ngOnInit() {
    this.fetchDetails();
  }

  fetchDetails() {
    this.isLoading.set(true);
    this.rideService.getAdminRideDetail(this.rideId())
      .subscribe({
        next: (detail) => {
          this.rideDetail.set(detail);
          console.log('Detail response:', detail);

          this.isLoading.set(false);

          setTimeout(() => this.initMap(detail), 50);
        },
        error: (err) => {
          console.error('Error fetching ride details', err);
          this.isLoading.set(false);
        }
      });
  }

  private initMap(detail: RideDetailResponse) {
    const pickup: [number, number] = [detail.pickupLatitude, detail.pickupLongitude];
    this.mapService.initMap('ride-map', pickup, 14);

    const url = `https://router.project-osrm.org/route/v1/driving/${detail.pickupLongitude},${detail.pickupLatitude};${detail.dropoffLongitude},${detail.dropoffLatitude}?overview=full&geometries=geojson`;

    fetch(url)
      .then(res => res.json())
      .then(data => {
        if (data.routes?.[0]) {
          const coords = data.routes[0].geometry.coordinates.map((c: any) => L.latLng(c[1], c[0]));
          this.mapService.drawRoute(coords);
        }
      });
  }

  closeModal() {
    this.mapService.destroyMap();
    this.onClose.emit();
  }

  protected readonly getStatusClass = getStatusClass;
}
