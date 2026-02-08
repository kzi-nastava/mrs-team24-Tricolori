import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { environment } from '../../environments/environment';
import { FavoriteRoute } from '../model/route';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class FavoriteRoutesService {
  private http = inject(HttpClient)
  private API_URL = `${environment.apiUrl}/favorite-routes`;

  private favoriteRoutes_ = signal<FavoriteRoute[]>([]);
  public favoriteRoutes = this.favoriteRoutes_.asReadonly();

  getFavoriteRoutes(): void {
    this.http.get<any[]>(`${this.API_URL}`).pipe(
      map(backendData => backendData.map(item => ({
        title: item.title,
        route: {
          pickup: {
            address: item.route.pickupStop.address,
            location: {
              lat: item.route.pickupStop.location.latitude,
              lng: item.route.pickupStop.location.longitude
            }
          },
          destination: {
            address: item.route.destinationStop.address,
            location: {
              lat: item.route.destinationStop.location.latitude,
              lng: item.route.destinationStop.location.longitude
            }
          },
          stops: item.route.stops.map((s: any) => ({
            address: s.address,
            location: {
              lat: s.location.latitude,
              lng: s.location.longitude
            }
          }))
        }
      } as FavoriteRoute)))
    ).subscribe({
      next: (routes) => this.favoriteRoutes_.set(routes),
      error: (err) => console.error("Greška pri dobavljanju ruta:", err)
    });
  }
}
