import { Component, inject, signal } from '@angular/core';
import { ProfileService } from '../../../core/services/profile.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProfileResponse } from '../../../shared/model/profile.model';
import { NgIcon } from '@ng-icons/core';

@Component({
  selector: 'app-base-profile',
  imports: [
    ReactiveFormsModule,
    NgIcon
  ],
  templateUrl: './base-profile.html',
  styleUrl: './base-profile.css',
})
export class BaseProfile {
  private profileService = inject(ProfileService);
  private formBuilder = inject(FormBuilder);

  personalForm: FormGroup;

  userProfile = signal<ProfileResponse | null>(null);
  editEnabled = signal(false);
  email = "hello"

  constructor() {
    this.personalForm = this.formBuilder.group({
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      address: ['', [Validators.required]],
      phone: ['', [Validators.required /*, Custom phone validation*/]],
    })
  }

  get editable() {
    return !!this.editEnabled();
  }

  resetChanges() {}

  toggleEdit() {}
}
