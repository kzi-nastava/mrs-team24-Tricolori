import { Component, inject, OnInit, signal, viewChild } from '@angular/core';
import { ProfileService } from '../../../services/profile.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProfileResponse } from '../../../model/profile.model';
import { NgIcon } from '@ng-icons/core';
import { environment } from '../../../../environments/environment';
import { PfpPicker } from '../../../components/pfp-picker/pfp-picker';

@Component({
  selector: 'app-base-profile',
  imports: [
    ReactiveFormsModule,
    NgIcon,
    PfpPicker
  ],
  templateUrl: './base-profile.html',
  styleUrl: './base-profile.css',
})
export class BaseProfile implements OnInit {
  private profileService = inject(ProfileService);
  private formBuilder = inject(FormBuilder);

  personalForm: FormGroup;

  userProfile = signal<ProfileResponse | null>(null);
  selectedFile = signal<File | null>(null);
  hasChanges = signal(false);

  pfpPicker = viewChild<PfpPicker>('pfpPicker');

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
      // TODO: remove this test data:
      profile.activeHours = 3;

      this.userProfile.set(profile);
      this.personalForm.patchValue(profile, {emitEvent: false});
      this.hasChanges.set(false);
    });
  }

  updateProfile() {
    if (this.personalForm.invalid || !this.hasChanges()) return;

    // Disable potential multiple requests immediately:
    this.hasChanges.set(false);
    const fileToUpload = this.selectedFile();

    // Check for pfp selection:
    if (fileToUpload) {
      const formData = new FormData();
      formData.append("pfp", fileToUpload);

      this.profileService.uploadPfp(formData).subscribe({
        next: (response) => {
          this.personalForm.patchValue({ pfp: response.url }, {emitEvent: false});
          // Create request:
          this.sendUpdateRequest();
        },
        error: err => console.error("Error: ", err)
      });
    } else {
      this.sendUpdateRequest();
    }
  }

  handleNewFile(file: File) {
    this.selectedFile.set(file);
    this.hasChanges.set(true);
  }

  private checkChanges() {
    const original = this.userProfile();
    if (!original) return;

    const current = this.personalForm.value;

    const textChanged =
      current.firstName   != original.firstName ||
      current.lastName    != original.lastName ||
      current.homeAddress != original.homeAddress ||
      current.phoneNumber != original.phoneNumber ||
      current.email       != original.email

    const fileChanged = this.selectedFile() !== null;

    this.hasChanges.set(textChanged || fileChanged);
  }

  private sendUpdateRequest() {
    this.profileService.updateProfile(this.personalForm.value).subscribe({
      next: (updatedProfile) => {
        this.userProfile.set(updatedProfile);
        this.personalForm.patchValue(updatedProfile, {emitEvent: false});

        // Reset everything:
        this.selectedFile.set(null);
        this.pfpPicker()?.reset();
        this.hasChanges.set(false);
      },
      error: (err) => {
        console.error("Error: ", err);
      },
    })
  }
}
