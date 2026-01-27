export type VehicleType = 'standard' | 'luxury' | 'van'

export interface VehicleDto {
  model: string;
  type: string;
  plateNumber: string;
  numSeats: number;
  babyFriendly: boolean;
  petFriendly: boolean;
}

export interface VehicleSpecification {
  capacity?: number;
  type?: string;
  // Add other fields from your VehicleSpecificationDto as needed
}

export interface Vehicle {
  vehicleId: number;
  model: string;
  plateNum: string;
  latitude: number;
  longitude: number;
  available: boolean;
  specification?: VehicleSpecification;
}