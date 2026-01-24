import { Component, inject, model } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIcon } from '@ng-icons/core';
import { AdminDriverRegistrationRequest, StepOneDriverRegistrationData, StepTwoDriverRegistrationData } from '../../../shared/model/driver-registration';
import { StepOneForm } from '../../../shared/components/driver-registration/step-one-form/step-one-form';
import { StepTwoForm } from '../../../shared/components/driver-registration/step-two-form/step-two-form';
import { PfpPicker } from '../../../shared/components/pfp-picker/pfp-picker';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-driver-register',
  imports: [
    FormsModule,
    ReactiveFormsModule,
    StepOneForm,
    StepTwoForm,
],
  templateUrl: './driver-register.html',
  styleUrl: './driver-register.css',
})


export class DriverRegister {
  private authService = inject(AuthService);

  step: number;
  
  savedStepOneData?: StepOneDriverRegistrationData;
  savedStepTwoData?: StepTwoDriverRegistrationData;

  // Tailwind class combination for step indicators:
  selectedStepClasses = "w-10 h-10 bg-base-700 text-white rounded-full flex items-center justify-center font-bold step-shadow";
  selectedTextClasses = "text-xs mt-2 text-base-600 font-medium";
  otherStepClasses = "w-10 h-10 border-2 border-gray-200 text-gray-400 rounded-full flex items-center justify-center font-bold";
  otherTextClasses = "text-xs mt-2 text-gray-400 font-medium";

  constructor() {
    this.step = 1;
  }

  handleStepOne(data: StepOneDriverRegistrationData) {
    this.savedStepOneData = data;
    this.step = 2;
  }

  handleStepTwo(data: StepTwoDriverRegistrationData) {
    this.savedStepTwoData = data;

    const { pfpFile, ...personalData } = this.savedStepOneData!;

    const finalRequest: AdminDriverRegistrationRequest = {
      ...personalData,
      ...this.savedStepTwoData!,
      vehicleType: (data.vehicleType as any).id || data.vehicleType 
    };

    const fileToUpload = pfpFile || null;

    // Send request:
    this.authService.registerDriver(finalRequest, fileToUpload).subscribe({
      next: (res) => { 
        console.log("Success!", res);
        // Because I use @if, components will be recreated, but this time
        // since saved data is undefined, they will be brand new, empty...
        this.savedStepOneData = undefined;
        this.savedStepTwoData = undefined;
        this.step = 1;
      },
      error: (err) => { console.log("Error: ", err) }
    })
  }

  prevStep(data: StepTwoDriverRegistrationData) {
    this.savedStepTwoData = data;
    this.step = 1;
  }
}
