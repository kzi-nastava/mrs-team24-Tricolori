import { Component } from '@angular/core';

@Component({
  selector: 'app-password-change',
  imports: [],
  templateUrl: './password-change.html',
  styleUrl: './password-change.css',
})

export class PasswordChange {
  showCurrentPassword = false;
  showNewPassword = false;
  showConfirmedPassword = false;
}
