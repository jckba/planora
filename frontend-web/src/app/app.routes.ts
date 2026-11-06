import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login';
import {RegisterComponent} from './features/auth/register/register';
import { GoogleCallbackComponent} from './features/auth/google-callback/google-callback';
import { GoogleRegistrationComponent } from './features/auth/google-registration/google-registration';
import {VerifyEmailComponent} from './features/auth/verify-email/verify-email';

import {authGuard} from './core/guards/auth.guard';
import {onboardingGuard} from './core/guards/onboarding.guard';
import {CompleteRegistrationComponent} from './features/auth/complete-registration/complete-registration';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'register',
    component: RegisterComponent
  },
  {
    path: 'verify-email',
    component: VerifyEmailComponent
  },
  {
    path: 'auth/google/callback',
    component: GoogleCallbackComponent
  },
  {
    path: 'auth/google/register',
    component: GoogleRegistrationComponent
  },
  {
    path: 'complete-registration',
    component: CompleteRegistrationComponent,
    canActivate: [onboardingGuard]
  },
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];
