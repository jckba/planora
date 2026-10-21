import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login';
import { GoogleCallbackComponent} from './features/auth/google-callback/google-callback';
import { GoogleRegistrationComponent } from './features/auth/google-registration/google-registration';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent
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
