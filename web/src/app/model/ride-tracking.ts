import { Stop } from "./route";

/**
 * Vehicle location information
 * Matches: com.tricolori.backend.dto.vehicle.VehicleLocationResponse
 */
export interface VehicleLocationResponse {
  vehicleId: number;
  model: string;
  plateNum: string;
  latitude: number;
  longitude: number;
  available: boolean;
}

/**
 * Route information with pickup, destination, and stops
 * Matches: com.tricolori.backend.dto.ride.RouteDto
 */
export interface RouteDto {
  id: number;
  pickupAddress: string;
  pickupLatitude: number;
  pickupLongitude: number;
  destinationAddress: string;
  destinationLatitude: number;
  destinationLongitude: number;
  stops?: Stop[];  // Intermediate stops (optional)
  distanceKm: number;
  estimatedTimeSeconds: number;
}

/**
 * Driver information
 * Matches: com.tricolori.backend.dto.profile.DriverDto
 */
export interface DriverDto {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  profilePicture?: string;
  rating?: number;
}

/**
 * Passenger information
 * Matches: com.tricolori.backend.dto.profile.PassengerDto
 */
export interface PassengerDto {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  profilePicture?: string;
}

/**
 * Real-time ride tracking response
 * Matches: com.tricolori.backend.dto.ride.RideTrackingResponse
 */
export interface RideTrackingResponse {
  rideId: number;
  status: string;  // RideStatus enum value (e.g., "SCHEDULED", "ONGOING", "FINISHED", "PANIC")
  currentLocation: VehicleLocationResponse | null;
  route: RouteDto | null;
  estimatedTimeMinutes: number | null;
  estimatedArrival: string | null;  // ISO 8601 datetime string
  scheduledFor: string | null;      // ISO 8601 datetime string
  startTime: string | null;         // ISO 8601 datetime string
  price: number | null;
  driver: DriverDto | null;
  passengers: PassengerDto[] | null;
}

/**
 * Request to report route inconsistency
 * Matches: com.tricolori.backend.dto.InconsistencyReportRequest
 */
export interface InconsistencyReportRequest {
  description: string;
}

/**
 * Request to trigger panic alert
 * Matches: com.tricolori.backend.dto.PanicRideRequest
 */
export interface PanicRideRequest {
  vehicleLocation: {
    lat: number;
    lng: number;
  };
}