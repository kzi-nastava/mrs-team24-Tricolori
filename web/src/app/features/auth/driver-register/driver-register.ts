import { Component, model } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgIcon } from '@ng-icons/core';

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
      firstName: new FormControl(''),
      lastName: new FormControl(''),
      phone: new FormControl(''),
      address: new FormControl('')
    });
    this.secondStepForm = new FormGroup({
      email: new FormControl(''),
      vehicleModel: new FormControl(''),
      vehicleType: new FormControl(''),
      registrationPlate: new FormControl(),
      seatNumber: new FormControl(''),
      petFriendly: new FormControl(''),
      babyFriendly: new FormControl(''),
    });
  }

  firstSubmit() {
    this.step = 2;
  }

  secondSubmit() {
    this.step = 1;
  }

  prevStep() {
    this.step = 1;
  }
}
