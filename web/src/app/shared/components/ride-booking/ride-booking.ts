import { AfterViewInit, Component, inject, OnDestroy, OnInit, signal, viewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouteSelector } from '../../../shared/components/ride-booking/route-selector/route-selector';
import { RideTrackersSelector } from '../../../shared/components/ride-booking/ride-trackers-selector/ride-trackers-selector';
import { PreferencesSelector } from '../../../shared/components/ride-booking/preferences-selector/preferences-selector';
import { FavoriteRouteSelector } from '../../../shared/components/ride-booking/favorite-route-selector/favorite-route-selector';
import { FavoriteRoute, Route } from '../../../shared/model/route';
import { NgIcon } from "@ng-icons/core";
import { MatDialog } from '@angular/material/dialog';
import { Overlay } from '@angular/cdk/overlay';
import { SchedulePicker } from '../../../shared/components/ride-booking/schedule-picker/schedule-picker';
import { RideOptions, RideRequest } from '../../../shared/model/ride';
import { MapService } from '../../../core/services/map.service';
import { EstimationService } from '../../../core/services/estimation.service';
import { GeocodingService } from '../../../core/services/geocoding.service';

import * as L from 'leaflet';
import { RideService } from '../../../core/services/ride.service';

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
  private estimationService = inject(EstimationService);
  private rideService = inject(RideService)
  private geocodingService = inject(GeocodingService);

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
        this.currentRoute.set({
          pickup: result.pickup,
          destination: result.destination,
          stops: result.stops
        });
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
    
    // Pristup child komponenti kroz signal
    const routeSelector = this.routeSelector();
    const preferencesSelector = this.preferencesSelector();
    const trackersSelector = this.trackersSelector();
    
    // Since route selection is only mandatory data, I don't check
    // other child components' validity...
    if (!routeSelector.routeForm.valid) {
      routeSelector.routeForm.markAllAsTouched();
    }

    const routeVal: Route = routeSelector.routeForm.getRawValue();

    const estimation = await this.estimationService.calculateRoute(
      { lat: routeVal.pickup.location.lat, lng: routeVal.pickup.location.lng },
      { lat: routeVal.destination.location.lat, lng: routeVal.destination.location.lng },
      routeVal.pickup.address,
      routeVal.destination.address
    );

    const rideRequest: RideRequest = {
      route: {
        pickup: routeVal.pickup,
        destination: routeVal.destination,
        stops: routeVal.stops
      },
      preferences: preferencesSelector.preferencesForm.value,
      trackers: trackersSelector.trackersForm.getRawValue().trackers as string[] || [],
      estimation: {
        distanceKilometers: estimation?.distance!,
        durationMinutes: estimation?.duration!
      }
    }

    /*this.rideService.bookRide(rideRequest).subscribe({
      next: (response) => {
        console.log(response);
      },
      error: (err) => {
        console.log(err);
      }
    })*/

    // Draw route on map:
    const esitmation = await this.estimationService.calculateRoute(
      { lat: routeVal.pickup.location.lat, lng: routeVal.pickup.location.lng },
      { lat: routeVal.destination.location.lat, lng: routeVal.destination.location.lng },
      routeVal.pickup.address,
      routeVal.destination.address
    )
    this.showRouteOnMap(routeVal, esitmation?.routeCoordinates);
  }

  // TODO: REFACTOR THIS...
  showRouteOnMap(route: Route, routeCoordinates?: L.LatLng[]) {
    this.mapService.drawRoute('', 
      [route.pickup.location.lat, route.pickup.location.lng],
      [route.destination.location.lat, route.destination.location.lng],
      routeCoordinates
    );
  }
}