import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIcon } from '@ng-icons/core';
import { passwordMatchValidator, strongPasswordValidator } from '../../../shared/passwordValidator';

@Component({
  selector: 'app-password-setup',
  imports: [
    ReactiveFormsModule,
    NgIcon
  ],
  templateUrl: './password-setup.html',
  styleUrl: './password-setup.css',
})
export class PasswordSetup {
  hideNewPassword: boolean = true;
  hideConfirmedPassword: boolean = true;

  passwordForm = new FormGroup({
    newPassword: new FormControl('', [Validators.required, strongPasswordValidator]),
    confirmedPassword: new FormControl('', [Validators.required])
  }, { validators: passwordMatchValidator })

  onSubmit() {
    if (this.passwordForm.valid) {
      const formData = this.passwordForm.value;
      alert('Password successfully set to: ' + formData.newPassword);
    } else {
      if (this.passwordForm.errors?.['passwordMismatch']) {
        alert('Passwords do not match!');
      } else if (this.passwordForm.get('newPassword')?.errors?.['weakPassword']) {
        alert('Password is too weak!');
      } else {
        alert('Please fill in all fields correctly!');
      }
    }
  }
}
