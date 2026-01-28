import { AbstractControl, FormArray, ValidationErrors, ValidatorFn } from '@angular/forms';

export function duplicateFormArrayEmailValidator(trackers: FormArray): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const email = control.value?.trim().toLowerCase();
    if (!email) return null;

    const isDuplicate = trackers.value.some(
      (existingEmail: string) => existingEmail?.toLowerCase() === email
    );

    return isDuplicate ? { duplicateFormArrayEmail: true } : null;
  };
}