import { Component, effect, input, output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { StepTwoDriverRegistrationData } from '../../../model/driver-registration';
import { NgIcon } from '@ng-icons/core';

@Component({
  selector: 'app-step-two-form',
  imports: [
    ReactiveFormsModule,
    NgIcon
  ],
  templateUrl: './step-two-form.html',
  styleUrl: './step-two-form.css',
})

export class StepTwoForm {
  secondStepForm: FormGroup;

  oldStepData = input<StepTwoDriverRegistrationData | undefined>();
  newStepData = output<StepTwoDriverRegistrationData>();
  back = output<StepTwoDriverRegistrationData>();

  vehicleTypes = [
    { id: 'standard', label: 'Standard', icon: '🚗' },
    { id: 'business', label: 'Business', icon: '💼' },
    { id: 'van', label: 'Van', icon: '🚐' }
  ];

  constructor() {
    this.secondStepForm = new FormGroup({
      email: new FormControl('', [Validators.required, Validators.email]),
      vehicleModel: new FormControl('', [Validators.required]),
      vehicleType: new FormControl('', [Validators.required]),
      registrationPlate: new FormControl('', [Validators.required]),
      seatNumber: new FormControl('', [Validators.required, Validators.min(1)]),
      petFriendly: new FormControl(false),
      babyFriendly: new FormControl(false),
    });

    effect(() => {
      const data = this.oldStepData();
      if (data) {
        this.secondStepForm.patchValue(data);
      }
    });
  }

  get email() { return this.secondStepForm.get('email')!; }
  get vehicleModel() { return this.secondStepForm.get('vehicleModel')!; }
  get vehicleType() { return this.secondStepForm.get('vehicleType')!; }
  get registrationPlate() { return this.secondStepForm.get('registrationPlate')!; }
  get seatNumber() { return this.secondStepForm.get('seatNumber')!; }
  get petFriendly() { return this.secondStepForm.get('petFriendly')!; }
  get babyFriendly() { return this.secondStepForm.get('babyFriendly')!; }

  handleSubmit() {
    this.secondStepForm.markAllAsTouched();
    if (this.secondStepForm.valid) {
      this.newStepData.emit(this.secondStepForm.value)
    }
  }

  goBack() {
    this.back.emit(this.secondStepForm.value);
  }
}
