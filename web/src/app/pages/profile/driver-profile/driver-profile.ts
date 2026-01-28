import { Component, computed } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { NgIcon } from '@ng-icons/core';
import { BaseProfile } from '../base-profile/base-profile';
import { PfpPicker } from '../../../components/pfp-picker/pfp-picker';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-driver-profile',
  standalone: true,
  imports: [ReactiveFormsModule, NgIcon, PfpPicker],
  templateUrl: './driver-profile.html'
})
export class DriverProfile extends BaseProfile {
  readonly WORK_GOAL = environment.dailyWorkGoal;

  vehicle = computed(() => this.userProfile()?.vehicle);
  activeHours = computed(() => this.userProfile()?.activeHours);
  
  activityPercentage = computed(() => Math.round(((this.activeHours() || 0) / this.WORK_GOAL) * 100));
  progressBarWidth = computed(() => Math.min(this.activityPercentage(), 100));

  protected override sendUpdateRequest() {
    console.log("DRIVER VERZIJA: Šaljem zahtev adminu na odobrenje...");
    
    // Šaljemo ProfileRequest backendu (/api/v1/change-requests/create)
    this.profileService.createChangeRequest(this.personalForm.getRawValue()).subscribe({
      next: () => {
        alert("Zahtev poslat adminu. Podaci će se promeniti nakon odobrenja.");
        // Vraćamo formu na staro stanje iz baze
        if (this.userProfile()) {
          this.personalForm.patchValue(this.userProfile()!, { emitEvent: false });
        }
        this.selectedFile.set(null);
        this.pfpPicker()?.reset();
        this.hasChanges.set(false);
      },
      error: (err) => alert(err.status === 400 ? "Već imate zahtev na čekanju!" : "Greška!")
    });
  }
}