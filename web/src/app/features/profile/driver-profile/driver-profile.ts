import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ProfileService } from '../../../core/services/profile.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProfileResponse } from '../../../shared/model/profile.model';
import { NgIcon } from '@ng-icons/core';
import { BaseProfile } from '../base-profile/base-profile';

@Component({
  selector: 'app-driver-profile',
  imports: [
    ReactiveFormsModule,
    NgIcon,
    BaseProfile
  ],
  templateUrl: './driver-profile.html',
  styleUrl: './driver-profile.css',
})
export class DriverProfile extends BaseProfile {
  vehicle = computed(() => this.userProfile()?.vehicle);
  activeHours = computed(() => this.userProfile()?.activeHours);
}

