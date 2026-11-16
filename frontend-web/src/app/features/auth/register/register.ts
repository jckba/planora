import {Component, inject, signal} from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import {AuthService} from "../../../core/services/auth.services";
import {Router, RouterLink} from '@angular/router';
import {HttpErrorResponse} from '@angular/common/http';
import {finalize} from 'rxjs';

const passwordMatchValidator: ValidatorFn = (
  control: AbstractControl,
): ValidationErrors | null => {
  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;

  if (!password || !confirmPassword) {
    return null;
  }

  return password == confirmPassword ? null
    : {passwordMismatch: true};

};

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class RegisterComponent {

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly registerForm = new FormGroup({
      firstName: new FormControl('', {
        nonNullable: true,
        validators: [
          Validators.required,
          Validators.pattern(/^(?!\s*$)[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ' -]+$/),
          Validators.maxLength(50),
        ],
      }),
      lastName: new FormControl('', {
        nonNullable: true,
        validators: [
          Validators.required,
          Validators.pattern(/^(?!\s*$)[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ' -]+$/),
          Validators.maxLength(50),
        ],
      }),
      email: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.email, Validators.maxLength(254)],
      }),
      password: new FormControl('', {
        nonNullable: true,
        validators: [
          Validators.required,
          Validators.minLength(8),
          Validators.maxLength(27),
        ],
      }),
      confirmPassword: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required],
      }),
    },
    {
      validators: passwordMatchValidator,
    }
  );

  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);

  onSubmit(): void {
    if (this.registerForm.invalid || this.isSubmitting()) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    const {
      firstName,
      lastName,
      email,
      password,
    } = this.registerForm.getRawValue();

    this.authService.register({
      firstName: firstName.trim(),
      lastName: lastName.trim(),
      email: email.trim().toLowerCase(),
      password,
    })
      .pipe(
        finalize(() => {
          this.isSubmitting.set(false);
        })
      )
      .subscribe({
        next: () => {
          const email = this.registerForm.controls.email.value.trim().toLowerCase();
          this.authService.setPendingVerificationEmail(email);

          void this.router.navigate(['/verify-email'], {
            state: {
              email: email.trim().toLowerCase(),
            },
          });
        },
        error: (error: HttpErrorResponse) => {
          this.isSubmitting.set(false);

          if (error.status === 0) {
            this.errorMessage.set('No se pudo conectar con el servidor. Comprueba tu conexion e intentalo nuevamente.');
          } else if (error.status === 409) {
            this.errorMessage.set('No se pudo completar el registro con el correo proporcionado.');
          } else if (error.status === 400) {
            this.errorMessage.set('Los datos enviados no son validos.');
          } else {
            this.errorMessage.set('No se pudo registrar la cuenta. Intentelo nuevamente.');
          }
        },
      });
  }

  get passwordMismatch(): boolean {
    return (
      this.registerForm.hasError('passwordMismatch') &&
      this.registerForm.controls.confirmPassword.touched
    )
  }
}
