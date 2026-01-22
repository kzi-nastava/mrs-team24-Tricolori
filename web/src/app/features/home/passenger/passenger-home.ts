// passenger-home.ts
import { Component, inject, signal, viewChild } from '@angular/core';
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
  templateUrl: './passenger-home.html',
  styleUrl: './passenger-home.css'
})
export class HomePassenger {
  routeSelector = viewChild.required(RouteSelector);
  preferencesSelector = viewChild.required(PreferencesSelector);
  trackersSelector = viewChild.required(RideTrackersSelector);
  
  private routesDialog = inject(MatDialog);
  private routeOverlay = inject(Overlay);

  private scheduleDialog = inject(MatDialog);
  private scheduleOverlay = inject(Overlay);

  currentRoute = signal<Route | undefined>(undefined);
  scheduledTime = signal<Date | undefined>(undefined);

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

  rideSubmit(event: Event) {
    event.preventDefault();
    
    // Pristup child komponenti kroz signal
    const routeSelector = this.routeSelector();
    const preferencesSelector = this.preferencesSelector();
    const trackersSelector = this.trackersSelector();
    
    // Since route selection is only mandatory data, I don't check
    // other child components' validity...
    if (routeSelector.routeForm.valid) {
      const routeVal: Route = routeSelector.routeForm.value;
      const preferencesVal: RideOptions = preferencesSelector.preferencesForm.value;
      const trackersVal: string[] = trackersSelector.trackersForm.getRawValue().trackers as string[] || [];

      const rideRequest: RideRequest = {
        route: routeVal,
        preferences: preferencesVal,
        trackers: trackersVal
      }

      console.log('Route Data:', rideRequest);
    } else {
      routeSelector.routeForm.markAllAsTouched();
    }
  }
}