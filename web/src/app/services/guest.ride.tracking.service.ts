import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface TokenValidationResponse {
  valid: boolean;
  rideId?: number;
  isRegistered?: boolean;
  email?: string;
  message?: string;
}

@Injectable({
  providedIn: 'root'
})
export class GuestRideTrackingService {
  private readonly API_URL = `${environment.apiUrl}/tracking`;

  constructor(private http: HttpClient) {}

  // Validate a guest tracking token
  validateToken(token: string): Observable<TokenValidationResponse> {
    return this.http.get<TokenValidationResponse>(`${this.API_URL}/validate`, {
      params: { token }
    });
  }

  // Track a ride using token (alternative to using ride ID)
  trackRideByToken(token: string): Observable<any> {
    return this.http.get(`${this.API_URL}/ride/${token}`);
  }
}