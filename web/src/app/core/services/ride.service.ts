import { Injectable } from '@angular/core';
import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import { Observable } from 'rxjs';

// Interfaces matching your backend DTOs
export interface RideHistoryResponse {
  id: number;
  passengerName: string;
  pickupAddress: string;
  dropoffAddress: string;
  status: string;
  totalPrice: number;
  distance: number;
  duration: number;
  createdAt: string;
  completedAt: string;
  driverRating: number | null;
  vehicleRating: number | null;
}

export interface RideDetailResponse {
  id: number;
  passengerName: string;
  passengerPhone: string;
  driverName: string;
  vehicleModel: string;
  vehicleLicensePlate: string;
  pickupAddress: string;
  pickupLatitude: number;
  pickupLongitude: number;
  dropoffAddress: string;
  dropoffLatitude: number;
  dropoffLongitude: number;
  status: string;
  totalPrice: number;
  distance: number;
  duration: number;
  createdAt: string;
  acceptedAt: string;
  startedAt: string;
  completedAt: string;
  driverRating: number | null;
  vehicleRating: number | null;
  ratingComment: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class RideService {
  private apiUrl = 'http://localhost:8080/api/v1/rides';

  constructor(private http: HttpClient) {}

  /**
   * Get driver's ride history with optional date filtering
   */
  getDriverHistory(
    startDate?: string,
    endDate?: string,
    sortBy: string = 'createdAt',
    sortDirection: string = 'DESC'
  ): Observable<RideHistoryResponse[]> {
    let params = new HttpParams()
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);

    if (startDate) {
      params = params.set('startDate', startDate);
    }
    if (endDate) {
      params = params.set('endDate', endDate);
    }

    return this.http.get<RideHistoryResponse[]>(`${this.apiUrl}/history/driver`, { params });
  }

  /**
   * Get detailed information for a specific ride
   */
  getDriverRideDetail(rideId: number): Observable<RideDetailResponse> {
    return this.http.get<RideDetailResponse>(`${this.apiUrl}/${rideId}/details/driver`);
  }

  cancelRide(rideId: number, reason: string) : Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${rideId}/cancel`, { reason: reason });
  }
}
