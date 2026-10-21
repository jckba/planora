import {Component, inject, OnInit} from '@angular/core';
import { Location } from '@angular/common';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../auth.services';
import {Currency} from '../currency.model';
import {HttpErrorResponse} from '@angular/common/http';


interface GoogleRegistrationState {
  email?: string | null;
  firstName?: string | null;
  lastName?: string | null;
}

@Component({
  selector: 'app-google-registration',
  imports: [ReactiveFormsModule],
  templateUrl: './google-registration.html',
  styleUrl: './google-registration.css',
})
export class GoogleRegistrationComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly location = inject(Location);

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
        Validators.pattern(/^[a-zA-Z0-9._-]{3,40}$/),
      ],
    }),
    firstName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(100)],
    }),
    lastName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(100)],
    }),
    primaryCurrencyId: new FormControl<number | null>(
      null,
      Validators.required,
    ),
  });

  ngOnInit(): void {
    const state = this.location.getState() as GoogleRegistrationState;

    if (!state.email) {
      this.errorMessage = 'No se encontraron los datos de Google. Inicia el proceso de nuevo.';
      this.isLoadingCurrencies = false;
      return;
    }

    this.email = state.email;

    this.registrationForm.patchValue({
      firstName: state.firstName ?? '',
      lastName: state.lastName ?? '',
    });

    this.authService.getCurrencies().subscribe({
      next: (currencies) => {
        this.currencies = currencies;
        this.isLoadingCurrencies = false;
      },
      error: () => {
        this.isLoadingCurrencies = false;
        this.errorMessage = 'No se pudieron obtener las monedas. Inicia el proceso de nuevo.';
      },
    });
  }

  onSubmit(): void {
    if (
      this.registrationForm.invalid ||
      this.isSubmitting ||
      this.isLoadingCurrencies
    ) {
      this.registrationForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    const request = this.registrationForm.getRawValue();

    if (request.primaryCurrencyId === null) {
      this.isSubmitting = false;
      this.errorMessage = 'Selecciona una moneda principal.';
      return;
    }

    this.authService.completeGoogleRegistration({
      username: request.username.trim(),
      firstName: request.firstName.trim(),
      lastName: request.lastName.trim(),
      primaryCurrencyId: request.primaryCurrencyId,
    }).subscribe({
      next: () => {
        this.authService.getCurrentUser().subscribe({
          next: (user) => {
            this.isSubmitting = false;
            this.successMessage =
              `Cuenta creada correctamente. ID de usuario: ${user.userId}`;
          },
          error: () => {
            this.isSubmitting = false;
            this.authService.clearSession();
            this.errorMessage =
              'La cuenta se creó, pero no se pudo validar la sesión. Intenta iniciar sesión.';
          },
        });
      },
      error: (error: HttpErrorResponse) => {
        this.isSubmitting = false;

        if (error.status === 409) {
          this.errorMessage =
            'Ese nombre de usuario ya está en uso. Elige otro.';
        } else if (error.status === 400) {
          this.errorMessage =
            'Los datos no son válidos. Revisa el nombre de usuario y la moneda.';
        } else if (error.status === 0) {
          this.errorMessage =
            'No se pudo conectar con el servidor.';
        } else {
          this.errorMessage =
            'No se pudo completar el registro. Inténtalo de nuevo.';
        }
      },
    });
  }
}
