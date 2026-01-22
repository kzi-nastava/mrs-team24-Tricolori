import { VehicleDto } from "./vehicle";

export interface ProfileResponse {
  email: string;
  firstName: string;
  lastName: string;
  homeAddress: string;
  phoneNumber: string;
  pfp: string;
  vehicle: VehicleDto | null; // Može biti null ako korisnik nije vozač
  activeHours: number | null; // Može biti null
}