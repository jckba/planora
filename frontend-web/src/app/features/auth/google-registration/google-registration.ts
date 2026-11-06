import {Component, inject, OnInit} from '@angular/core';
import {Location} from '@angular/common';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../../core/services/auth.services';
import {Currency} from '../currency.model';
import {HttpErrorResponse} from '@angular/common/http';
import {Router} from '@angular/router';
import {last} from 'rxjs';


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
  private readonly router = inject(Router);

  email = '';
  errorMessage = '';

  readonly registrationForm = new FormGroup({
    firstName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(100)],
    }),
    lastName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(100)],
    }),
  });

  ngOnInit(): void {
    const state = this.location.getState() as GoogleRegistrationState;

    if (!state.email) {
      this.errorMessage = 'No se encontraron los datos de Google. Inicia el proceso de nuevo.';
      return;
    }

    this.email = state.email;

    this.registrationForm.patchValue({
      firstName: state.firstName ?? '',
      lastName: state.lastName ?? '',
    });
  }

  onContinue(): void {
    if (!this.email || this.registrationForm.invalid) {
      this.registrationForm.markAllAsTouched();
      return;
    }

    const {firstName, lastName} = this.registrationForm.getRawValue();

    void this.router.navigate(['/complete-registration'], {
      state: {
        email: this.email,
        firstName: firstName.trim(),
        lastName: lastName.trim(),
      },
    });
  }
}
