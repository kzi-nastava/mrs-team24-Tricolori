import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { environment } from '../../environments/environment';
import { FavoriteRoute } from '../model/route';
import { map } from 'rxjs/operators';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class FavoriteRoutesService {
  private http = inject(HttpClient)
  private API_URL = `${environment.apiUrl}/favorite-routes`;

  addFavoriteRoute(routeId: number, title: string): Observable<any> {
    const body = { routeId, title };
    return this.http.post(`${this.API_URL}/add`, body);
  }

  removeFavoriteRoute(routeId: number): Observable<any> {
    return this.http.delete(`${this.API_URL}/remove/${routeId}`)
  }

  getFavoriteRoutes(): Observable<FavoriteRoute[]> {
    return this.http.get<any[]>(`${this.API_URL}`).pipe(
      map(backendData => backendData.map(item => ({
        routeId: item.routeId,
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
    )
  }
}
