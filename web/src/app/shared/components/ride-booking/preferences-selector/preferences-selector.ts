import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { NgIcon } from '@ng-icons/core';

@Component({
  selector: 'app-preferences-selector',
  imports: [
    ReactiveFormsModule,
    NgIcon
  ],
  templateUrl: './preferences-selector.html',
  styleUrl: './preferences-selector.css',
})
export class PreferencesSelector {
  private fb = inject(FormBuilder);

  selectedType = signal('standard');

  preferencesForm: FormGroup = this.fb.group({
    vehicleType: ['standard'],
    babySeat: [false],
    petFriendly: [false],
    scheduleLater: [false]
  });

  vehicleTypes = [
    { id: 'standard', label: 'Standard', icon: '🚗' },
    { id: 'business', label: 'Business', icon: '💼' },
    { id: 'van', label: 'Van', icon: '🚐' }
  ];

  constructor() {
    this.preferencesForm.get('vehicleType')?.valueChanges.subscribe(value => {
      this.selectedType.set(value);
    });
  }

  get currentIcon() {
    return this.vehicleTypes.find(t => t.id === this.selectedType())?.icon || '🚗';
  }
}
