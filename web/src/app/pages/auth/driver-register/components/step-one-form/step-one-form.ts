import { Component, effect, input, output, signal, viewChild } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { StepOneDriverRegistrationData } from '../../../../../model/driver-registration';
import { NgIcon } from '@ng-icons/core';
import { PfpPicker } from '../../../../../components/pfp-picker/pfp-picker';

@Component({
  selector: 'app-step-one-form',
  imports: [
    ReactiveFormsModule,
    NgIcon,
    PfpPicker
  ],
  templateUrl: './step-one-form.html',
  styleUrl: './step-one-form.css',
})

export class StepOneForm {
  firstStepForm: FormGroup;

  oldStepData = input<StepOneDriverRegistrationData | undefined>();
  newStepData = output<StepOneDriverRegistrationData>();

  pfpPicker = viewChild<PfpPicker>('pfpPicker')
  selectedFile = signal<File | null>(null);

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
        this.selectedFile.set(data.pfpFile);
      }
    });
  }

  get firstName() { return this.firstStepForm.get('firstName')!; }
  get lastName() { return this.firstStepForm.get('lastName')!; }
  get phone() { return this.firstStepForm.get('phone')!; }
  get address() { return this.firstStepForm.get('address')!; }

  completeStep() {
    this.firstStepForm.markAllAsTouched();
    if (this.firstStepForm.valid) {
      const stepData: StepOneDriverRegistrationData = {
        ...this.firstStepForm.value,
        pfpFile: this.selectedFile()
      };

      this.newStepData.emit(stepData);
    }
  }

  handleNewFile(file: File) {
    this.selectedFile.set(file);
  }
}
