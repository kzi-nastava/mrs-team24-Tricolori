import { Component, inject, OnInit, signal } from '@angular/core';
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
export class BaseProfile implements OnInit {
  private profileService = inject(ProfileService);
  private formBuilder = inject(FormBuilder);

  personalForm: FormGroup;

  userProfile = signal<ProfileResponse | null>(null);
  hasChanges = signal(false);

  constructor() {
    this.personalForm = this.formBuilder.group({
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      homeAddress: ['', [Validators.required]],
      phoneNumber: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      pfp: ['']
    });

    this.personalForm.valueChanges.subscribe(() => {
      this.checkChanges();
    });
  }

  ngOnInit(): void {
    this.profileService.getMyProfile().subscribe((profile: ProfileResponse) => {
      this.userProfile.set(profile);
      this.personalForm.patchValue(profile, {emitEvent: false});
      this.hasChanges.set(false);
    });
  }

  get pfp() { return this.personalForm.get('pfp')?.value || 'assets/icons/logo.svg'; }

  private checkChanges() {
    const original = this.userProfile(); 
    if (!original) {
      this.hasChanges.set(false);
      return;
    }

    const current = this.personalForm.value;

    const isChanged = 
      current.firstName   != original.firstName ||
      current.lastName    != original.lastName ||
      current.homeAddress != original.homeAddress ||
      current.phoneNumber != original.phoneNumber ||
      current.email       != original.email ||
      current.pfp         != original.pfp;

    this.hasChanges.set(isChanged);
  }

  updateProfile() {
    if (this.personalForm.invalid || !this.hasChanges()) return;

    this.profileService.updateProfile(this.personalForm.value).subscribe({
      next: (updatedProfile) => {
        console.log("Stiglo");
        console.log(updatedProfile)
        this.userProfile.set(updatedProfile);
        this.personalForm.patchValue(updatedProfile, {emitEvent: false});
        this.hasChanges.set(false);
      },
      error: (err) => {
        console.error("Error: ", err);
      },
    })
  }
}
