import { Component, inject} from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { NgIcon } from '@ng-icons/core';

import { AuthService } from '../../../services/auth.service';
import { LoginRequest } from '../../../model/auth.model';
import {ToastService} from '../../../services/toast.service';


@Component({
  selector: 'app-login',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatCardModule,
    NgIcon
  ],
  templateUrl: './login.html'
})
export class Login {

  hidePassword: boolean = true;

  private authService = inject(AuthService);
  private toastService = inject(ToastService);

  loginForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required])
  });

  onSubmit() {
    if (this.loginForm.invalid) {
      this.toastService.show(this.getErrorMessage(), 'warning');
      return;
    }

    const loginRequest: LoginRequest = this.loginForm.getRawValue() as LoginRequest;

    this.authService.login(loginRequest).subscribe({
      error: () => {
        this.toastService.show('Invalid email or password', 'error');
      }
    });
  }

  private getErrorMessage(): string {
    const { email, password } = this.loginForm.controls;

    if (email.errors?.['required']) return 'Email address is required!';
    if (email.errors?.['email']) return 'Please enter a valid email!';
    if (password.errors?.['required']) return 'Password is required!';

    return 'Please check your credentials.';
  }
}
