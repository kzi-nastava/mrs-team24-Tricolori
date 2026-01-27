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

  getFavoriteRoutes(userId: number): void {
    this.http.get<FavoriteRoute[]>(`${this.API_URL}/${userId}`)
      .pipe(
        map(backendRoutes => backendRoutes.map(route => ({
          pickup: route.pickup,
          destination: route.destination,
          stops: route.stops,
          title: "Neki naslov"
        })))
      )
      .subscribe(routes => {
        this.favoriteRoutes_.set(routes);
      });
  }
}
