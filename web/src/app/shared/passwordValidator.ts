import {AbstractControl, ValidationErrors, ValidatorFn} from '@angular/forms';

export const strongPasswordValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = control.value || '';
  const hasUpper = /[A-Z]/.test(value);
  const hasLower = /[a-z]/.test(value);
  const hasNumber = /[0-9]/.test(value);
  const validLength = value.length >= 8;
  const passwordValid = hasUpper && hasLower && hasNumber && validLength;
  return passwordValid ? null : { weakPassword: true };
}

export const passwordMatchValidator: ValidatorFn = (control: AbstractControl) : ValidationErrors | null => {
  const newPassword = control.get('newPassword')?.value;
  const confirmedPassword = control.get('confirmedPassword')?.value;

  if (!newPassword || !confirmedPassword) {
    return null;
  }

  return newPassword === confirmedPassword ? null : { passwordMismatch: true};
}
