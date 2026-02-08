import { AfterViewInit, Component, inject, OnDestroy, OnInit, signal, viewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouteSelector } from './route-selector/route-selector';
import { RideTrackersSelector } from './ride-trackers-selector/ride-trackers-selector';
import { PreferencesSelector } from './preferences-selector/preferences-selector';
import { FavoriteRouteSelector } from './favorite-route-selector/favorite-route-selector';
import { FavoriteRoute, Route } from '../../../../../model/route';
import { NgIcon } from "@ng-icons/core";
import { MatDialog } from '@angular/material/dialog';
import { Overlay } from '@angular/cdk/overlay';
import { SchedulePicker } from './schedule-picker/schedule-picker';
import { RideRequest } from '../../../../../model/ride';
import { MapService } from '../../../../../services/map.service';
import { EstimationService } from '../../../../../services/estimation.service';
import { GeocodingService } from '../../../../../services/geocoding.service';

import * as L from 'leaflet';
import { RideService } from '../../../../../services/ride.service';

@Component({
  selector: 'app-home-passenger',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouteSelector,
    RideTrackersSelector,
    PreferencesSelector,
    NgIcon
  ],
  templateUrl: './ride-booking.html',
  styleUrl: './ride-booking.css'
})
export class RideBooking implements OnInit, AfterViewInit {
  // Services:
  private mapService = inject(MapService);
  private rideService = inject(RideService);

  routeSelector = viewChild.required(RouteSelector);
  preferencesSelector = viewChild.required(PreferencesSelector);
  trackersSelector = viewChild.required(RideTrackersSelector);

  private routesDialog = inject(MatDialog);
  private routeOverlay = inject(Overlay);

  private scheduleDialog = inject(MatDialog);
  private scheduleOverlay = inject(Overlay);

  currentRoute = signal<Route | undefined>(undefined);
  scheduledTime = signal<Date | undefined>(undefined);


  ngOnInit(): void {
    // Set default Leaflet icon
    L.Marker.prototype.options.icon = L.icon({
      iconUrl: 'https://unpkg.com/leaflet@1.6.0/dist/images/marker-icon.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.6.0/dist/images/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      shadowSize: [41, 41]
    });
  }

  ngAfterViewInit(): void {
    this.mapService.initMap('map');
  }

  openFavoriteRoutes() {
    const dialogRef = this.routesDialog.open(FavoriteRouteSelector, {
      width: '100%',
      maxWidth: '32rem',
      data: {userId: 2},
      panelClass: 'custom-modal',
      backdropClass: 'custom-backdrop',
      autoFocus: false,
      scrollStrategy: this.routeOverlay.scrollStrategies.block()
    });

    dialogRef.afterClosed().subscribe((result: FavoriteRoute | undefined) => {
      if (result) {
        this.currentRoute.set(result.route);
      }
    });
  }

  openTimeScheduler() {
    const dialogRef = this.scheduleDialog.open(SchedulePicker, {
      width: '100%',
      maxWidth: '24rem',
      panelClass: 'custom-modal',
      backdropClass: 'custom-backdrop',
      autoFocus: false,
      scrollStrategy: this.scheduleOverlay.scrollStrategies.block()
    });

    dialogRef.afterClosed().subscribe((result: Date | undefined) => {
      if (result) {
        this.scheduledTime.set(result);
      }
    });
  }

  

  async rideSubmit(event: Event) {
    event.preventDefault();

    // 1. ViewChild reference
    const routeComp = this.routeSelector();
    const prefComp = this.preferencesSelector();
    const trackComp = this.trackersSelector();

    // 2. Osnovna validacija (adrese moraju biti unete)
    if (!routeComp.routeForm.valid) {
      routeComp.routeForm.markAllAsTouched();
      return;
    }

    const route = routeComp.getRoute();
    const preferences = prefComp.getPreferences();
    const trackers = trackComp.trackersForm.getRawValue().trackers;

    // 4. Pakovanje za OrderRequest (Java Record struktura)
    const orderRequest = {
      route: route,
      preferences: preferences,
      createdAt: this.rideService.formatLocalDateTime(new Date()),
      trackers: trackers
    };

    console.log("A:",orderRequest);

    // 5. Slanje
    this.rideService.bookRide(orderRequest).subscribe({
      next: (res) => {
        console.log(res);
      },
      error: (err) => {
        console.error('Greška:', err);
        alert('Došlo je do greške pri naručivanju.');
      }
    });
  }
}
