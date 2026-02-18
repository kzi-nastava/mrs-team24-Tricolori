import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { NgIcon, provideIcons } from '@ng-icons/core';
import {
  heroMapPin,
  heroFlag,
  heroClock,
  heroXMark,
  heroUser,
  heroShieldCheck
} from '@ng-icons/heroicons/outline';
import { CancelRideModalComponent } from '../../../driver/components/cancel-ride-modal/cancel-ride-modal';
import { RideService } from '../../../../../services/ride.service';
import { Router, ActivatedRoute } from '@angular/router';
import {ToastService} from '../../../../../services/toast.service';
import { RideAssignmentResponse } from '../../../../../model/ride';

@Component({
  selector: 'app-ride-wait',
  standalone: true,
  imports: [CommonModule, NgIcon, CancelRideModalComponent, DecimalPipe],
  templateUrl: './ride-wait.html',
  viewProviders: [provideIcons({
    heroMapPin, heroFlag, heroClock, heroXMark, heroUser, heroShieldCheck
  })]
})
export class RideWait implements OnInit {
  private rideService = inject(RideService);
  private router = inject(Router);
  private toastService = inject(ToastService);
  private route = inject(ActivatedRoute);

  showCancelModal = signal(false);

  activeRide = signal<RideAssignmentResponse | undefined>(undefined);

  ngOnInit(): void {
    const rideId = this.route.snapshot.paramMap.get('id');

    if (rideId) {
      this.loadRideData(+rideId);
    } else {
      this.toastService.show('Invalid ride ID', 'error');
      this.router.navigate(['/passenger/home']);
    }
  }

  private loadRideData(id: number) {
    this.rideService.getRideDetails(id).subscribe({
      next: (res: RideAssignmentResponse) => {
        this.activeRide.set(res);
      },
      error: (err) => {
        const msg = err.error?.message || 'Could not load ride details';
        this.toastService.show(msg, 'error');
        this.router.navigate(['/passenger/home']);
      }
    });
  }

  handleCancel() {
    this.showCancelModal.set(true);
  }

  submitCancellation(reason: string) {
    this.rideService.cancelRide(reason).subscribe({
      next: () => {
        this.showCancelModal.set(false);
        this.toastService.show('Ride canceled successfully!', 'success');
        this.router.navigate(['/passenger/home']);
      },
      error: (err) => {
        this.showCancelModal.set(false);
        const msg = err.error?.message || err.error || 'Something went wrong';
        this.toastService.show(msg, 'error');
      }
    });
  }
}
