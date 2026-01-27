import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface RideRatingRequest {
  driverRating: number;
  vehicleRating: number;
  comment?: string;
}

export interface RideRatingResponse {
  canRate: boolean;
  alreadyRated: boolean;
  deadlinePassed: boolean;
  deadline: string; 
}

@Injectable({
  providedIn: 'root'
})
export class RatingService {
  private readonly API_URL = `${environment.apiUrl}/rides`;

  constructor(private http: HttpClient) {}

  submitRating(rideId: string | number, ratingData: RideRatingRequest): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/${rideId}/rate`, ratingData);
  }

  getRatingStatus(rideId: string | number): Observable<RideRatingResponse> {
    return this.http.get<RideRatingResponse>(`${this.API_URL}/${rideId}/rating-status`);
  }
}