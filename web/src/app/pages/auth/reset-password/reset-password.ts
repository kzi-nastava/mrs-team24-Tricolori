import { Component, OnInit, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { passwordMatchValidator, strongPasswordValidator } from '../../../shared/password-validator';
import { NgIcon } from '@ng-icons/core';
import { AuthService } from '../../../services/auth.service';
import { ResetPasswordRequest } from '../../../model/auth.model';
import {ToastService} from '../../../services/toast.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgIcon
  ],
  templateUrl: './reset-password.html'
})
export class ResetPassword implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);

  hideNewPassword = true;
  hideConfirmedPassword = true;
  token: string | null = null;

  resetPasswordForm = new FormGroup({
    newPassword: new FormControl('', [Validators.required, strongPasswordValidator]),
    confirmedPassword: new FormControl('', [Validators.required])
  }, { validators: passwordMatchValidator });

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token');

    if (!this.token) {
      this.toastService.show('Invalid or missing reset token. Please request a new link.', 'error');
    }
  }

  onSubmit() {
    if (this.resetPasswordForm.invalid) {
      this.toastService.show(this.getErrorMessage(), 'warning');
      this.resetPasswordForm.markAllAsTouched();
      return;
    }

    if (!this.token) {
      this.toastService.show('No token found. Cannot reset password.', 'error');
      return;
    }

    const request: ResetPasswordRequest = {
      token: this.token,
      password: this.resetPasswordForm.value.newPassword!
    };

    this.authService.resetPassword(request).subscribe({
      next: () => {
        this.toastService.show('Your password has been successfully reset.', 'success');
        setTimeout(() => this.router.navigate(['/login']), 1000);
      },
      error: (err) => {
        this.toastService.show(
          err.status === 400
            ? 'The link has expired or is invalid. Please request a new one.'
            : 'Something went wrong. Please try again later.', 'error'
        );
      }
    });
  }

  getErrorMessage(): string {
    const password = this.resetPasswordForm.get('newPassword');
    const confirm = this.resetPasswordForm.get('confirmedPassword');

    if (password?.hasError('required')) {
      return 'New password is required!';
    }

    if (password?.hasError('weakPassword')) {
      return 'Password must be at least 8 characters long and include uppercase, lowercase, and a number.';
    }

    if (confirm?.hasError('required')) {
      return 'Please confirm your new password!';
    }

    if (this.resetPasswordForm.hasError('passwordMismatch')) {
      return 'Passwords do not match!';
    }

    return 'Please fill in the form correctly.';
  }
}
