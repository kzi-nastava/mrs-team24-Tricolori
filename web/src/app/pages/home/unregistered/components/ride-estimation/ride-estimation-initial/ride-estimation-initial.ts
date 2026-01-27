import {Component, input, Input, output} from '@angular/core';
import {NgIcon} from "@ng-icons/core";

@Component({
  selector: 'app-ride-estimation-initial',
    imports: [
        NgIcon
    ],
  templateUrl: './ride-estimation-initial.html',
  styleUrl: './ride-estimation-initial.css',
})
export class RideEstimationInitial {

  availableDrivers = input.required<number>();
  averageRating = input.required<number>();

  onStart = output<void>();

}
