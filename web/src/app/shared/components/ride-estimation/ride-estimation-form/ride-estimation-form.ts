import {Component, input, output} from '@angular/core';
import {NgIcon} from '@ng-icons/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';

@Component({
  selector: 'app-ride-estimation-form',
  imports: [
    NgIcon,
    ReactiveFormsModule
  ],
  templateUrl: './ride-estimation-form.html',
  styleUrl: './ride-estimation-form.css',
})
export class RideEstimationForm {
  errorMessage = input<string>('');
  onEstimate = output<{pickup: string, destination: string}>();

  estimateForm = new FormGroup({
    pickup: new FormControl('', { validators: [Validators.required], nonNullable: true }),
    destination: new FormControl('', { validators: [Validators.required], nonNullable: true })
  });

  onSubmit() {
    if (this.estimateForm.valid) {
      this.onEstimate.emit(this.estimateForm.getRawValue());
    }
  }
}
