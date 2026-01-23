import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ProfileService } from '../../../core/services/profile.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProfileResponse } from '../../../shared/model/profile.model';
import { NgIcon } from '@ng-icons/core';
import { BaseProfile } from '../base-profile/base-profile';
import { environment } from '../../../../environments/environment';

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
  readonly WORK_GOAL = environment.dailyWorkGoal;

  vehicle = computed(() => this.userProfile()?.vehicle);
  activeHours = computed(() => this.userProfile()?.activeHours);

  activityPercentage = computed(() => {
    const hours = this.activeHours() || 0;
    return Math.round((hours / this.WORK_GOAL) * 100);
  });

  progressBarWidth = computed(() => {
    return Math.min(this.activityPercentage(), 100);
  });
}

