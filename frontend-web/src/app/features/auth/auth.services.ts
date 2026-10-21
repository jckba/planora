import {Observable, tap, of} from 'rxjs';
import {LoginRequest, LoginResponse, UserRegistrationRequest} from './auth.models';
import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpStatusCode} from '@angular/common/http';
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

  private currenciesCache: Currency[] | null = null;
  private readonly currenciesCacheKey = 'planora_currencies_cache';
  private readonly currenciesCacheTimeKey = 'planora_currencies_cache_time';
  private readonly currenciesCacheTtlMs = 30 * 60 * 1000


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

  register(request: UserRegistrationRequest): Observable<void> {
    return this.http
      .post<void>(
        `${API_CONFIG.baseUrl}/auth/register`,
        request,
      );
  }

  getCurrentUser(): Observable<CurrentUser> {
    return this.http.get<CurrentUser>(
      `${API_CONFIG.baseUrl}/auth/me`
    );
  }

  getCurrencies(): Observable<Currency[]> {
    if (this.currenciesCache !== null) {
      return of(this.currenciesCache);
    }

    const cachedCurrencies = this.readCurrenciesCache();

    if (cachedCurrencies !== null) {
      this.currenciesCache = cachedCurrencies;
      return of(cachedCurrencies);
    }

    return this.http
      .get<Currency[]>(
        `${API_CONFIG.baseUrl}/currencies`
      ).pipe(
        tap((currencies) => {
          this.currenciesCache = currencies;

          try {
            sessionStorage.setItem(
              this.currenciesCacheKey,
              JSON.stringify(currencies),
            );
            sessionStorage.setItem(
              this.currenciesCacheTimeKey,
              Date.now().toString(),
            );
          } catch {

          }
        }),
      );
  }

  private readCurrenciesCache(): Currency[] | null {
    try {
      const rawCurrencies = sessionStorage.getItem(
        this.currenciesCacheKey,
      );
      const rawTime = sessionStorage.getItem(
        this.currenciesCacheTimeKey,
      );

      if (!rawCurrencies || !rawTime) {
        return null;
      }

      const savedAt = Number(rawTime);

      if (
        !Number.isFinite(savedAt) ||
        Date.now() - savedAt > this.currenciesCacheTtlMs ||
        savedAt > Date.now()
      ) {
        sessionStorage.removeItem(this.currenciesCacheKey);
        sessionStorage.removeItem(this.currenciesCacheTimeKey);
        return null;
      }

      const currencies: unknown = JSON.parse(rawCurrencies);

      if (
        !Array.isArray(currencies) ||
        !currencies.every(
          (currency) =>
            currency !== null &&
            typeof currency === 'object' &&
            typeof currency.id === 'number' &&
            typeof currency.code === 'string' &&
            typeof currency.name === 'string' &&
            typeof currency.symnol === 'string' &&
            typeof currency.decimalPlaces === 'number'
        )
      ) {
        return null;
      }

      return currencies as Currency[];
    } catch {
      return null;
    }
  }

  startGoogleSession(): Observable<GoogleSessionResponse> {
    return this.http.post<GoogleSessionResponse>(
      `${API_CONFIG.baseUrl}/auth/google/session`,
      {},
      {withCredentials: true},
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
      {withCredentials: true},
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
      {withCredentials: true},
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
