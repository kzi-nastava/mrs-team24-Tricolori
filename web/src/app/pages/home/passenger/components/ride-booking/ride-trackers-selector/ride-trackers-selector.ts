import { Component, inject } from '@angular/core';
import { FormArray, FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIcon } from '@ng-icons/core';
import { duplicateFormArrayEmailValidator } from '../../../../../../shared/duplicate-form-array-email-validator';

@Component({
  selector: 'app-ride-trackers-selector',
  imports: [
    NgIcon,
    ReactiveFormsModule
  ],
  templateUrl: './ride-trackers-selector.html',
  styleUrl: './ride-trackers-selector.css',
})

export class RideTrackersSelector {
  private fb = inject(FormBuilder);

  trackersForm = this.fb.group({
    trackers: this.fb.array([])
  });

  newTrackerEmail = new FormControl('', {
    validators: [Validators.email, duplicateFormArrayEmailValidator(this.trackers)],
    updateOn: 'change'
  });

  get trackers() {
    return this.trackersForm.get('trackers') as FormArray;
  }

  addTracker() {
    if (this.newTrackerEmail.value && this.newTrackerEmail.valid) {
      this.trackers.push(this.fb.control(this.newTrackerEmail.value, [Validators.required, Validators.email]));
      this.newTrackerEmail.reset();
    }
  }

  removeTracker(index: number) {
    this.trackers.removeAt(index);
    this.newTrackerEmail.updateValueAndValidity();
  }
}
