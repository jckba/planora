import {Component, inject, OnInit} from '@angular/core';
import {AuthService} from '../../../core/services/auth.services';
import {Currency} from '../currency.model';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Location} from '@angular/common';
import {CompleteRegistrationRequest} from '../auth.models';
import {HttpErrorResponse} from '@angular/common/http';
import {Router} from '@angular/router';

interface CompleteRegistrationState {
  registrationSource?: 'GOOGLE' | 'LOCAL';
  email?: string | null;
  firstName?: string | null;
  lastName?: string | null;
  username?: string;
  primaryCurrencyId?: number | null;
}

@Component({
  selector: 'app-complete-registration',
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './complete-registration.html',
  styleUrl: './complete-registration.css',
})
export class CompleteRegistrationComponent implements OnInit {

  private readonly authService = inject(AuthService);
  private readonly location = inject(Location);
  private readonly router = inject(Router);

  registrationSource: 'GOOGLE' | 'LOCAL' = 'LOCAL';

  firstName = '';
  lastName = '';
  email = '';

  currencies: Currency[] = [];
  isLoadingCurrencies = true;
  isSubmitting = false;

  errorMessage = '';
  successMessage = '';

  readonly registrationForm = new FormGroup({
    username: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.pattern(/^(?![0-9])[a-zA-Z0-9._-]{5,15}$/)
      ]
    }),

    primaryCurrencyId: new FormControl<number | null>(null, Validators.required)
  });

  ngOnInit(): void {
    const state = this.location.getState() as CompleteRegistrationState;

    this.registrationSource = state.registrationSource ?? 'LOCAL';

    this.email = state.email?.trim() ?? '';
    this.firstName = state.firstName?.trim() ?? '';
    this.lastName = state.lastName?.trim() ?? '';

    this.registrationForm.patchValue({
      username: state.username ?? '',
      primaryCurrencyId: state.primaryCurrencyId ?? null
      });

    if (
      this.registrationSource === 'GOOGLE' &&
      (!this.email ||!this.firstName || !this.lastName)
    ) {
      this.errorMessage = "No se encontraron los nombres del registro de Google. Regrese al paso anterior.";
      this.isLoadingCurrencies = false;
      return;
    }

    this.loadCurrencies();
  }

  private loadCurrencies(): void {
    this.authService.getCurrencies().subscribe({
      next: (currencies) => {
        this.currencies = currencies;
        this.isLoadingCurrencies = false;
      },
      error: () => {
        this.isLoadingCurrencies = false;
        this.errorMessage = "No se pudieron cargar las monedas. Intente nuevamente.";
      }
    });
  }

  onSubmit(): void {
    if (
      this.registrationForm.invalid ||
      this.isSubmitting ||
      this.isLoadingCurrencies ||
      this.currencies.length === 0
    ) {
      this.registrationForm.markAllAsTouched();
      return;
    }

    const formValue = this.registrationForm.getRawValue();

    if (formValue.primaryCurrencyId === null) {
      this.errorMessage = 'Selecciona una moneda principal.';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    const request: CompleteRegistrationRequest = {
      username: formValue.username.trim(),
      primaryCurrencyId: formValue.primaryCurrencyId,
      ...(this.registrationSource === 'GOOGLE'
        ? {
          firstName: this.firstName,
          lastName: this.lastName
        }
        : {})
    };

    this.authService.completeRegistration(request).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.successMessage =
          'Tu perfil se completó correctamente. Ya puedes acceder a Planora.';
        void this.router.navigate(['/dashboard']);
      },
      error: (error: HttpErrorResponse) => {
        this.isSubmitting = false;

        if (error.status === 409) {
          this.errorMessage =
            'Ese nombre de usuario ya está en uso. Elige otro.';
        } else if (error.status === 400) {
          this.errorMessage =
            'Los datos enviados no son válidos. Revisa tu información.';
        } else if (error.status === 0) {
          this.errorMessage =
            'No se pudo conectar con el servidor.';
        } else {
          this.errorMessage =
            'No se pudo completar tu perfil. Inténtalo nuevamente.';
        }
      }
    });
  }

  onBack(): void {
    if (this.registrationSource !=='GOOGLE') {
      return;
    }

    const formValue = this.registrationForm.getRawValue();

    void this.router.navigate(['/auth/google/register'], {
      state: {
        email: this.email,
        firstName: this.firstName,
        lastName: this.lastName,
        username: formValue.username,
        primaryCurrencyId: formValue.primaryCurrencyId,
      },
    });
  }

}
