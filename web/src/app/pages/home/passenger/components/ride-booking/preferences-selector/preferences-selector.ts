import { AfterViewInit, Component, effect, inject, input, output, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIcon } from '@ng-icons/core';
import { DatePipe } from '@angular/common';
import { environment } from '../../../../../../../environments/environment';
import { RideService } from '../../../../../../services/ride.service';

@Component({
  selector: 'app-preferences-selector',
  imports: [
    ReactiveFormsModule,
    NgIcon,
    DatePipe
  ],
  templateUrl: './preferences-selector.html',
  styleUrl: './preferences-selector.css'
})
export class PreferencesSelector {
  private fb = inject(FormBuilder);
  private rideService = inject(RideService);

  readonly vehicleTypes = environment.vehicleTypes;

  selectedType = signal('standard');
  scheduled = input<Date>();
  scheduleWanted = output();

  preferencesForm: FormGroup;

  get vehicleType() { return this.preferencesForm.get('vehicleType'); }
  get babySeat() { return this.preferencesForm.get('babySeat'); }
  get petFriendly() { return this.preferencesForm.get('petFriendly'); }
  get scheduledTime() { return this.preferencesForm.get('scheduledTime'); }
  get isScheduled(): boolean {
    return !!this.scheduledTime?.value;
  }

  constructor() {
    this.preferencesForm = this.fb.group({
      vehicleType: [environment.vehicleTypes[0].id, [Validators.required]],
      babySeat: [false],
      petFriendly: [false],
      scheduledTime: [null as Date | null]
    });

    this.preferencesForm.get('vehicleType')?.valueChanges.subscribe(value => {
      this.selectedType.set(value);
    });

    effect(() => {
      const time = this.scheduled();
      if (time) {
        this.preferencesForm.patchValue({
          scheduledTime: time
        })
      }
    })
  }

  schedule() {
    if (this.isScheduled) {
      this.preferencesForm.patchValue({
        scheduledTime: null
      });
    } else {
      this.scheduleWanted.emit();
    }
  }

  getPreferences() {
    const formData = this.preferencesForm.getRawValue();

    // This maps to RidePreferemces on backend...
    return {
      vehicleType: formData.vehicleType.toUpperCase(),
      petFriendly: formData.petFriendly,
      babyFriendly: formData.babySeat,
      scheduledFor: this.rideService.formatLocalDateTime(formData.scheduledTime)
    }
  }

  isTomorrow(date: Date | null): boolean {
    if (!date) return false;

    const today = new Date();
    return date.getDate() !== today.getDate();
  }

  get currentIcon() {
    return this.vehicleTypes.find(t => t.id === this.selectedType())?.icon || '🚗';
  }
}
