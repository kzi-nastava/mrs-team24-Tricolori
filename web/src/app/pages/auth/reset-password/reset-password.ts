import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { passwordMatchValidator, strongPasswordValidator } from '../../../shared/password-validator';
import { NgIcon } from '@ng-icons/core';
import { AuthService } from '../../../services/auth.service';
import { ResetPasswordRequest } from '../../../model/auth.model';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgIcon
  ],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css',
})
export class ResetPassword implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);

  hideNewPassword = true;
  hideConfirmedPassword = true;
  token: string | null = null;

  errorMessage = signal<string>('');
  successMessage = signal<string>('');

  resetPasswordForm = new FormGroup({
    newPassword: new FormControl('', [Validators.required, strongPasswordValidator]),
    confirmedPassword: new FormControl('', [Validators.required])
  }, { validators: passwordMatchValidator });

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token');

    if (!this.token) {
      this.errorMessage.set('Invalid or missing reset token. Please request a new link.');
    }
  }

  onSubmit() {
    this.errorMessage.set('');
    this.successMessage.set('');

    if (this.resetPasswordForm.invalid) {
      this.resetPasswordForm.markAllAsTouched();
      return;
    }

    if (!this.token) {
      this.errorMessage.set('No token found. Cannot reset password.');
      return;
    }

    const request: ResetPasswordRequest = {
      token: this.token,
      password: this.resetPasswordForm.value.newPassword!
    };

    this.authService.resetPassword(request).subscribe({
      next: () => {
        this.successMessage.set('Your password has been successfully reset.');
        setTimeout(() => this.router.navigate(['/login']), 1000);
      },
      error: (err) => {
        this.errorMessage.set(
          err.status === 400
            ? 'The link has expired or is invalid. Please request a new one.'
            : 'Something went wrong. Please try again later.'
        );
      }
    });
  }
}
