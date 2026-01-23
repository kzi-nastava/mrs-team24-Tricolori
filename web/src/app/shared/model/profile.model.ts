import { VehicleDto } from "./vehicle.model";

export interface ProfileResponse {
  email: string;
  firstName: string;
  lastName: string;
  homeAddress: string;
  phoneNumber: string;
  pfp: string;
  vehicle: VehicleDto | null; // Can be null if user is not a driver
  activeHours: number | null; // Can be null if user is not a driver
}

export interface ProfileRequest {
  firstName: string;
  lastName: string;
  homeAddress: string;
  phoneNumber: string;
  pfp: string;
}
