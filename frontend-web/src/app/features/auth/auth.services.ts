import {Observable, tap} from 'rxjs';
import {LoginRequest, LoginResponse} from './auth.models';
import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {API_CONFIG} from '../../core/config/api.config';
import {CurrentUser} from "./current-user.model";
import {Currency} from "./currency.model";
import {GoogleRegistrationRequest, GoogleSessionResponse} from './auth.models';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private accessToken: string | null = null;

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(
        `${API_CONFIG.baseUrl}/auth/login`,
        request,
      )
      .pipe(
        tap((response) => {
          this.accessToken = response.accessToken;
        }),
      );
  }

  getCurrentUser(): Observable<CurrentUser> {
    return this.http.get<CurrentUser>(
      `${API_CONFIG.baseUrl}/auth/me`
    );
  }

  getCurrencies(): Observable<Currency[]> {
    return this.http.get<Currency[]>(
      `${API_CONFIG.baseUrl}/currencies`
    );
  }

  startGoogleSession(): Observable<GoogleSessionResponse> {
    return this.http.post<GoogleSessionResponse>(
      `${API_CONFIG.baseUrl}/auth/google/session`,
      {},
      { withCredentials: true },
    ).pipe(
      tap((response) => {
        if (response.login) {
          this.accessToken = response.login.accessToken;
        }
      })
    );
  }

  completeGoogleRegistration(
    request: GoogleRegistrationRequest
  ): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(
      `${API_CONFIG.baseUrl}/auth/google/complete`,
      request,
      { withCredentials: true },
    ).pipe(
      tap((response) => {
        this.accessToken = response.accessToken;
      }),
    );
  }

  linkGoogleAccount(): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(
      `${API_CONFIG.baseUrl}/auth/google/link`,
      {},
      { withCredentials: true },
    ).pipe(
      tap((response) => {
        this.accessToken = response.accessToken;
      })
    )
  }

  getAccessToken(): string | null {
    return this.accessToken;
  }

  clearSession(): void {
    this.accessToken = null;
  }
}
