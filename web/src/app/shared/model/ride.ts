import { Route } from "./route";
import { VehicleType } from "./vehicle.model";
import { Location } from './location';

export interface RideOptions {
    vehicleType: VehicleType;
    petFriendly: boolean;
    babyFriendly: boolean;
    schedule?: Date;
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
  pickupCoords: [number, number];
  destinationCoords: [number, number];
  driverName: string;
  vehicleType: string;
  licensePlate: string;
  totalDistance: number;
  estimatedDuration: number;
}

export interface StopRideRequest {
  location: Location
}

export interface StopRideResponse {
  updatedPrice: number
}
