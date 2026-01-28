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

  /*
  async rideSubmit(event: Event) {
    event.preventDefault();

    const routeSelector = this.routeSelector();
    const preferences = this.preferencesSelector().preferencesForm.getRawValue();
    const trackers = this.trackersSelector().trackersForm.getRawValue().trackers;

    if (!routeSelector.routeForm.valid) {
      routeSelector.routeForm.markAllAsTouched();
      return;
    }

    const routeVal = routeSelector.routeForm.getRawValue();

    try {
      const pickupGeocoded = await this.geocodingService.geocodeAddress(routeVal.pickup);
      const destinationGeocoded = await this.geocodingService.geocodeAddress(routeVal.destination);
      
      if (!pickupGeocoded || !destinationGeocoded) {
        alert("Adrese nisu pronađene.");
        return;
      }

      const stopPromises = (routeVal.stops || []).map((s: string) => this.geocodingService.geocodeAddress(s));
      const stopsGeocodedResults = await Promise.all(stopPromises);

      const rideRequest = {
        // Ruta sa koordinatama za backend
        route: {
          pickupStop: {
            address: pickupGeocoded.displayName,
            location: { longitude: pickupGeocoded.lng, latitude: pickupGeocoded.lat }
          },
          destinationStop: {
            address: destinationGeocoded.displayName,
            location: { longitude: destinationGeocoded.lng, latitude: destinationGeocoded.lat }
          },
          stops: stopsGeocodedResults
            .filter(res => res !== null)
            .map(res => ({
              address: res!.displayName,
              location: { longitude: res!.lng, latitude: res!.lat }
            }))
        },

        // Podaci iz PreferencesSelector
        vehicleType: preferences.vehicleType,
        babyTransport: preferences.babySeat,
        petTransport: preferences.petFriendly,
        scheduledTime: preferences.scheduledTime, // Date objekat ili null

        passengers: trackers
      };

      console.log("Finalni zahtev za backend:", rideRequest);

      // 4. Slanje na backend
      // this.rideService.bookRide(rideRequest).subscribe({ ... });

    } catch (error) {
      console.error("Greška:", error);
    }
  }*/
}
