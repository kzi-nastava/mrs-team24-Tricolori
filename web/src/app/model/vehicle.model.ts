export interface VehicleDto {
  model: string;
  type: string;
  plateNumber: string;
  numSeats: number;
  babyFriendly: boolean;
  petFriendly: boolean;
}

export type VehicleType = 'STANDARD' | 'LUXURY' | 'VAN';

export interface VehicleSpecification {
  type: VehicleType;
  seats: number;
  babyTransport: boolean;
  petTransport: boolean;
}

export interface Vehicle {
  vehicleId: number;
  model: string;
  plateNum: string;
  latitude: number;
  longitude: number;
  available: boolean;
  specification: VehicleSpecification;
}

export interface VehicleLocationUpdate {
  latitude: number;
  longitude: number;
}
