import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
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
import { ToastService } from '../../../../../services/toast.service';
import { RideAssignmentResponse } from '../../../../../model/ride';
import { WebSocketService } from '../../../../../services/websocket.service';

@Component({
  selector: 'app-ride-wait',
  standalone: true,
  imports: [CommonModule, NgIcon, CancelRideModalComponent, DecimalPipe],
  templateUrl: './ride-wait.html',
  viewProviders: [provideIcons({
    heroMapPin, heroFlag, heroClock, heroXMark, heroUser, heroShieldCheck
  })]
})
export class RideWait implements OnInit, OnDestroy {
  private rideService = inject(RideService);
  private router = inject(Router);
  private toastService = inject(ToastService);
  private route = inject(ActivatedRoute);
  private webSocketService = inject(WebSocketService);

  private readonly rideUpdatesTopic = '/user/queue/ride-updates';

  showCancelModal = signal(false);
  activeRide = signal<RideAssignmentResponse | undefined>(undefined);

  ngOnInit(): void {
    const rideId = this.route.snapshot.paramMap.get('id');

    if (rideId) {
      this.loadRideData(+rideId);
      this.subscribeToRideUpdates();
    } else {
      this.toastService.show('Invalid ride ID', 'error');
      this.router.navigate(['/passenger/home']);
    }
  }

  ngOnDestroy(): void {
    this.webSocketService.unsubscribe(this.rideUpdatesTopic);
  }

  private subscribeToRideUpdates(): void {
    this.webSocketService.subscribe(
      this.rideUpdatesTopic,
      (update: any) => {
        console.log('Ride update received:', update);
        this.handleRideUpdate(update);
      }
    );
  }

  private handleRideUpdate(update: any): void {
    switch (update.status) {
      case 'ONGOING':
        this.toastService.show('Your ride has started!', 'success');
        this.router.navigate(['/passenger/ride-tracking', this.activeRide()?.id]);
        break;

      case 'CANCELLED_BY_DRIVER':
        this.toastService.show(update.message || 'Driver cancelled your ride', 'error');
        this.router.navigate(['/passenger/home']);
        break;

      case 'CANCELLED_BY_PASSENGER':
        this.toastService.show(update.message || 'Ride has been cancelled', 'error');
        this.router.navigate(['/passenger/home']);
        break;

      case 'FINISHED':
        this.toastService.show('Your ride has been finished!', 'success');
        this.router.navigate(['/passenger/ride-history', this.activeRide()?.id]);
        break;

      default:
        console.log('Unknown ride status:', update.status);
    }
  }

  private loadRideData(id: number): void {
    this.rideService.getRideAssignment(id).subscribe({
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

  handleCancel(): void {
    this.showCancelModal.set(true);
  }

  submitCancellation(reason: string): void {
    const rideId = this.activeRide()?.id;

    if (!rideId) {
      this.toastService.show('Ride ID missing', 'error');
      this.showCancelModal.set(false);
      return;
    }

    this.rideService.cancelRide(rideId, reason).subscribe({
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
