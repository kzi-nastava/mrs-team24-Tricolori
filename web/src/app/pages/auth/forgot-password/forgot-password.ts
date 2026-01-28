import {Component, inject} from '@angular/core';
import {RouterLink} from '@angular/router';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {NgIcon} from '@ng-icons/core';
import {AuthService} from '../../../services/auth.service';
import {ForgotPasswordRequest} from '../../../model/auth.model';

@Component({
  selector: 'app-forgot-password',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    NgIcon
  ],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css',
})
export class ForgotPassword {
  authService = inject(AuthService);

  errorMessage: string = '';
  successMessage: string = '';

  constructor() {}

  forgotPasswordForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email])
  });

  onSubmit() {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.forgotPasswordForm.invalid) {
      this.errorMessage = 'Please enter a valid email address.';
      this.forgotPasswordForm.markAllAsTouched();
      return;
    }

    const request : ForgotPasswordRequest = { email: this.forgotPasswordForm.value.email! };
    this.authService.forgotPassword(request).subscribe({
      next: () => {
        this.successMessage = 'Reset link sent successfully';
        this.forgotPasswordForm.reset();
      },
      error: (err) => {
        this.errorMessage = 'Could not send reset link. Please try again later.';
        console.error('Email error:', err);
      }
    })
  }
}
