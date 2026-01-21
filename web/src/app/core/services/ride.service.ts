import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

// Interface matching your backend DTO
export interface Address {
  street?: string;
  city?: string;
  country?: string;
  postalCode?: string;
  latitude?: number;
  longitude?: number;
}

export interface RideHistoryResponse {
  rideId: number;
  startTime: string;
  endTime: string;
  pickupAddress: Address;
  destinationAddress: Address;
  price: number;
  rideStatus: 'PENDING' | 'ACCEPTED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
}

export interface RideDetailResponse {
  rideId: number;
  startTime: string;
  endTime: string;
  pickupAddress: Address;
  destinationAddress: Address;
  price: number;
  rideStatus: string;
  duration?: string;
  passengerName?: string;
  passengerPhone?: string;
  distance?: number;
  paymentMethod?: string;
  notes?: string;
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
}