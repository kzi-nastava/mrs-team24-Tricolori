import { Component, inject, OnInit, signal } from '@angular/core';
import { ProfileService } from '../../../core/services/profile.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProfileResponse } from '../../../shared/model/profile.model';
import { NgIcon } from '@ng-icons/core';
import { environment } from '../../../../environments/environment';

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

  // Handling pfp selection:
  selectedFile: File | null = null;
  imagePreviewSrc = signal<string | null>(null);

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

  handlePfpError(event: any) {
    event.target.src = environment.defaultPfp;
  }

  updateProfile() {
    if (this.personalForm.invalid || !this.hasChanges()) return;

    // Disable potential multiple requests immediately:
    this.hasChanges.set(false);

    // Check for pfp selection:
    if (this.selectedFile) {
      const formData = new FormData();
      formData.append("pfp", this.selectedFile);

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

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if(file) {
      if (file.size > 5 * 1024 * 1024) {
          alert("File is too large (max 5MB)");
          return;
      }

      this.selectedFile = file;

      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreviewSrc.set(reader.result as string);
        this.hasChanges.set(true);
      }
      reader.readAsDataURL(file);
    }
  }

  get pfp() { 
    const previewSrc = this.imagePreviewSrc();
    if(previewSrc) return previewSrc;

    return this.personalForm.get('pfp')?.value || environment.defaultPfp; 
  }

  private checkChanges() {
    const original = this.userProfile(); 
    if (!original) {
      this.hasChanges.set(false);
      return;
    }

    const current = this.personalForm.value;

    const textChanged = 
      current.firstName   != original.firstName ||
      current.lastName    != original.lastName ||
      current.homeAddress != original.homeAddress ||
      current.phoneNumber != original.phoneNumber ||
      current.email       != original.email ||
      current.pfp         != original.pfp;

    const fileChanged = this.selectedFile !== null;

    this.hasChanges.set(textChanged || fileChanged);
  }

  private sendUpdateRequest() {
    this.profileService.updateProfile(this.personalForm.value).subscribe({
      next: (updatedProfile) => {
        this.userProfile.set(updatedProfile);
        this.personalForm.patchValue(updatedProfile, {emitEvent: false});
        this.hasChanges.set(false);

        this.selectedFile = null;
        this.imagePreviewSrc.set(null);
      },
      error: (err) => {
        console.error("Error: ", err);
      },
    })
  }
}
