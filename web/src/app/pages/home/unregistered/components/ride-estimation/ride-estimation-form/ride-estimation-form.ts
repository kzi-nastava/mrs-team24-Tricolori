import {Component, inject, signal} from '@angular/core';
import {NgIcon} from '@ng-icons/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {EstimationService} from '../../../../../../services/estimation.service';
import {RidePlanningService} from '../../../../../../services/ride-planning.service';
import {MapService} from '../../../../../../services/map.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-ride-estimation-form',
  imports: [
    NgIcon,
    ReactiveFormsModule
  ],
  templateUrl: './ride-estimation-form.html'
})
export class RideEstimationForm {
  private estimationService = inject(EstimationService);
  private planningService = inject(RidePlanningService);
  private mapService = inject(MapService);
  private router = inject(Router);

  isLoading = signal(false);
  errorMessage = signal<string | null>(null);

  estimateForm = new FormGroup({
    pickup: new FormControl('', { validators: [Validators.required], nonNullable: true }),
    destination: new FormControl('', { validators: [Validators.required], nonNullable: true })
  });

  onSubmit() {
    if (this.estimateForm.invalid) return;

    this.isLoading.set(true);
    this.errorMessage.set(null);
    const { pickup, destination } = this.estimateForm.getRawValue();

    this.estimationService.calculateRouteFromAddresses([pickup, destination]).subscribe({
      next: (result) => {
        if (result) {
          this.planningService.setResults({
            pickup,
            destination,
            distance: result.distance,
            duration: result.duration,
            routeGeometry: result.routeGeometry
          });

          this.mapService.drawRoute(result.routeGeometry);
          this.router.navigate(['/unregistered/result']);
        } else {
          this.errorMessage.set('Could not find route or addresses. Please be more specific.');
          // TODO: show Toast and remove error message
        }
      },
      error: (err) => {
        this.errorMessage.set('A server error occurred');
        // TODO: show Toast and remove error message
        console.error(err);
      },
      complete: () => {
        this.isLoading.set(false);
      }
    });

  }
}
