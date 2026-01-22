import {ChangeDetectorRef, Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, RouterLink} from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { NgIcon } from '@ng-icons/core';

import { AuthService } from '../../../core/services/auth.service';
import { LoginRequest } from '../../../shared/model/auth.model';


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
export class Login implements OnInit {
  private route = inject(ActivatedRoute);

  hidePassword: boolean = true;
  errorMessage: string = '';
  successMessage: string = '';

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (params['activated']) {
        this.successMessage = "Account activated successfully! Please log in.";
        this.cdr.detectChanges();
      }
    })
  }

  constructor(private authService: AuthService, private cdr: ChangeDetectorRef) {}

  loginForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required])
  });

  onSubmit() {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.loginForm.valid) {

      const loginRequest: LoginRequest = {
        email: this.loginForm.value.email!,
        password: this.loginForm.value.password!
      };

      this.authService.login(loginRequest).subscribe({
        next: () => {
          this.successMessage = 'Success! Redirecting...';
        },
        error: () => {
          this.errorMessage = 'Invalid email or password';
          this.cdr.detectChanges();
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

    if (passwordControl?.errors?.['required']) {
      return 'Password is required!';
    }

    return 'Please fill in all fields correctly!';
  }
}
