import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import {PanicRequest, StopRideRequest, StopRideResponse} from '../../shared/model/ride';
import {environment} from '../../../environments/environment';

// Interfaces matching your backend DTOs
export interface RideHistoryResponse {
  id: number;
  passengerName?: string;
  pickupAddress: string;
  destinationAddress: string;
  status: string;
  price: number;
  distance?: number;
  duration?: number;
  startDate: string;
  endDate: string | null;
  driverRating?: number | null;
  vehicleRating?: number | null;
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
  readonly API_URL = `${environment.apiUrl}/rides`;

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

    return this.http.get<RideHistoryResponse[]>(`${this.API_URL}/history/driver`, { params });
  }

  /**
   * Get detailed information for a specific ride (for driver)
   */
  getDriverRideDetail(rideId: number): Observable<RideDetailResponse> {
    return this.http.get<RideDetailResponse>(`${this.API_URL}/${rideId}/details/driver`);
  }

  /**
   * Get detailed information for a specific ride (for passenger)
   */
  getPassengerRideDetail(rideId: number): Observable<RideDetailResponse> {
    return this.http.get<RideDetailResponse>(`${this.API_URL}/${rideId}/details/passenger`);
  }

  ridePanic(rideId: number, panicRequest: PanicRequest): Observable<void> {
    return this.http.put<void>(`${this.API_URL}/${rideId}/panic`, panicRequest);
  }

  cancelRide(rideId: number, reason: string) : Observable<void> {
    return this.http.put<void>(`${this.API_URL}/${rideId}/cancel`, { reason: reason });
  }

  stopRide(rideId: number, stopRideRequest: StopRideRequest) : Observable<StopRideResponse> {
    return this.http.put<StopRideResponse>(`${this.API_URL}/${rideId}/stop`, stopRideRequest);
  }

}
