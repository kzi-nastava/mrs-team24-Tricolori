export type VehicleType = 'STANDARD' | 'LUXURY' | 'VAN'

export interface VehicleDto {
  model: string;
  type: string;
  plateNumber: string;
  numSeats: number;
  babyFriendly: boolean;
  petFriendly: boolean;
}
