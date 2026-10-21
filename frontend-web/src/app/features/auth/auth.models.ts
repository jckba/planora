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
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  primaryCurrencyId: number;
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
