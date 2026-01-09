import {Component, input, output} from '@angular/core';
import {NgIcon} from '@ng-icons/core';
import {RouterLink} from '@angular/router';
import {EstimateResults, RideOption} from '../../../model/ride-estimation';

@Component({
  selector: 'app-ride-estimation-result',
  imports: [
    NgIcon,
    RouterLink
  ],
  templateUrl: './ride-estimation-result.html',
  styleUrl: './ride-estimation-result.css',
})
export class RideEstimationResult {
  results = input.required<EstimateResults>();
  rideOptions = input.required<RideOption[]>();
  onReset = output<void>();
}
