import { Component, ElementRef, inject, OnInit, signal, ViewChild } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PersonDto } from '../../shared/model/auth.model';
import { ProfileService } from '../../core/services/profile.service';
import { ProfileResponse } from '../../shared/model/profile.model';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-profile',
  imports: [
    RouterLink,
    ReactiveFormsModule
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {
  private profileService = inject(ProfileService);

  private formBuilder = inject(FormBuilder);
  personalForm: FormGroup;

  userProfile = signal<ProfileResponse | null>(null);
  editEnabled = signal(false);
  email: string = ""

  constructor() {
    this.personalForm = this.formBuilder.group({
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      address: ['', [Validators.required]],
      phone: ['', [Validators.required /*, Custom phone validation*/]],
    })
  }

  ngOnInit(): void {
    this.loadData();
  }

  loadData() {
    this.profileService.getMyProfile().subscribe({
      next: (data) => {
        this.userProfile.set(data);
        this.personalForm.patchValue({
          firstName: data.firstName,
          lastName: data.lastName,
          address: data.homeAddress,
          phone: data.phoneNumber
        });
        this.email = data.email;
      },
      error: (err) => console.error(err)
    })
  }

  toggleEdit() {
    this.editEnabled.update(v => !v);
  }

  get editable() {
    return !!this.editEnabled();
  }

  resetChanges() {
    this.editEnabled.set(false);

    const originalData = this.userProfile();
    if (originalData) {
      this.personalForm.patchValue({
        firstName: originalData.firstName,
        lastName: originalData.lastName,
        address: originalData.homeAddress,
        phone: originalData.phoneNumber
      });
    }
  }

}
