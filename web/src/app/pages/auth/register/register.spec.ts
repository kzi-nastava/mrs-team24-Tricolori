import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { provideLocationMocks } from '@angular/common/testing';

import { Register } from './register';
import { AuthService } from '../../../services/auth.service';
import { ToastService } from '../../../services/toast.service';
import { provideIcons } from '@ng-icons/core';
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
import { Router } from '@angular/router';

describe('Register Component', () => {
  let component: Register;
  let fixture: ComponentFixture<Register>;
  let authService: jasmine.SpyObj<AuthService>;
  let toastService: jasmine.SpyObj<ToastService>;
  let router: Router;

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['registerPassenger']);
    const toastServiceSpy = jasmine.createSpyObj('ToastService', ['show']);

    await TestBed.configureTestingModule({
      imports: [Register, ReactiveFormsModule],
      providers: [
        provideRouter([]),
        provideLocationMocks(),
        { provide: AuthService, useValue: authServiceSpy },
        { provide: ToastService, useValue: toastServiceSpy },
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
      ]
    }).compileComponents();

    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    toastService = TestBed.inject(ToastService) as jasmine.SpyObj<ToastService>;
    router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    fixture = TestBed.createComponent(Register);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('Form Initialization', () => {
    it('should initialize form with empty values', () => {
      expect(component.registerForm.get('firstName')?.value).toBe('');
      expect(component.registerForm.get('lastName')?.value).toBe('');
      expect(component.registerForm.get('phone')?.value).toBe('');
      expect(component.registerForm.get('address')?.value).toBe('');
      expect(component.registerForm.get('email')?.value).toBe('');
      expect(component.registerForm.get('newPassword')?.value).toBe('');
      expect(component.registerForm.get('confirmedPassword')?.value).toBe('');
    });

    it('should have invalid form when empty', () => {
      expect(component.registerForm.valid).toBeFalsy();
    });
  });

  describe('Form Validation', () => {
    it('should require firstName', () => {
      const firstName = component.registerForm.get('firstName');
      expect(firstName?.valid).toBeFalsy();
      expect(firstName?.hasError('required')).toBeTruthy();

      firstName?.setValue('John');
      expect(firstName?.hasError('required')).toBeFalsy();
    });

    it('should require lastName', () => {
      const lastName = component.registerForm.get('lastName');
      expect(lastName?.valid).toBeFalsy();
      expect(lastName?.hasError('required')).toBeTruthy();

      lastName?.setValue('Doe');
      expect(lastName?.hasError('required')).toBeFalsy();
    });

    it('should require valid email format', () => {
      const email = component.registerForm.get('email');

      email?.setValue('');
      expect(email?.hasError('required')).toBeTruthy();

      email?.setValue('invalid-email');
      expect(email?.hasError('email')).toBeTruthy();

      email?.setValue('valid@email.com');
      expect(email?.valid).toBeTruthy();
    });

    it('should require phone number with correct pattern', () => {
      const phone = component.registerForm.get('phone');

      phone?.setValue('');
      expect(phone?.hasError('required')).toBeTruthy();

      phone?.setValue('123');
      expect(phone?.hasError('pattern')).toBeTruthy();

      phone?.setValue('1234567890');
      expect(phone?.valid).toBeTruthy();

      phone?.setValue('+381641234567');
      expect(phone?.valid).toBeTruthy();
    });

    it('should require address', () => {
      const address = component.registerForm.get('address');
      expect(address?.valid).toBeFalsy();
      expect(address?.hasError('required')).toBeTruthy();

      address?.setValue('123 Main St');
      expect(address?.hasError('required')).toBeFalsy();
    });

    it('should require strong password', () => {
      const password = component.registerForm.get('newPassword');

      password?.setValue('');
      expect(password?.hasError('required')).toBeTruthy();

      password?.setValue('weak');
      expect(password?.hasError('weakPassword')).toBeTruthy();

      password?.setValue('StrongPass123!');
      expect(password?.valid).toBeTruthy();
    });

    it('should require password confirmation', () => {
      const confirmedPassword = component.registerForm.get('confirmedPassword');
      expect(confirmedPassword?.valid).toBeFalsy();
      expect(confirmedPassword?.hasError('required')).toBeTruthy();

      confirmedPassword?.setValue('password');
      expect(confirmedPassword?.hasError('required')).toBeFalsy();
    });

    it('should validate password match', () => {
      component.registerForm.patchValue({
        newPassword: 'StrongPass123!',
        confirmedPassword: 'DifferentPass123!'
      });

      expect(component.registerForm.hasError('passwordMismatch')).toBeTruthy();

      component.registerForm.patchValue({
        confirmedPassword: 'StrongPass123!'
      });

      expect(component.registerForm.hasError('passwordMismatch')).toBeFalsy();
    });
  });

  describe('Form Submission', () => {
    beforeEach(() => {
      component.registerForm.patchValue({
        firstName: 'John',
        lastName: 'Doe',
        email: 'john.doe@example.com',
        phone: '1234567890',
        address: '123 Main Street',
        newPassword: 'StrongPass123!',
        confirmedPassword: 'StrongPass123!'
      });
    });

    it('should not submit when form is invalid', () => {
      component.registerForm.patchValue({ email: '' });

      component.onSubmit();

      expect(toastService.show).toHaveBeenCalledWith(
        jasmine.any(String),
        'warning'
      );
      expect(authService.registerPassenger).not.toHaveBeenCalled();
    });

    it('should send correct data on successful registration', () => {
      authService.registerPassenger.and.returnValue(
        of('Registration successful! Verification email sent.')
      );

      component.onSubmit();

      expect(authService.registerPassenger).toHaveBeenCalledWith(
        {
          email: 'john.doe@example.com',
          password: 'StrongPass123!',
          firstName: 'John',
          lastName: 'Doe',
          homeAddress: '123 Main Street',
          phoneNum: '1234567890'
        },
        undefined
      );
    });

    it('should show success message and navigate to login on successful registration', (done) => {
      const successMessage = 'Registration successful! Verification email sent.';
      authService.registerPassenger.and.returnValue(of(successMessage));

      component.onSubmit();

      expect(component.isLoading()).toBeFalsy();
      expect(toastService.show).toHaveBeenCalledWith(successMessage, 'success');

      setTimeout(() => {
        expect(router.navigate).toHaveBeenCalledWith(['/login']);
        done();
      }, 2100);
    });

    it('should set loading state during registration', () => {
      authService.registerPassenger.and.returnValue(
        of('Registration successful!')
      );

      expect(component.isLoading()).toBeFalsy();

      component.onSubmit();

      expect(component.isLoading()).toBeFalsy();
    });

    it('should handle registration error', () => {
      const errorResponse = { error: 'Email already exists' };
      authService.registerPassenger.and.returnValue(
        throwError(() => errorResponse)
      );

      component.onSubmit();

      expect(component.isLoading()).toBeFalsy();
      expect(toastService.show).toHaveBeenCalledWith(
        'Email already exists',
        'error'
      );
      expect(router.navigate).not.toHaveBeenCalled();
    });

    it('should handle generic registration error', () => {
      const errorResponse = { error: { message: 'Server error' } };
      authService.registerPassenger.and.returnValue(
        throwError(() => errorResponse)
      );

      component.onSubmit();

      expect(toastService.show).toHaveBeenCalledWith(
        'Registration failed. Please try again.',
        'error'
      );
    });

    it('should include selected file in registration request', () => {
      const mockFile = new File(['image'], 'profile.jpg', { type: 'image/jpeg' });
      component.selectedFile = mockFile;

      authService.registerPassenger.and.returnValue(
        of('Registration successful!')
      );

      component.onSubmit();

      expect(authService.registerPassenger).toHaveBeenCalledWith(
        jasmine.any(Object),
        mockFile
      );
    });
  });

  describe('Error Messages', () => {
    it('should return correct error message for missing first/last name', () => {
      component.registerForm.patchValue({ firstName: '', lastName: '' });
      component.registerForm.markAllAsTouched();

      const errorMsg = component.getErrorMessage();
      expect(errorMsg).toBe('First and last name are required!');
    });

    it('should return correct error message for missing email', () => {
      component.registerForm.patchValue({
        firstName: 'John',
        lastName: 'Doe',
        email: ''
      });
      component.registerForm.markAllAsTouched();

      const errorMsg = component.getErrorMessage();
      expect(errorMsg).toBe('Email is required!');
    });

    it('should return correct error message for invalid email format', () => {
      component.registerForm.patchValue({
        firstName: 'John',
        lastName: 'Doe',
        email: 'invalid-email'
      });
      component.registerForm.markAllAsTouched();

      const errorMsg = component.getErrorMessage();
      expect(errorMsg).toBe('Email format is invalid!');
    });

    it('should return correct error message for missing phone', () => {
      component.registerForm.patchValue({
        firstName: 'John',
        lastName: 'Doe',
        email: 'john@example.com',
        phone: ''
      });
      component.registerForm.markAllAsTouched();

      const errorMsg = component.getErrorMessage();
      expect(errorMsg).toBe('Phone number is required!');
    });

    it('should return correct error message for invalid phone format', () => {
      component.registerForm.patchValue({
        firstName: 'John',
        lastName: 'Doe',
        email: 'john@example.com',
        phone: '123'
      });
      component.registerForm.markAllAsTouched();

      const errorMsg = component.getErrorMessage();
      expect(errorMsg).toBe('Phone format: 10-15 digits!');
    });

    it('should return correct error message for password mismatch', () => {
      component.registerForm.patchValue({
        firstName: 'John',
        lastName: 'Doe',
        email: 'john@example.com',
        phone: '1234567890',
        address: '123 Main St',
        newPassword: 'StrongPass123!',
        confirmedPassword: 'DifferentPass123!'
      });
      component.registerForm.markAllAsTouched();

      const errorMsg = component.getErrorMessage();
      expect(errorMsg).toBe('Passwords do not match!');
    });
  });

  describe('File Upload', () => {
    it('should accept valid image file', () => {
      const mockFile = new File(['image'], 'profile.jpg', { type: 'image/jpeg' });
      const event = {
        target: { files: [mockFile] }
      } as any;

      component.onFileSelected(event);

      expect(component.selectedFile).toBe(mockFile);
      expect(component.selectedFileName).toBe('profile.jpg');
    });

    it('should reject non-image files', () => {
      const mockFile = new File(['content'], 'document.pdf', { type: 'application/pdf' });
      const event = {
        target: { files: [mockFile] }
      } as any;

      component.onFileSelected(event);

      expect(toastService.show).toHaveBeenCalledWith(
        'Please select an image file',
        'warning'
      );
      expect(component.selectedFile).toBeNull();
    });

    it('should reject files larger than 5MB', () => {
      const largeFile = new File(
        [new ArrayBuffer(6 * 1024 * 1024)],
        'large.jpg',
        { type: 'image/jpeg' }
      );
      const event = {
        target: { files: [largeFile] }
      } as any;

      component.onFileSelected(event);

      expect(toastService.show).toHaveBeenCalledWith(
        'File size must be less than 5MB',
        'warning'
      );
      expect(component.selectedFile).toBeNull();
    });

    it('should create image preview for valid file', (done) => {
      const mockFile = new File(['image'], 'profile.jpg', { type: 'image/jpeg' });
      const event = {
        target: { files: [mockFile] }
      } as any;

      component.onFileSelected(event);

      setTimeout(() => {
        expect(component.imgPreview).toBeTruthy();
        done();
      }, 100);
    });
  });

  describe('Password Visibility Toggle', () => {
    it('should initialize with hidden passwords', () => {
      expect(component.hidePassword).toBeTruthy();
      expect(component.hideRepeatPassword).toBeTruthy();
    });

    it('should toggle password visibility', () => {
      component.hidePassword = false;
      expect(component.hidePassword).toBeFalsy();

      component.hidePassword = true;
      expect(component.hidePassword).toBeTruthy();
    });
  });
});
