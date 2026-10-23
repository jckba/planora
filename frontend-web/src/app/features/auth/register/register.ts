import {Component, inject} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from "../auth.services";
import {Router} from '@angular/router';
import {HttpErrorResponse} from '@angular/common/http';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class RegisterComponent {

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly registerForm = new FormGroup({
    firstName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    lastName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  isSubmitting = false;
  errorMessage = '';
  successMessage = '';

  onSubmit(): void {
    if (this.registerForm.invalid || this.isSubmitting) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.register(this.registerForm.getRawValue())
      .subscribe({
        next: () => {
          this.isSubmitting = false;
          this.successMessage = 'Cuenta creada correctamente, redirigiendo a login...'
          this.registerForm.reset();
          setTimeout(() => {
            this.router.navigate(['/auth/login']);
          }, 1000);
        },
        error: (error: HttpErrorResponse) => {
          this.isSubmitting = false;

          if (error.status === 0) {
            this.errorMessage = 'No se pudo conectar con el servidor.';
          } else if (error.status === 409 || error.status === 400) {
            this.errorMessage = "El correo electronico ya esta registrado o son invalidos."
          }
          else {
            this.errorMessage = 'No se pudo registrar la cuenta. Intentelo nuevamente.'
          }
        },
      });
  }
}
