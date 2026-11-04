import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login';
import {RegisterComponent} from './features/auth/register/register';
import { GoogleCallbackComponent} from './features/auth/google-callback/google-callback';
import { GoogleRegistrationComponent } from './features/auth/google-registration/google-registration';
import {VerifyEmailComponent} from './features/auth/verify-email/verify-email';

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
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  }
];
