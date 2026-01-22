import {ChangeDetectorRef, Component, inject, signal} from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { passwordMatchValidator, strongPasswordValidator } from '../../../shared/passwordValidator';
import { Router, RouterLink } from '@angular/router';
import { NgIcon } from '@ng-icons/core';
import { AuthService } from '../../../core/services/auth.service';
import { CommonModule } from '@angular/common';
import {RegisterRequest} from '../../../shared/model/auth.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterLink,
    NgIcon,
  ],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  private authService = inject(AuthService);
  private router = inject(Router);

  registerForm: FormGroup;
  hidePassword = true;
  hideRepeatPassword = true;
  selectedFileName: string | null = null;
  imgPreview: string | ArrayBuffer | null = null;
  selectedFile: File | null = null;

  // Loading & Error states
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  constructor() {
    this.registerForm = new FormGroup({
      firstName: new FormControl('', Validators.required),
      lastName: new FormControl('', Validators.required),
      phone: new FormControl('', [
        Validators.required,
        Validators.pattern(/^\+?[0-9]{10,15}$/)
      ]),
      address: new FormControl('', Validators.required),
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required, strongPasswordValidator]),
      repeatPassword: new FormControl('', Validators.required)
    }, { validators: passwordMatchValidator });
  }

  onSubmit() {
    if (this.registerForm.invalid) {
      // Mark all as touched to show errors
      Object.keys(this.registerForm.controls).forEach(key => {
        this.registerForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const formValue = this.registerForm.value;

    // Map form fields to backend expected fields
    const registerData : RegisterRequest = {
      email: formValue.email,
      password: formValue.password,
      firstName: formValue.firstName,
      lastName: formValue.lastName,
      homeAddress: formValue.address,
      phoneNum: formValue.phone
    }

    this.authService.registerPassenger(registerData, this.selectedFile || undefined)
      .subscribe({
        next: (message) => {
          this.isLoading.set(false);
          this.successMessage.set(message);

          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 1000);
        },
        error: (error) => {
          this.isLoading.set(false);
          console.error('Registration error:', error);
          this.errorMessage.set(
            error.error || 'Registration failed. Please try again.'
          );
        }
      });
  }

  onFileSelected(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      if (!file.type.startsWith('image/')) {
        this.errorMessage.set('Please select an image file');
        return;
      }

      if (file.size > 5 * 1024 * 1024) {
        this.errorMessage.set('File size must be less than 5MB');
        return;
      }

      this.selectedFile = file;
      this.selectedFileName = file.name;


      // Create preview
      const reader = new FileReader();
      reader.onload = () => this.imgPreview = reader.result;
      reader.readAsDataURL(file);
    }
  }
}
