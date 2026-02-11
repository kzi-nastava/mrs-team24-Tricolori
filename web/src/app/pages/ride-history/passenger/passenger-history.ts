import {Component, OnInit, signal, inject} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { heroEye, heroXMark, heroStar, heroArrowUp, heroArrowDown } from '@ng-icons/heroicons/outline';
import { heroHeartSolid, heroStarSolid } from '@ng-icons/heroicons/solid';
import { RideService } from '../../../services/ride.service';
import {ToastService} from '../../../services/toast.service';
import {PassengerRide} from '../../../model/ride';
import {finalize} from 'rxjs/operators';

@Component({
  selector: 'app-passenger-old-history',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NgIconComponent
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

  getStatusClass(status: string): string {
    switch (status) {
      case 'FINISHED':
        return 'bg-green-100 text-green-700 border-green-200';
      case 'ONGOING':
        return 'bg-blue-100 text-blue-700 border-blue-200';
      case 'SCHEDULED':
        return 'bg-yellow-100 text-yellow-700 border-yellow-200';
      case 'PANIC':
        return 'bg-red-600 text-white border-red-700 animate-pulse';
      case 'CANCELLED_BY_PASSENGER':
      case 'CANCELLED_BY_DRIVER':
      case 'REJECTED':
      case 'DECLINED':
        return 'bg-red-100 text-red-700 border-red-200';
      default:
        return 'bg-gray-100 text-gray-700 border-gray-200';
    }
  }

}
