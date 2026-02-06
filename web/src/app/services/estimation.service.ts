import { inject, Injectable } from '@angular/core';
import * as L from 'leaflet';
import 'leaflet-routing-machine';
import { GeocodingService } from './geocoding.service';
import { forkJoin, from, Observable, of, switchMap } from 'rxjs';

export interface RouteEstimation {
  distance: number;
  duration: number;
  routeGeometry: L.LatLng[];
}

@Injectable({
  providedIn: 'root',
})
export class EstimationService {

  private geocoding = inject(GeocodingService);

  calculateRouteFromAddress(pickup: string, destination: string): Observable<RouteEstimation | null> {
    return forkJoin({
      pickupLatLng: this.geocoding.geocodeAddress(pickup),
      destLatLng: this.geocoding.geocodeAddress(destination)
    }).pipe(
      switchMap(({ pickupLatLng, destLatLng }) => {
        if (!pickupLatLng || !destLatLng) {
          return of(null);
        }
        return this.calculateRouteFromCoords(pickupLatLng, destLatLng);
      })
    );
  }

  calculateRouteFromCoords(pickup: L.LatLng, destination: L.LatLng): Observable<RouteEstimation | null> {
    const promise = new Promise<RouteEstimation | null>((resolve) => {
      const router = L.Routing.osrmv1({
        serviceUrl: 'https://router.project-osrm.org/route/v1'
      });

      const callback = (err: any, routes: any[]) => {
        if (err || !routes || routes.length === 0) {
          if (err) console.error('Routing error:', err);
          resolve(null);
          return;
        }

        const route = routes[0];
        resolve({
          distance: parseFloat((route.summary.totalDistance / 1000).toFixed(1)),
          duration: Math.round(route.summary.totalTime / 60),
          routeGeometry: route.coordinates || []
        });
      };

      router.route(
        [
          { latLng: pickup },
          { latLng: destination }
        ],
        callback as any
      );
    });

    return from(promise);
  }
}
