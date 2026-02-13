import {Component, inject, input, OnInit, output, signal} from '@angular/core';
import {NgIcon, provideIcons} from '@ng-icons/core';
import { heroXMark, heroStar } from '@ng-icons/heroicons/outline';
import { heroStarSolid, heroHeartSolid } from '@ng-icons/heroicons/solid';
import {getStatusClass, RideHistory} from '../../../../../model/ride';
import {RideService} from '../../../../../services/ride.service';
import {MapService} from '../../../../../services/map.service';
import {FavoriteRoutesService} from '../../../../../services/favorite-routes.service';
import {finalize} from 'rxjs/operators';
import * as L from 'leaflet';
import {RideDetailResponse} from '../../../../../model/ride-history';
import {Router} from '@angular/router';
import {DatePipe, DecimalPipe} from '@angular/common';

@Component({
  selector: 'app-ride-details-modal',
  imports: [NgIcon, DatePipe, DecimalPipe],
  providers: [
    provideIcons({ heroXMark, heroStar, heroStarSolid, heroHeartSolid })
  ],
  templateUrl: './ride-details-modal.html'
})
export class RideDetailsModal implements OnInit {
  ride = input.required<RideHistory>();
  onClose = output<void>();

  private rideService = inject(RideService);
  private mapService = inject(MapService);
  private favoriteService = inject(FavoriteRoutesService);
  private router = inject(Router);

  rideDetail = signal<RideDetailResponse>({} as RideDetailResponse);
  isLoading = signal(true);
  isFavorite = signal(false);

  ngOnInit() {
    this.fetchDetails();
  }

  fetchDetails() {
    this.isLoading.set(true);
    this.rideService.getPassengerRideDetail(this.ride().id)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (detail) => {
          this.rideDetail.set(detail);
          this.checkIfFavorite();
          setTimeout(() => this.initMapWithRoute(detail), 100);
        }
      });
  }

  private initMapWithRoute(detail: RideDetailResponse) {
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
      })
      .catch(err => console.error('Error fetching route:', err));
  }

  checkIfFavorite() {
    this.favoriteService.getFavoriteRoutes().subscribe(favs => {
      this.isFavorite.set(favs.some(f => f.routeId === this.rideDetail().routeId));
    });
  }

  toggleFavorite() {
    const detail = this.rideDetail();
    if (this.isFavorite()) {
      this.favoriteService.removeFavoriteRoute(detail.routeId).subscribe(() => this.isFavorite.set(false));
    } else {
      this.favoriteService.addFavoriteRoute(detail.routeId, "My Favorite").subscribe(() => this.isFavorite.set(true));
    }
  }

  closeModal() {
    this.mapService.destroyMap();
    this.onClose.emit();
  }

  navigateToRating(rideId: number): void {
    this.router.navigate(['/passenger/ride-rating', rideId]);
  }

  navigateToTracking(rideId: number): void {
    this.closeModal();
    this.router.navigate(['/passenger/ride-tracking', rideId]);
  }

  protected readonly getStatusClass = getStatusClass;

  canRideBeRatedFromDetail(): boolean {
    const detail = this.rideDetail();
    if (!detail || !detail.completedAt) return false;

    const isCompleted = detail.status === 'FINISHED';
    const hasNoRating = detail.driverRating === null && detail.vehicleRating === null;
    const within72Hours = !this.isRatingExpired(detail.completedAt);

    return isCompleted && hasNoRating && within72Hours;
  }

  isRatingExpired(completedAt: string): boolean {
    const now = new Date();
    const completedDate = new Date(completedAt);
    const diffInHours = (now.getTime() - completedDate.getTime()) / (1000 * 60 * 60);
    return diffInHours > 72;
  }

  getHoursRemaining(): number {
    const detail = this.rideDetail();
    if (!detail || !detail.completedAt) return 0;

    const now = new Date();
    const completedAt = new Date(detail.completedAt);
    const hoursSinceCompleted = (now.getTime() - completedAt.getTime()) / (1000 * 60 * 60);

    return Math.max(0, 72 - Math.floor(hoursSinceCompleted));
  }

  isRatingDeadlineNear(): boolean {
    const hoursRemaining = this.getHoursRemaining();
    return hoursRemaining > 0 && hoursRemaining <= 24;
  }
}
