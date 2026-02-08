import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

import { NgIcon, provideIcons } from '@ng-icons/core';
import {
  heroUser,
  heroPhone,
  heroHome,
  heroEnvelope,
  heroLockClosed,
  heroEye,
  heroEyeSlash,
  heroArrowUpTray,
  heroArrowRight
} from '@ng-icons/heroicons/outline';

import { AuthService } from '../../../services/auth.service';
import { ToastService } from '../../../services/toast.service';
import { passwordMatchValidator, strongPasswordValidator } from '../../../shared/password-validator';
import { RegisterRequest } from '../../../model/auth.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    NgIcon,
  ],
  providers: [
    provideIcons({
      heroUser,
      heroPhone,
      heroHome,
      heroEnvelope,
      heroLockClosed,
      heroEye,
      heroEyeSlash,
      heroArrowUpTray,
      heroArrowRight
    })
  ],
  templateUrl: './register.html'
})
export class Register {
  private authService = inject(AuthService);
  private router = inject(Router);
  private toastService = inject(ToastService);

  hidePassword = true;
  hideRepeatPassword = true;
  selectedFileName: string | null = null;
  imgPreview: string | ArrayBuffer | null = null;
  selectedFile: File | null = null;

  isLoading = signal(false);

  registerForm = new FormGroup({
    firstName: new FormControl('', Validators.required),
    lastName: new FormControl('', Validators.required),
    phone: new FormControl('', [
      Validators.required,
      Validators.pattern(/^\+?[0-9]{10,15}$/)
    ]),
    address: new FormControl('', Validators.required),
    email: new FormControl('', [Validators.required, Validators.email]),
    newPassword: new FormControl('', [Validators.required, strongPasswordValidator]),
    confirmedPassword: new FormControl('', Validators.required)
  }, { validators: passwordMatchValidator });

  onSubmit() {
    if (this.registerForm.invalid) {
      this.toastService.show(this.getErrorMessage(), 'warning');
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    const formValue = this.registerForm.getRawValue();

    const registerData: RegisterRequest = {
      email: formValue.email!,
      password: formValue.newPassword!,
      firstName: formValue.firstName!,
      lastName: formValue.lastName!,
      homeAddress: formValue.address!,
      phoneNum: formValue.phone!
    };

    this.authService.registerPassenger(registerData, this.selectedFile || undefined)
      .subscribe({
        next: (message) => {
          this.isLoading.set(false);
          this.toastService.show(message || 'Registration successful! Verification email sent.', 'success');

          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2000);
        },
        error: (error) => {
          this.isLoading.set(false);
          const errorMsg = typeof error.error === 'string' ? error.error : 'Registration failed. Please try again.';
          this.toastService.show(errorMsg, 'error');
        }
      });
  }

  getErrorMessage(): string {
    const controls = this.registerForm.controls;

    if (controls.firstName.errors?.['required'] || controls.lastName.errors?.['required']) {
      return 'First and last name are required!';
    }
    if (controls.email.errors?.['required']) return 'Email is required!';
    if (controls.email.errors?.['email']) return 'Email format is invalid!';
    if (controls.phone.errors?.['required']) return 'Phone number is required!';
    if (controls.phone.errors?.['pattern']) return 'Phone format: 10-15 digits!';
    if (controls.address.errors?.['required']) return 'Home address is required!';
    if (controls.newPassword.errors?.['required']) return 'Password is required!';
    if (controls.newPassword.hasError('weakPassword')) return 'Password is too weak!';
    if (controls.confirmedPassword.errors?.['required']) return 'Please repeat password!';
    if (this.registerForm.hasError('passwordMismatch')) return 'Passwords do not match!';

    console.log('Form errors:', this.registerForm.errors);
    console.log('Password errors:', controls.newPassword.errors);

    return 'Form is invalid. Please check all fields.';
  }

  onFileSelected(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      if (!file.type.startsWith('image/')) {
        this.toastService.show('Please select an image file', 'warning');
        return;
      }

      if (file.size > 5 * 1024 * 1024) {
        this.toastService.show('File size must be less than 5MB', 'warning');
        return;
      }

      this.selectedFile = file;
      this.selectedFileName = file.name;
      const reader = new FileReader();
      reader.onload = () => this.imgPreview = reader.result;
      reader.readAsDataURL(file);
    }
  }
}
