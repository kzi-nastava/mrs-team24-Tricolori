import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ProfileService } from '../../../services/profile.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProfileResponse } from '../../../model/profile.model';
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

  protected override sendUpdateRequest() {
    this.profileService.requestProfileChanges(this.personalForm.value).subscribe({
      next: (res) => {
        alert("Poslat zahtjev za izmjenu profila")
        console.log("[Request Profile Changes]:", res);
        // Reset everything:
        this.selectedFile.set(null);
        this.pfpPicker()?.reset();
        this.hasChanges.set(false);
      },
      error: (err) => {
        console.error("Error: ", err);
      },
    });
    /*this.profileService.updateProfile(this.personalForm.value).subscribe({
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
    })*/
  }
}

