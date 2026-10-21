import {Component, inject, OnInit} from '@angular/core';
import {AuthService} from '../auth.services';
import {ActivatedRoute, Router} from '@angular/router';
import {HttpErrorResponse} from '@angular/common/http';

@Component({
  selector: 'app-google-callback',
  imports: [],
  templateUrl: './google-callback.html',
  styleUrl: './google-callback.css',
})
export class GoogleCallbackComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  message = 'Verificando la autenticación con Google...';
  errorMessage = '';
  isLoading = true;

  ngOnInit(): void {
    const oauthError = this.route.snapshot.queryParamMap.get('error');

    if (oauthError) {
      this.isLoading = false;
      this.errorMessage = this.getOAuthErrorMessage(oauthError);
      return;
    }

    this.authService.startGoogleSession().subscribe({
      next: (response) => {
        switch (response.status) {
          case 'AUTHENTICATED':
            if (!response.login) {
              this.showError(`Google autenticó la cuenta, pero el servidor no devolvió el token`);
              return;
            }

            this.authService.getCurrentUser().subscribe({
                next: (user) => {
                  this.isLoading = false;
                  this.message = `Session iniciada correctamente. Usuario: ${user.userId}`;
                },
                error: () => {
                  this.authService.clearSession();
                  this.showError('No se pudo validar la sesion. Intenta iniciar sesion de nuevo.');
                },
              }
            );
            return;

          case 'REGISTRATION_REQUIRED':
            this.isLoading = false;
            void this.router.navigate(
              ['/auth/google/register'],
              {
                state: {
                  email: response.email,
                  firstName: response.firstName,
                  lastName: response.lastName,
                },
              },
            );
            return;

          case 'LINK_REQUIRED':
            this.isLoading = false;
            void this.router.navigate(
              ['/login'],
              {
                queryParams: {linkGoogle: 'true'},
                state: {
                  googleEmail: response.email,
                }
              },
            );
            return;
        }
      },
      error: (error: HttpErrorResponse) => {
        this.showError(
          error.status === 0
            ? 'No se pudo conectar con el servidor. Comprueba que el servidor este activo.'
            : 'No se pudo completar la autenticacion con Google. Intentelo de nuevo.',
        );
      },
    });
  }

  private getOAuthErrorMessage(error: string): string {
    switch (error) {
      case 'email_not_verified':
        return 'Google no devolvio un correo verificado. Usa otra cuenta de Google.';
      case 'google_auth_failed':
        return 'No se pudo completar la autenticacion con Google. Intentelo de nuevo.';
    }
    return 'La autenticacion con Google no pudo completarse.';
  }

  private showError(message: string): void {
    this.isLoading = false;
    this.errorMessage = message;
  }

  goToLogin(): void {
    void this.router.navigate(['/login']);
  }

}
