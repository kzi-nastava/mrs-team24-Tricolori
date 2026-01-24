export interface StepOneDriverRegistrationData {
  firstName: string;
  lastName: string;
  phone: string;
  address: string;
  pfpFile: File | null;
}

export interface StepTwoDriverRegistrationData {
  email: string;
  vehicleModel: string;
  vehicleType: string;
  registrationPlate: string;
  seatNumber: number;
  petFriendly: boolean;
  babyFriendly: boolean;
}

export interface AdminDriverRegistrationRequest {
  firstName: string;
  lastName: string;
  phone: string;
  address: string;
  email: string;
  vehicleModel: string;
  vehicleType: string;
  registrationPlate: string;
  seatNumber: number;
  petFriendly: boolean;
  babyFriendly: boolean;
}

export interface DriverPasswordSetupRequest {
  token: string;
  password: string;
}
