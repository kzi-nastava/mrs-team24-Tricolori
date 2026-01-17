import { Injectable } from '@angular/core';
import * as L from 'leaflet';
import 'leaflet-routing-machine';

export interface RouteEstimation {
  distance: number;
  duration: number;
  routeCoordinates: L.LatLng[];
}

@Injectable({
  providedIn: 'root',
})
export class EstimationService {

  /**
   * Calculates route estimation using OSRM
   * Used for unregistered users (frontend calculation)
   */
  async calculateRoute(
    pickup: { lat: number; lng: number },
    destination: { lat: number; lng: number },
    pickupAddress: string,
    destinationAddress: string
  ): Promise<RouteEstimation | null> {
    return new Promise((resolve, reject) => {
      try {
        const router = L.Routing.osrmv1({
          serviceUrl: 'https://router.project-osrm.org/route/v1'
        });

        const pickupLatLng = L.latLng(pickup.lat, pickup.lng);
        const destLatLng = L.latLng(destination.lat, destination.lng);

        // Correct callback signature: (err, routes) where err is Error | null
        router.route(
          [
            { latLng: pickupLatLng, name: pickupAddress },
            { latLng: destLatLng, name: destinationAddress }
          ],
          (err: Error | null, routes?: any[]) => {
            if (err) {
              console.error('Routing error:', err);
              resolve(null);
              return;
            }

            if (!routes || routes.length === 0) {
              console.error('No routes found');
              resolve(null);
              return;
            }

            const route = routes[0];
            const summary = route.summary;

            const distanceKm = summary.totalDistance / 1000;
            const durationMin = Math.round(summary.totalTime / 60);

            const estimation: RouteEstimation = {
              distance: parseFloat(distanceKm.toFixed(1)),
              duration: durationMin,
              routeCoordinates: route.coordinates || []
            };

            resolve(estimation);
          }
        );
      } catch (error) {
        console.error('Route calculation error:', error);
        reject(error);
      }
    });
  }
}
