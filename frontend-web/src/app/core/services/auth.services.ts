import {Observable, tap, of} from 'rxjs';
import {
  CompleteRegistrationRequest,
  EmailVerificationConfirmationResponse,
  LoginRequest,
  LoginResponse,
  UserRegistrationRequest
} from "../../features/auth/auth.models"
import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {API_CONFIG} from '../config/api.config';
import {CurrentUser} from "../../features/auth/current-user.model";
import {Currency} from "../../features/auth/currency.model";
import {GoogleRegistrationRequest, GoogleSessionResponse} from "../../features/auth/auth.models"

type TokenUse = 'ACCESS' | 'ONBOARDING';

interface ActiveCredential {
  token: string;
  tokenUse: TokenUse;
  expiresAt: number;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly credentialStorageKey = 'planora_active_credential';

  private activeCredential: ActiveCredential | null = this.restoreCredential();

  private currenciesCache: Currency[] | null = null;

  private readonly currenciesCacheKey = 'planora_currencies_cache';
  private readonly currenciesCacheTimeKey = 'planora_currencies_cache_time';
  private readonly currenciesCacheTtlMs = 30 * 60 * 1000

  private storeCredential(
    token: string,
    expiresIn: number,
    expectedTokenUse?: TokenUse
  ): void {
    const tokenUse = this.extractTokenUse(token);

    if (!tokenUse) {
      this.clearSession();
      throw new Error('El JWT recibido no contiene un token_use valido.');
    }

    if (expectedTokenUse && tokenUse !== expectedTokenUse) {
      this.clearSession();
      throw new Error(
        `Se esperaba un token ${expectedTokenUse}, pero se recibió ${tokenUse}`);
    }

    if (!Number.isFinite(expiresIn) || expiresIn <= 0) {
      this.clearSession();
      throw new Error('El JWT recibido no contiene un expiresIn valido.');
    }

    const credential: ActiveCredential = {
      token,
      tokenUse,
      expiresAt: Date.now() + expiresIn * 1000
    };

    this.activeCredential = credential;

    try {
      sessionStorage.setItem(
        this.credentialStorageKey,
        JSON.stringify(credential)
      );
    } catch {
      // La sesión seguirá disponible en memoria durante esta carga.
    }
  }

  private restoreCredential(): ActiveCredential | null {
    try {
      const rawCredential = sessionStorage.getItem(this.credentialStorageKey);

      if (!rawCredential) {
        return null;
      }

      const credential: unknown = JSON.parse(rawCredential);

      if (
        credential === null ||
        typeof credential !== 'object' ||
        !('token' in credential) ||
        !('tokenUse' in credential) ||
        !('expiresAt' in credential)
      ) {
        sessionStorage.removeItem(this.credentialStorageKey);
        return null;
      }

      const saved = credential as ActiveCredential;

      if (
        typeof saved.token !== 'string' ||
        (saved.tokenUse !== 'ACCESS' &&
          saved.tokenUse !== 'ONBOARDING') ||
        typeof saved.expiresAt !== 'number' ||
        saved.expiresAt <= Date.now()
      ) {
        sessionStorage.removeItem(this.credentialStorageKey);
        return null;
      }

      const actualTokenUse = this.extractTokenUse(saved.token);

      if (actualTokenUse !== saved.tokenUse) {
        sessionStorage.removeItem(this.credentialStorageKey);
        return null;
      }

      return saved;
    } catch {
      return null;
    }
  }

  private extractTokenUse(token: string): TokenUse | null {
    try {
      const parts = token.split('.');

      if (parts.length !== 3) {
        return null;
      }

      const payload = parts[1]
        .replace(/-/g, '+')
        .replace(/_/g, '/');

      const paddedPayload = payload.padEnd(
        payload.length + ((4 - payload.length % 4) % 4), '='
      );

      const binaryPayload = atob(paddedPayload);

      const bytes = Uint8Array.from(
        binaryPayload,
        character => character.charCodeAt(0)
      );

      const decodedPayload = new TextDecoder().decode(bytes);
      const claims: unknown = JSON.parse(decodedPayload);

      if (
        claims === null ||
        typeof claims !== 'object' ||
        !('token_use' in claims)
      ) {
        return null;
      }

      const tokenUse = (claims as { token_use: string }).token_use;

      if (tokenUse === 'ACCESS' || tokenUse === 'ONBOARDING') {
        return tokenUse;
      }

      return null;
    } catch {
      return null;
    }
  }

  private getCurrentCredential(): ActiveCredential | null {
    if (!this.activeCredential) {
      return null;
    }

    if (this.activeCredential.expiresAt <= Date.now()) {
      this.clearSession();
      return null;
    }

    return this.activeCredential;
  }

  setAccessToken(token: string, expiresIn: number): void {
    this.storeCredential(token, expiresIn, 'ACCESS');

  }

  setOnboardingToken(token: string, expiresIn: number): void {
    this.storeCredential(token, expiresIn, 'ONBOARDING');
  }

  getAccessToken(): string | null {
    const credential = this.getCurrentCredential();

    return credential?.tokenUse === 'ACCESS' ? credential.token : null;
  }

  getOnboardingToken(): string | null {
    const credential = this.getCurrentCredential();

    return credential?.tokenUse === 'ONBOARDING' ? credential.token : null;
  }

  getActiveToken(): string | null {
    return this.getCurrentCredential()?.token ?? null;
  }

  getActiveTokenUse(): string | null {
    return this.getCurrentCredential()?.tokenUse ?? null;
  }

  getTokenForRequest(url: string): string | null {

    const credential = this.getCurrentCredential();
    if (!credential) {
      return null;
    }

    const isCompleteRegistrationRequest = /\/auth\/complete-registration(?:\?|$)/.test(url);

    if (isCompleteRegistrationRequest) {
      return credential.tokenUse === 'ONBOARDING'
        ? credential.token : null;
    }

    return credential.tokenUse === 'ACCESS'
      ? credential.token : null;

  }


  hasAccessToken(): boolean {
    return this.getAccessToken() !== null;
  }

  hasOnboardingToken(): boolean {
    return this.getOnboardingToken() !== null;
  }

  hasActiveSession(): boolean {
    return this.getActiveToken() !== null;
  }

  clearSession(): void {
    this.activeCredential = null;

    try {
      sessionStorage.removeItem(this.credentialStorageKey);
    } catch {

    }
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(
      `${API_CONFIG.baseUrl}/auth/login`,
      request,
    ).pipe(
      tap(response => {
        this.storeCredential(response.token, response.expiresIn);
      })
    );
  }

  register(request: UserRegistrationRequest): Observable<void> {
    return this.http.post<void>(
      `${API_CONFIG.baseUrl}/auth/register`,
      request,
    );
  }

  completeRegistration(request: CompleteRegistrationRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(
      `${API_CONFIG.baseUrl}/auth/complete-registration`,
      request
    ).pipe(
      tap(response => {
        this.storeCredential(
          response.token,
          response.expiresIn,
          'ACCESS'
        );
      })
    );
  }

  getCurrentUser(): Observable<CurrentUser> {
    return this.http.get<CurrentUser>(
      `${API_CONFIG.baseUrl}/auth/me`
    );
  }

  // ------------------------------------
  // GOOGLE WITH OAUTH2
  // ------------------------------------
  startGoogleSession(): Observable<GoogleSessionResponse> {
    return this.http.post<GoogleSessionResponse>(
      `${API_CONFIG.baseUrl}/auth/google/session`,
      {},
      {withCredentials: true},
    ).pipe(
      tap(response => {
        if (response.login) {
          this.storeCredential(
            response.login.token,
            response.login.expiresIn
          );
        }
      })
    );
  }

  completeGoogleRegistration(request: GoogleRegistrationRequest): Observable<LoginResponse> {
    return this.completeRegistration(request);
  }

  linkGoogleAccount(): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(
      `${API_CONFIG.baseUrl}/auth/google/link`,
      {},
      {withCredentials: true},
    ).pipe(
      tap(response => {
        this.storeCredential(
          response.token,
          response.expiresIn
        );
      })
    );
  }

  // ------------------------------------
  // VERIFY EMAIL
  // ------------------------------------
  confirmEmailVerification(email: string, code: string): Observable<EmailVerificationConfirmationResponse> {
    return this.http.post<EmailVerificationConfirmationResponse>(
      `${API_CONFIG.baseUrl}/auth/email-verification/confirm`,
      {email, code}
    ).pipe(
      tap(response => {
        this.storeCredential(
          response.token,
          response.expiresIn,
          'ONBOARDING'
        );
      })
    );
  }

  resendEmailVerification(email: string): Observable<void> {
    return this.http.post<void>(
      `${API_CONFIG.baseUrl}/auth/email-verification/resend`,
      {email}
    );
  }

  // ------------------------------------
  // LOAD AND SELECT CURRENCY
  // ------------------------------------

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
            typeof currency.symbol === 'string' &&
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


  setPendingVerificationEmail(email: string): void {
    try {
      sessionStorage.setItem('planora_pending_verification_email', email);
    } catch {

    }
  }

  getPendingVerificationEmail(): string | null {
    try {
      return sessionStorage.getItem('planora_pending_verification_email');
    } catch {
      return null;
    }
  }

  clearPendingVerificationEmail(): void {
    try {
      sessionStorage.removeItem('planora_pending_verification_email');
    } catch {

    }
  }

}
