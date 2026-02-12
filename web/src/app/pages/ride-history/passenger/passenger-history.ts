import {Component, OnInit, signal, inject} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { heroEye, heroXMark, heroStar, heroArrowUp, heroArrowDown } from '@ng-icons/heroicons/outline';
import { heroHeartSolid, heroStarSolid } from '@ng-icons/heroicons/solid';
import { RideService } from '../../../services/ride.service';
import {ToastService} from '../../../services/toast.service';
import {getStatusClass, PassengerRide} from '../../../model/ride';
import {finalize} from 'rxjs/operators';
import {RideDetailsModal} from './components/ride-details-modal/ride-details-modal';

@Component({
  selector: 'app-passenger-old-history',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NgIconComponent,
    RideDetailsModal
  ],
  providers: [
    provideIcons({ heroEye, heroXMark, heroStar, heroStarSolid, heroHeartSolid,
      heroArrowUp, heroArrowDown})
  ],
  templateUrl: './passenger-history.html'
})
export class PassengerHistory implements OnInit {
  // Filters & State
  startDate = signal<string>('');
  endDate = signal<string>('');
  statusFilter = signal<string>('all');
  searchQuery = signal<string>('');

  // Pagination & Sorting
  sortBy = signal('createdAt');
  sortDirection = signal('desc');
  page = signal(0);
  size = signal(10);
  totalElements = signal(0);

  rides = signal<PassengerRide[]>([]);
  selectedRide = signal<PassengerRide | null>(null);
  isLoading = signal(false);

  private rideService = inject(RideService);
  private toastService = inject(ToastService);

  ngOnInit(): void {
    this.loadRideHistory();
  }

  loadRideHistory(): void {
    this.isLoading.set(true);

    const sortParam = `${this.sortBy()},${this.sortDirection()}`;

    this.rideService.getPassengerHistory(
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
    this.statusFilter.set('all');
    this.searchQuery.set('');
    this.page.set(0);
    this.loadRideHistory();
  }

  viewRideDetails(ride: PassengerRide): void {
    this.selectedRide.set(ride);
  }

  protected readonly getStatusClass = getStatusClass;
}
