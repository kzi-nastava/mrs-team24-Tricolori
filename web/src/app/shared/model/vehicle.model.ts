export type VehicleType = 'standard' | 'luxury' | 'van'

export interface VehicleDto {
  model: string;
  type: string;
  plateNumber: string;
  numSeats: number;
  babyFriendly: boolean;
  petFriendly: boolean;
}
