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