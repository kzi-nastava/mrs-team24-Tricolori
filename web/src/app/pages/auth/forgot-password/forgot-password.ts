import {Component, inject} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {NgIcon} from '@ng-icons/core';
import {AuthService} from '../../../services/auth.service';
import {ForgotPasswordRequest} from '../../../model/auth.model';
import {ToastService} from '../../../services/toast.service';

@Component({
  selector: 'app-forgot-password',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    NgIcon
  ],
  templateUrl: './forgot-password.html'
})
export class ForgotPassword {

  private authService = inject(AuthService);
  private toastService = inject(ToastService);
  private router = inject(Router);

  forgotPasswordForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email])
  });

  onSubmit() {
    if (this.forgotPasswordForm.invalid) {
      this.toastService.show('Please enter a valid email address.', 'warning');
      this.forgotPasswordForm.markAllAsTouched();
      return;
    }

    const request : ForgotPasswordRequest = { email: this.forgotPasswordForm.value.email! };
    this.authService.forgotPassword(request).subscribe({
      next: () => {
        this.toastService.show('Reset link sent! Please check your inbox.', 'success');
        this.forgotPasswordForm.reset();
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (err) => {
        const errorMsg = err.error?.message || 'Could not send reset link. Try again later.';
        this.toastService.show(errorMsg, 'error');
      }
    })
  }
}
