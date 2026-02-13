import { AccountStatus } from "./auth.model";

export interface ActivePersonStatus {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  registrationDate: string;
  status: AccountStatus;
}

export interface BlockRequest {
  blockReason: string;
  userEmail: string;
}