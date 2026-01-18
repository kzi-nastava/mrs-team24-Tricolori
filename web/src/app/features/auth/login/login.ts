import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { strongPasswordValidator } from '../../../shared/passwordValidator';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { NgIcon } from '@ng-icons/core';

import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';


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
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  hidePassword: boolean = true;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(private authService: AuthService, private router: Router) {}

  loginForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, strongPasswordValidator])
  });

  onSubmit() {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.loginForm.valid) {
      const formData = this.loginForm.value;
      this.successMessage = 'Successfully logged in!';

      console.log('Logged in:', formData.email);
      
      this.authService.login(formData.email!, formData.password!).subscribe({
    next: user => {
      this.successMessage = 'Successfully logged in!';

      switch (user?.role) {
        case 'driver':
          this.router.navigate(['/driver']);
          break;

        case 'user': // passenger
          this.router.navigate(['/passenger']);
          break;

        case 'admin':
          this.router.navigate(['/admin']);
          break;

        default:
          this.router.navigate(['/']);
      }
    },
    error: err => {
      this.errorMessage = err?.message ?? 'Login failed';
    }
  });
      
    } else {
      this.errorMessage = this.getErrorMessage();
    }
  }

  private getErrorMessage(): string {
    const emailControl = this.loginForm.get('email');
    const passwordControl = this.loginForm.get('password');

    if (emailControl?.errors?.['required']) {
      return 'Email address is required!';
    }

    if (emailControl?.errors?.['email']) {
      return 'Please enter a valid email address!';
    }

    if (passwordControl?.errors?.['required']) {
      return 'Password is required!';
    }

    if (passwordControl?.errors?.['weakPassword']) {
      return 'Password must be at least 8 characters with uppercase, lowercase and number!';
    }

    return 'Please fill in all fields correctly!';
  }
}
