import { Component, inject, OnInit, signal, viewChild } from '@angular/core';
import { ProfileService } from '../../../services/profile.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProfileResponse } from '../../../model/profile.model';
import { NgIcon } from '@ng-icons/core';
import { PfpPicker } from '../../../components/pfp-picker/pfp-picker';

@Component({
  selector: 'app-base-profile',
  standalone: true,
  imports: [ReactiveFormsModule, NgIcon, PfpPicker],
  templateUrl: './base-profile.html'
})
export class BaseProfile implements OnInit {
  protected profileService = inject(ProfileService);
  protected formBuilder = inject(FormBuilder);

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
      email: [{value: '', disabled: true}, [Validators.required, Validators.email]],
      pfp: ['']
    });

    this.personalForm.valueChanges.subscribe(() => this.checkChanges());
  }

  ngOnInit(): void {
    this.profileService.getMyProfile().subscribe((profile) => {
      this.userProfile.set(profile);
      this.personalForm.patchValue(profile, { emitEvent: false });
      this.hasChanges.set(false);
    });
  }

  updateProfile() {
    if (this.personalForm.invalid || !this.hasChanges()) return;
    this.hasChanges.set(false);

    if (this.selectedFile()) {
      const formData = new FormData();
      formData.append("pfp", this.selectedFile()!);
      this.profileService.uploadPfp(formData).subscribe({
        next: (res) => {
          this.personalForm.patchValue({ pfp: res.url }, { emitEvent: false });
          this.sendUpdateRequest();
        }
      });
    } else {
      this.sendUpdateRequest();
    }
  }

  protected sendUpdateRequest() {
    // DIREKTAN UPDATE (za putnike/obične usere)
    this.profileService.updateProfile(this.personalForm.getRawValue()).subscribe((updated) => {
      this.userProfile.set(updated);
      this.hasChanges.set(false);
    });
  }

  handleNewFile(file: File) {
    this.selectedFile.set(file);
    this.hasChanges.set(true);
  }

  private checkChanges() {
    const original = this.userProfile();
    if (!original) return;
    const current = this.personalForm.getRawValue();
    const textChanged = current.firstName !== original.firstName || 
                        current.lastName !== original.lastName ||
                        current.homeAddress !== original.homeAddress ||
                        current.phoneNumber !== original.phoneNumber;
    this.hasChanges.set(textChanged || !!this.selectedFile());
  }
}