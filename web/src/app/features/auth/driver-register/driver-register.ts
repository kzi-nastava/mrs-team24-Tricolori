import { Component, model } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIcon } from '@ng-icons/core';
import { DriverRegistrationData } from '../../../shared/model/driver-registration';

@Component({
  selector: 'app-driver-register',
  imports: [
    FormsModule, 
    ReactiveFormsModule,
    NgIcon
  ],
  templateUrl: './driver-register.html',
  styleUrl: './driver-register.css',
})


export class DriverRegister {
  step: number;
  firstStepForm: FormGroup;
  secondStepForm: FormGroup;

  selectedStepClasses = "w-10 h-10 bg-base-700 text-white rounded-full flex items-center justify-center font-bold step-shadow";
  selectedTextClasses = "text-xs mt-2 text-base-600 font-medium";
  otherStepClasses = "w-10 h-10 border-2 border-gray-200 text-gray-400 rounded-full flex items-center justify-center font-bold";
  otherTextClasses = "text-xs mt-2 text-gray-400 font-medium";

  constructor() {
    this.step = 1;
    this.firstStepForm = new FormGroup({
      firstName: new FormControl('', [Validators.required]),
      lastName: new FormControl('', [Validators.required]),
      phone: new FormControl('', [Validators.required]),
      address: new FormControl('', [Validators.required])
    });

    this.secondStepForm = new FormGroup({
      email: new FormControl('', [Validators.required, Validators.email]),
      vehicleModel: new FormControl('', [Validators.required]),
      vehicleType: new FormControl('', [Validators.required]),
      registrationPlate: new FormControl('', [Validators.required]),
      seatNumber: new FormControl('', [Validators.required, Validators.min(1)]),
      petFriendly: new FormControl(false),
      babyFriendly: new FormControl(false),
    });
  }

  get firstName() { return this.firstStepForm.get('firstName')!; }
  get lastName() { return this.firstStepForm.get('lastName')!; }
  get phone() { return this.firstStepForm.get('phone')!; }
  get address() { return this.firstStepForm.get('address')!; }
  get email() { return this.secondStepForm.get('email')!; }
  get vehicleModel() { return this.secondStepForm.get('vehicleModel')!; }
  get vehicleType() { return this.secondStepForm.get('vehicleType')!; }
  get registrationPlate() { return this.secondStepForm.get('registrationPlate')!; }
  get seatNumber() { return this.secondStepForm.get('seatNumber')!; }
  get petFriendly() { return this.secondStepForm.get('petFriendly')!; }
  get babyFriendly() { return this.secondStepForm.get('babyFriendly')!; }

  firstSubmit() {
    this.firstStepForm.markAllAsTouched();
    if (this.firstStepForm.valid)
      this.step = 2;
  }

  secondSubmit() {
    this.secondStepForm.markAllAsTouched();
    if (this.secondStepForm.valid) {
      const finalData: DriverRegistrationData = {
        ...this.firstStepForm.value,
        ...this.secondStepForm.value,
        seatNumber: Number(this.secondStepForm.value.seatNumber)
      };

      console.log('Data ready for backend:', finalData);

      this.firstStepForm.reset();
      this.secondStepForm.reset({
        petFriendly: false,
        babyFriendly: false
      });

      this.firstStepForm.markAsPristine();
      this.firstStepForm.markAsUntouched();
      this.secondStepForm.markAsPristine();
      this.secondStepForm.markAsUntouched();
      
      this.step = 1;
    }
  }

  prevStep() {
    this.step = 1;
  }
}
