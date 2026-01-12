import { Component, effect, input, output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { StepOneDriverRegistrationData } from '../../../model/driver-registration';

@Component({
  selector: 'app-step-one-form',
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './step-one-form.html',
  styleUrl: './step-one-form.css',
})

export class StepOneForm {
  firstStepForm: FormGroup;

  oldStepData = input<StepOneDriverRegistrationData | undefined>();
  newStepData = output<StepOneDriverRegistrationData>();

  constructor() {
    this.firstStepForm = new FormGroup({
      firstName: new FormControl('', [Validators.required]),
      lastName: new FormControl('', [Validators.required]),
      phone: new FormControl('', [Validators.required]),
      address: new FormControl('', [Validators.required])
    });

    effect(() => {
      const data = this.oldStepData();
      if (data) {
        this.firstStepForm.patchValue(data);
      }
    });
  }

  get firstName() { return this.firstStepForm.get('firstName')!; }
  get lastName() { return this.firstStepForm.get('lastName')!; }
  get phone() { return this.firstStepForm.get('phone')!; }
  get address() { return this.firstStepForm.get('address')!; }

  submit() {
    this.firstStepForm.markAllAsTouched();
    if (this.firstStepForm.valid)
      this.newStepData.emit(this.firstStepForm.value);
  }
}
