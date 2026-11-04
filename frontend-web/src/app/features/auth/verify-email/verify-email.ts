import {Component, inject} from '@angular/core';
import {FormControl, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../../core/services/auth.services';
import {Router} from '@angular/router';
import {HttpErrorResponse} from '@angular/common/http';
import {finalize} from 'rxjs';

@Component({
  selector: 'app-verify-email',
  imports: [ReactiveFormsModule, FormsModule],
  templateUrl: './verify-email.html',
  styleUrl: './verify-email.css',
})
export class VerifyEmailComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly email = this.authService.getPendingVerificationEmail();

  readonly codeControl = new FormControl('', {
    nonNullable: true,
    validators: [
      Validators.required,
      Validators.pattern(/^\d{6}$/),
    ],
  });


  isVerifying = false;
  isResending = false;
  errorMessage = '';
  successMessage = '';

  constructor() {
    if (!this.email) {
      void this.router.navigate(['/register']);
    }
  }

  confirmCode(): void {
    if (!this.email || this.codeControl.invalid || this.isVerifying) {
      this.codeControl.markAsTouched();
      return;
    }

    this.isVerifying = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.confirmEmailVerification(
      this.email,
      this.codeControl.value
    ).pipe(
      finalize(() => {
        this.isVerifying = false;
      })
    ).subscribe({
      next: () => {
        this.isVerifying = false;
        this.authService.clearPendingVerificationEmail();

        void this.router.navigate(['/complete-registration']);
      },
      error: (error: HttpErrorResponse) => {
        this.isVerifying = false;

        if (error.status === 0) {
          this.errorMessage = 'No se pudo conectar con el servidor.';
        } else if (error.status === 400 || error.status === 404) {
          this.errorMessage = 'El código es inválido o ha expirado. Verifica el código e inténtalo nuevamente.';
        } else if (error.status === 429) {
          this.errorMessage = 'Has realizado demasiados intentos. Espera antes de volver a intentarlo.';
        } else {
          this.errorMessage = 'No se pudo verificar el correo. Inténtalo nuevamente.';
        }
      },
    });
  }

  resendCode(): void {
    if (!this.email || this.isResending || this.isVerifying) {
      return;
    }

    this.isResending = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.resendEmailVerification(this.email).pipe(
      finalize(() => {
        this.isResending = false;
      })
    ).subscribe({
      next: () => {
        this.successMessage = 'Si la solicitud es valida, recibiras un codigo nuevamente en tu correo.';
      },
      error: (error: HttpErrorResponse) => {
        this.isResending = false;

        if (error.status === 0) {
          this.errorMessage = 'No se pudo conectar con el servidor.';
        } else if (error.status === 429) {
          this.errorMessage = 'Debes esperar antes de solicitar otro código.';
        } else {
          this.errorMessage = 'No se pudo procesar la solicitud. Inténtalo nuevamente.';
        }
      },
    });
  }

  backToRegister(): void {
    this.authService.clearPendingVerificationEmail();
    void this.router.navigate(['/register']);
  }

}
