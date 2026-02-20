import {Component, inject, OnInit, signal} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import {
  heroEye,
  heroXMark,
  heroExclamationTriangle,
  heroMapPin,
  heroFlag,
  heroChevronUp,
  heroChevronDown,
  heroUser
} from '@ng-icons/heroicons/outline';
import { heroStarSolid } from '@ng-icons/heroicons/solid';
import {getStatusClass, RideHistory} from '../../../model/ride';
import {RideService} from '../../../services/ride.service';
import {ToastService} from '../../../services/toast.service';
import {finalize} from 'rxjs/operators';
import {RideDetailsModal} from './components/ride-details-modal/ride-details-modal';
import { Router } from '@angular/router';

@Component({
  selector: 'app-admin-ride-history',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NgIconComponent,
    RideDetailsModal,
    RideDetailsModal
  ],
  providers: [
    provideIcons({
      heroEye,
      heroXMark,
      heroExclamationTriangle,
      heroMapPin,
      heroFlag,
      heroStarSolid,
      heroChevronUp,
      heroChevronDown,
      heroUser
    })
  ],
  templateUrl: './admin-history.html'
})
export class AdminRideHistoryComponent implements OnInit {
  // Filters & State
  startDate = signal<string>('');
  endDate = signal<string>('');
  statusFilter = signal<string>('all');
  personEmail = signal<string>('');

  // Pagination & Sorting
  sortBy = signal('createdAt');
  sortDirection = signal('desc');
  page = signal(0);
  size = signal(10);
  totalElements = signal(0);

  rides = signal<RideHistory[]>([]);
  selectedRide = signal<RideHistory | null>(null);
  isLoading = signal(false);

  private rideService = inject(RideService);
  private toastService = inject(ToastService);

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.loadRideHistory();
  }

  loadRideHistory(): void {
    this.isLoading.set(true);

    const sortParam = `${this.sortBy()},${this.sortDirection()}`;

    this.rideService.getAdminHistory(
      this.personEmail() || undefined,
      this.startDate() || undefined,
      this.endDate() || undefined,
      this.page(),
      this.size(),
      sortParam
    )
      .pipe(
        finalize(() => this.isLoading.set(false))
      )
      .subscribe({
        next: (response: any) => {
          this.rides.set(response.content || response);
          this.totalElements.set(response.totalElements || 0);
        },
        error: (err) => {
          const msg = err.error?.message || 'Error loading history';
          this.toastService.show(msg, 'error');
        }
      });
  }

  onFilterChange(): void {
    this.page.set(0);
    this.loadRideHistory();
  }

  toggleSort(column: string): void {
    if (this.sortBy() === column) {
      this.sortDirection.set(this.sortDirection() === 'asc' ? 'desc' : 'asc');
    } else {
      this.sortBy.set(column);
      this.sortDirection.set('desc');
    }
    this.page.set(0);
    this.loadRideHistory();
  }

  clearFilters(): void {
    this.startDate.set('');
    this.endDate.set('');
    this.personEmail.set('');
    this.statusFilter.set('all');
    this.page.set(0);
    this.loadRideHistory();
  }

  viewRideDetails(ride: RideHistory): void {
    this.selectedRide.set(ride);
  }

  goToReports(): void {
  this.router.navigate(['/admin/reports']);
  }

  protected readonly getStatusClass = getStatusClass;
}
