import {Component, inject, OnInit} from '@angular/core';
import {AuthService} from '../../../core/services/auth.services';
import {Currency} from '../currency.model';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Location} from '@angular/common';
import {CompleteRegistrationRequest} from '../auth.models';
import {HttpErrorResponse} from '@angular/common/http';


interface CompleteRegistrationState {
  registrationSource?: 'GOOGLE' | 'LOCAL';
  firstName?: string | null;
  lastName?: string | null;
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

  registrationSource: 'GOOGLE' | 'LOCAL' = 'LOCAL';

  firstName = '';
  lastName = '';

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
        Validators.pattern(/^[a-zA-Z0-9._-]{3,20}$/)
      ]
    }),

    primaryCurrencyId: new FormControl<number | null>(null, Validators.required)
  });

  ngOnInit(): void {
    const state = this.location.getState() as CompleteRegistrationState;

    this.registrationSource = state.registrationSource ?? 'LOCAL';

    this.firstName = state.firstName?.trim() ?? '';
    this.lastName = state.lastName?.trim() ?? '';

    if (
      this.registrationSource === 'GOOGLE' &&
      (!this.firstName || !this.lastName)
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
}
