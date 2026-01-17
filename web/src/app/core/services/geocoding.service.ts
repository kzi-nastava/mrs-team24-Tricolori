import { Injectable } from '@angular/core';

export interface GeocodingResult {
  lat: number;
  lng: number;
  displayName: string;
}

@Injectable({
  providedIn: 'root',
})
export class GeocodingService {
  private readonly NOMINATIM_URL = 'https://nominatim.openstreetmap.org/search';
  private readonly DEFAULT_CITY = 'Novi Sad';

  /**
   * Geocodes an address to coordinates using Nominatim
   * @param address - Address string to geocode
   * @param city - Optional city to narrow search (defaults to Novi Sad)
   */
  async geocodeAddress(address: string, city: string = this.DEFAULT_CITY): Promise<GeocodingResult | null> {
    try {
      const query = city ? `${address}, ${city}` : address;
      const response = await fetch(
        `${this.NOMINATIM_URL}?format=json&q=${encodeURIComponent(query)}`
      );

      const results = await response.json();

      if (results.length === 0) {
        return null;
      }

      const firstResult = results[0];
      return {
        lat: parseFloat(firstResult.lat),
        lng: parseFloat(firstResult.lon),
        displayName: firstResult.display_name
      };
    } catch (error) {
      console.error('Geocoding error:', error);
      return null;
    }
  }

  /**
   * Reverse geocodes coordinates to an address
   * @param lat - Latitude
   * @param lng - Longitude
   */
  async reverseGeocode(lat: number, lng: number): Promise<string | null> {
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`
      );

      const result = await response.json();
      return result.display_name || null;
    } catch (error) {
      console.error('Reverse geocoding error:', error);
      return null;
    }
  }
}
