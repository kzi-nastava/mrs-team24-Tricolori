import { Component } from '@angular/core';
import { NgIcon } from '@ng-icons/core';

@Component({
  selector: 'app-password-change',
  imports: [NgIcon],
  templateUrl: './password-change.html',
  styleUrl: './password-change.css',
})

export class PasswordChange {
  showCurrentPassword = false;
  showNewPassword = false;
  showConfirmedPassword = false;

  currentPassword: string = '';
  newPassword: string = '';
  confirmPassword: string = '';

  isValidLength(): boolean {
    return this.newPassword.length >= 8;
  }

  hasUpperAndLowerCase(): boolean {
    return /[a-z]/.test(this.newPassword) && /[A-Z]/.test(this.newPassword);
  }

  hasNumber(): boolean {
    return /\d/.test(this.newPassword);
  }

  hasSpecialCharacter(): boolean {
    return /[!@#$%^&*(),.?":{}|<>]/.test(this.newPassword);
  }
}
