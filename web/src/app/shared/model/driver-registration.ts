export interface DriverRegistrationData 
  extends StepOneDriverRegistrationData, StepTwoDriverRegistrationData
{}

export interface StepOneDriverRegistrationData {
  firstName: string;
  lastName: string;
  phone: string;
  address: string;
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