import {Component, inject} from '@angular/core';
import {ReactiveFormsModule, FormControl, FormGroup, Validators} from '@angular/forms';
import {HttpErrorResponse} from '@angular/common/http';
import {AuthService} from '../auth.services';
import {switchMap} from 'rxjs';

import {API_CONFIG} from '../../../core/config/api.config';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginComponent {

  private readonly authService = inject(AuthService);

  readonly loginForm = new FormGroup({
    identifier: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
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
    if (this.loginForm.invalid || this.isSubmitting) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.login(this.loginForm.getRawValue())
      .pipe(
        switchMap(() => this.authService.getCurrentUser()),
      )
      .subscribe({
        next: (user) => {
          this.isSubmitting = false;
          this.successMessage = `Sesión autenticada. ID de usuario: ${user.userId}`;
          this.loginForm.controls.password.reset('');
        },
        error: (error: HttpErrorResponse) => {
          this.isSubmitting = false;

          if (error.status === 401) {
            this.errorMessage = 'Credenciales incorrectas.';
          } else if (error.status === 0) {
            this.errorMessage = 'No se pudo conectar con el servidor.';
          } else {
            this.errorMessage = 'No se pudo iniciar sesión. Inténtelo nuevamente.';
          }
        },
      });
  }

  loginWithGoogle(): void {
    const backendUrl = API_CONFIG.baseUrl.replace(/\/api\/?$/, '');

    window.location.href =
      `${backendUrl}/oauth2/authorization/google`;
  }

}
