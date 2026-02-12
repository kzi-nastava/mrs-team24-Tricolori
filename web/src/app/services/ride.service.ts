import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {PanicRequest, PassengerRide, RideRequest, StopRideRequest, StopRideResponse} from '../model/ride';
import { RideHistoryResponse, RideDetailResponse, RideRatingRequest, PassengerRideHistoryResponse } from '../model/ride-history';
import { environment } from '../../environments/environment';
import {
  RideTrackingResponse,
  InconsistencyReportRequest,
  PanicRideRequest
} from '../model/ride-tracking';


@Injectable({
  providedIn: 'root'
})
export class RideService {
  readonly API_URL = `${environment.apiUrl}/rides`;

  constructor(private http: HttpClient) {}

  // Get driver's ride history with optional date filtering
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

  getPassengerHistory(
    startDate?: string,
    endDate?: string,
    page: number = 0,
    size: number = 10,
    sort: string = 'createdAt,desc',
  ): Observable<PassengerRide[]> {

    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort)

    if (startDate) {
      params = params.set('startDate', startDate);
    }
    if (endDate) {
      params = params.set('endDate', endDate);
    }

    return this.http.get<PassengerRide[]>(`${this.API_URL}/passenger`, { params });
  }

  // Get detailed information for a specific ride (for driver)
  getDriverRideDetail(rideId: number): Observable<RideDetailResponse> {
    return this.http.get<RideDetailResponse>(`${this.API_URL}/${rideId}/details/driver`);
  }

  // Get detailed information for a specific ride (for passenger)
  getPassengerRideDetail(rideId: number): Observable<RideDetailResponse> {
    return this.http.get<RideDetailResponse>(`${this.API_URL}/${rideId}/details/passenger`);
  }

  // Track a ride in real-time - get current status, location, and estimates
  trackRide(rideId: number): Observable<RideTrackingResponse> {
    return this.http.get<RideTrackingResponse>(`${this.API_URL}/${rideId}/track`);
  }

  // Update passenger location during ride
  updatePassengerLocation(rideId: number, location: { latitude: number; longitude: number }): Observable<void> {
    return this.http.put<void>(`${this.API_URL}/${rideId}/passenger-location`, location);
  }

  // Update vehicle location during ride (for testing/simulation)
  updateVehicleLocation(rideId: number, location: { latitude: number; longitude: number }): Observable<void> {
    return this.http.put<void>(`${this.API_URL}/${rideId}/vehicle-location`, location);
  }

  // Rate a completed ride
  rateRide(rideId: number, request: RideRatingRequest): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/${rideId}/rate`, request);
  }

  // Trigger panic alert for a ride
  ridePanic(panicRequest: PanicRideRequest): Observable<void> {
    return this.http.put<void>(`${this.API_URL}/panic`,  { panicRequest: panicRequest });
  }

  // Cancel a ride with a reason
  cancelRide(reason: string): Observable<void> {
    return this.http.put<void>(`${this.API_URL}/cancel`, { reason: reason });
  }

  bookRide(orderData: any): Observable<any> {
    return this.http.post(`${this.API_URL}/order`, orderData, {
      responseType: 'text'
    });
  }

  // Use this method on 'Date' object before sending to backend
  // if backend is expecting 'LocalDateTime'
  formatLocalDateTime(date: Date | null): string | null {
    if (!date) return null;

    const pad = (n: number) => n < 10 ? '0' + n : n;

    return date.getFullYear() + '-' +
      pad(date.getMonth() + 1) + '-' +
      pad(date.getDate()) + 'T' +
      pad(date.getHours()) + ':' +
      pad(date.getMinutes()) + ':' +
      pad(date.getSeconds());
  }
  stopRide(stopRideRequest: StopRideRequest) : Observable<StopRideResponse> {
    return this.http.put<StopRideResponse>(`${this.API_URL}/stop`, stopRideRequest);
  }

  completeRide(rideId: number): Observable<void> {
    return this.http.put<void>(`${this.API_URL}/${rideId}/complete`, {});
  }

  reportInconsistency(rideId: number, request: InconsistencyReportRequest): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/${rideId}/report-inconsistency`, request);
  }

  startRide(rideId: number): Observable<any> {
    return this.http.put(`${this.API_URL}/${rideId}/start`, {});
  }
}
