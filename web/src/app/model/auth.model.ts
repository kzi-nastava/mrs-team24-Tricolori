
export type PersonRole = 'ROLE_DRIVER' | 'ROLE_PASSENGER' | 'ROLE_ADMIN' | 'ROLE_GUEST';


export type AccountStatus = 'ACTIVE' | 'WAITING_FOR_ACTIVATION' | 'SUSPENDED';

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  homeAddress: string;
  phoneNum: string;
}

export interface PersonDto {
  id: any;
  role: PersonRole;
  firstName: string;
  lastName: string;
  phoneNum: string;
  homeAddress: string;
  email: string;
  accountStatus: AccountStatus;
  pfpUrl: string;
  block: any;
}


export interface LoginRequest {
  email: string;
  password: string;
}


export interface LoginResponse {
  accessToken: string;
  personDto: PersonDto;
}
