export interface DriverRegistrationData 
  extends StepOneDriverRegistrationData, StepTwoDriverRegistrationData
{}

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