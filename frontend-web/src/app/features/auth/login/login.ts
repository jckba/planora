import {Component, inject, signal} from '@angular/core';
import {ReactiveFormsModule, FormControl, FormGroup, Validators} from '@angular/forms';
import {HttpErrorResponse} from '@angular/common/http';
import {AuthService} from '../../../core/services/auth.services';
import {finalize, switchMap} from 'rxjs';

import {API_CONFIG} from '../../../core/config/api.config';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
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

  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  onSubmit(): void {
    if (this.loginForm.invalid || this.isSubmitting()) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const credentials = this.loginForm.getRawValue();

    this.isSubmitting.set(true);
    this.loginForm.controls.identifier.disable();
    this.loginForm.controls.password.disable();
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.authService.login(credentials)
      .pipe(
        switchMap(() => this.authService.getCurrentUser()),
        finalize(() => {
          this.isSubmitting.set(false);
          this.loginForm.controls.identifier.enable();
          this.loginForm.controls.password.enable();
        }),
      )
      .subscribe({
        next: (user) => {
          this.successMessage.set(`Sesión autenticada. ID de usuario: ${user.userId}`);
          this.loginForm.controls.password.reset('');
        },
        error: (error: HttpErrorResponse) => {
          this.isSubmitting.set(false);

          if (error.status === 401) {
            this.errorMessage.set('Credenciales incorrectas.');
          } else if (error.status === 0) {
            this.errorMessage.set('No se pudo conectar con el servidor.');
          } else {
            this.errorMessage.set('No se pudo iniciar sesión. Inténtelo nuevamente.');
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
