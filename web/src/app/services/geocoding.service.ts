import { Injectable } from '@angular/core';
import * as L from 'leaflet';
import { Observable, from, map, catchError, of } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class GeocodingService {
  private readonly NOMINATIM_URL = 'https://nominatim.openstreetmap.org/search';
  private readonly REVERSE_URL = 'https://nominatim.openstreetmap.org/reverse';
  private readonly DEFAULT_CITY = 'Novi Sad';

  /**
   * Geocodes address directly to Leaflet LatLng
   */
  geocodeAddress(address: string, city: string = this.DEFAULT_CITY): Observable<L.LatLng | null> {
    const query = city ? `${address}, ${city}` : address;
    const url = `${this.NOMINATIM_URL}?format=json&q=${encodeURIComponent(query)}&limit=1`;

    return from(fetch(url).then(res => res.json())).pipe(
      map((results: any[]) => {
        if (results && results.length > 0) {
          const first = results[0];
          return L.latLng(parseFloat(first.lat), parseFloat(first.lon));
        }
        return null;
      }),
      catchError(error => {
        console.error('Geocoding error:', error);
        return of(null);
      })
    );
  }

  /**
   * Reverse geocodes coordinates to a string address
   */
  reverseGeocode(lat: number, lng: number): Observable<string | null> {
    const url = `${this.REVERSE_URL}?format=json&lat=${lat}&lon=${lng}`;

    return from(fetch(url).then(res => res.json())).pipe(
      map(result => result.display_name || null),
      catchError(() => of(null))
    );
  }
}
