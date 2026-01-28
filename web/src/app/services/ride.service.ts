import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PanicRequest, RideRequest, StopRideRequest, StopRideResponse } from '../model/ride';
import { environment } from '../../environments/environment';
import {
  RideTrackingResponse,
  InconsistencyReportRequest,
  PanicRideRequest
} from '../model/ride-tracking';

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

export interface RideRatingRequest {
  driverRating: number;
  vehicleRating: number;
  comment: string;
}

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
  ridePanic(rideId: number, panicRequest: PanicRideRequest): Observable<void> {
    return this.http.put<void>(`${this.API_URL}/${rideId}/panic`, panicRequest);
  }

  // Cancel a ride with a reason
  cancelRide(rideId: number, reason: string): Observable<void> {
    return this.http.put<void>(`${this.API_URL}/${rideId}/cancel`, { reason: reason });
  }

  bookRide(request: RideRequest): Observable<any> {
    return this.http.post(`${this.API_URL}/order`, {
      route: {
        pickup: {
          address: request.route.pickup.address,
          location: {
            longitude: request.route.pickup.location.lng,
            latitude: request.route.pickup.location.lat
          }
        },
        destination: {
          address: request.route.destination.address,
          location: {
            longitude: request.route.destination.location.lng,
            latitude: request.route.destination.location.lat
          }
        },
        stops: (request.route.stops || []).map(s => ({
          address: s.address,
          location: {
            longitude: s.location.lng,
            latitude: s.location.lat
          }
        }))
      },
      preferences: {
        vehicleType: request.preferences.vehicleType,
        petFriendly: request.preferences.petFriendly,
        babyFriendly: request.preferences.babyFriendly,
        scheduledFor: this.formatLocalDateTime(request.preferences.schedule)
      },
      estimations: {
        distanceKilometers: request.estimation.distanceKilometers,
        durationMinutes: request.estimation.durationMinutes
      },
      createdAt: this.formatLocalDateTime(new Date()),
      trackers: request.trackers
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
  stopRide(rideId: number, stopRideRequest: StopRideRequest) : Observable<StopRideResponse> {
    return this.http.put<StopRideResponse>(`${this.API_URL}/${rideId}/stop`, stopRideRequest);
  }

  // Report route inconsistency
  reportInconsistency(rideId: number, request: InconsistencyReportRequest): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/${rideId}/report-inconsistency`, request);
  }
}