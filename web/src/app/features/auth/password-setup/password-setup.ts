import { Component, inject, OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIcon } from '@ng-icons/core';
import { passwordMatchValidator, strongPasswordValidator } from '../../../shared/passwordValidator';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DriverPasswordSetupRequest } from '../../../shared/model/driver-registration';

@Component({
  selector: 'app-password-setup',
  imports: [
    ReactiveFormsModule,
    NgIcon
  ],
  templateUrl: './password-setup.html',
  styleUrl: './password-setup.css',
})
export class PasswordSetup implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);

  private token: string | null = null;

  hidePassword: boolean = true;
  hideConfirmedPassword: boolean = true;

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get("token");
  }

  passwordForm = new FormGroup({
    password: new FormControl('', [Validators.required, strongPasswordValidator]),
    confirmedPassword: new FormControl('', [Validators.required])
  }, { validators: passwordMatchValidator })

  onSubmit() {
    if (this.passwordForm.invalid) {
      if (this.passwordForm.errors?.['passwordMismatch']) {
        alert('Passwords do not match!');
      } else if (this.passwordForm.get('password')?.errors?.['weakPassword']) {
        alert('Password is too weak!');
      } else {
        alert('Please fill in all fields correctly!');
      }
      return;
    }
    
    const request: DriverPasswordSetupRequest = {
      token: this.token!,
      password: this.passwordForm.get('password')?.value!
    }

    this.authService.driverPasswordSetup(request).subscribe({
      next: (res) => {
        alert("Success! Your account is now active.");
        this.router.navigate(['/login']);
      },
      error: (err) => {
        alert(err.error || "An error occurred during activation.");
      }
    });
  }
}
