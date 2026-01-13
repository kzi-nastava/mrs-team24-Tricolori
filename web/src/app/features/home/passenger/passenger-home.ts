import { Component, inject, signal } from '@angular/core';
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

interface Stop {
  id: number;
  location: string;
}

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
  private routesDialog = inject(MatDialog);
  private routeOverlay = inject(Overlay);

  private scheduleDialog = inject(MatDialog);
  private scheduleOverlay = inject(Overlay);

  currentRoute = signal<Route | undefined>(undefined);
  scheduledTime = signal<Date | undefined>(undefined)

  openFavoriteRoutes() {
    const dialogRef = this.routesDialog.open(FavoriteRouteSelector, {
      width: '100%',
      maxWidth: '32rem',
      panelClass: 'custom-modal',
      backdropClass: 'custom-backdrop',
      autoFocus: false,
      scrollStrategy: this.routeOverlay.scrollStrategies.block()
    });

    dialogRef.afterClosed().subscribe((result: FavoriteRoute | undefined) => {
      if (result) {
        this.currentRoute.set({
          from: result.from,
          stops: result.stops,
          to: result.to
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
}