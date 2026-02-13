import { Route } from "./route";
import { VehicleType } from "./vehicle.model";
import { Location } from './location';
import { Passenger } from "./passenger.model";

export interface RideOptions {
    vehicleType: VehicleType;
    petFriendly: boolean;
    babyFriendly: boolean;
    schedule: Date | null;
}

export interface RideRequest {
    route: Route;
    preferences: RideOptions;
    trackers: string[];
}

export interface PanicRequest {
  vehicleLocation: Location;
}

export interface RideDetails {
  id: number;
  pickup: string;
  destination: string;
  pickupCoords: number[];  // Changed from [number, number] to number[]
  destinationCoords: number[];  // Changed from [number, number] to number[]
  driverName: string;
  vehicleType: string;
  licensePlate: string;
  totalDistance: number;
  estimatedDuration: number;
  passengers?: Passenger[];
}

export interface StopRideRequest {
  location: Location
}

export interface StopRideResponse {
  updatedPrice: number
}

export interface RideAssignment {
  id: number;
  pickupAddress: string;
  destinationAddress: string;
  passengerName: string;
  passengerPhone: string;
  estimatedDistance: number;
  estimatedDuration: number;
  estimatedPrice: number;
  pickupCoords: [number, number];
  destinationCoords: [number, number];
  eta: number;
}

export type RideStatus =
  | 'SCHEDULED'
  | 'ONGOING'
  | 'FINISHED'
  | 'PANIC'
  | 'CANCELLED_BY_DRIVER'
  | 'CANCELLED_BY_PASSENGER'
  | 'DECLINED'
  | 'STOPPED'
  | 'REJECTED';

export function getStatusClass(status: string): string {
  switch (status) {
    case 'FINISHED':
      return 'bg-green-100 text-green-700 border-green-200';
    case 'ONGOING':
      return 'bg-blue-100 text-blue-700 border-blue-200';
    case 'SCHEDULED':
      return 'bg-yellow-100 text-yellow-700 border-yellow-200';
    case 'PANIC':
      return 'bg-red-600 text-white border-red-700 animate-pulse';
    case 'CANCELLED_BY_PASSENGER':
    case 'CANCELLED_BY_DRIVER':
    case 'REJECTED':
    case 'DECLINED':
      return 'bg-red-100 text-red-700 border-red-200';
    default:
      return 'bg-gray-100 text-gray-700 border-gray-200';
  }
}

export interface RideHistory {
  id: number;
  pickupAddress: string;
  destinationAddress: string;
  createdAt: string;
  status: RideStatus;
  price: number;
}

interface PassengerRideDetails {
  id: number;
  routeId: number;
  route: string;
  startDate: string;
  endDate: string;
  price: number;
  status: RideStatus;
  startTime: string;
  endTime: string;
  duration: string;
  driverName: string;
  driverPhone: string;
  vehicleType: string;
  licensePlate: string;
  distance: number;
  paymentMethod: string;
  notes?: string;
  rating?: {
    driverRating: number;
    vehicleRating: number;
    comment: string;
    ratedAt: string;
  };
  completedAt: Date;
  canRate: boolean;
  ratingExpired: boolean;
  driverRating?: number | null;
  vehicleRating?: number | null;
  // Map data
  pickupLat?: number;
  pickupLng?: number;
  dropoffLat?: number;
  dropoffLng?: number;
}
