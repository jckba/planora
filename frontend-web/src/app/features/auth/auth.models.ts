export interface LoginRequest {
  identifier: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface UserRegistrationRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export type GoogleSessionStatus =
  | 'AUTHENTICATED'
  | 'REGISTRATION_REQUIRED'
  | 'LINK_REQUIRED';

export interface GoogleSessionResponse {
  status: GoogleSessionStatus;
  email: string | null;
  firstName: string | null;
  lastName: string | null;
  login: LoginResponse | null;
}

export interface GoogleRegistrationRequest {
  username: string;
  firstName: string;
  lastName: string;
  primaryCurrencyId: number;
}
