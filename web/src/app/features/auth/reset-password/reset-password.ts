import { Component } from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {passwordMatchValidator, strongPasswordValidator} from '../../../shared/passwordValidator';
import {NgIcon} from '@ng-icons/core';

@Component({
  selector: 'app-reset-password',
  imports: [
    ReactiveFormsModule,
    NgIcon
  ],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css',
})
export class ResetPassword {
  hideNewPassword: boolean = true;
  hideConfirmedPassword: boolean = true;

  resetPasswordForm = new FormGroup({
    newPassword: new FormControl('', [Validators.required, strongPasswordValidator]),
    confirmedPassword: new FormControl('', [Validators.required])
  }, { validators: passwordMatchValidator })

  onSubmit() {
    if (this.resetPasswordForm.valid) {
      const formData = this.resetPasswordForm.value;
      alert('Password successfully reset to: ' + formData.newPassword);
    } else {
      if (this.resetPasswordForm.errors?.['passwordMismatch']) {
        alert('Passwords do not match!');
      } else if (this.resetPasswordForm.get('newPassword')?.errors?.['weakPassword']) {
        alert('Password is too weak!');
      } else {
        alert('Please fill in all fields correctly!');
      }
    }
  }
}
