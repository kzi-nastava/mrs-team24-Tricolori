import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { NgIcon } from '@ng-icons/core';
import { RidePlanningService } from '../../../../../../services/ride-planning.service';
import { MapService } from '../../../../../../services/map.service';

@Component({
  selector: 'app-ride-estimation-result',
  standalone: true,
  imports: [NgIcon, RouterLink],
  templateUrl: './ride-estimation-result.html'
})
export class RideEstimationResult implements OnInit {
  private planningService = inject(RidePlanningService);
  private mapService = inject(MapService);
  private router = inject(Router);

  results = this.planningService.estimateResults;

  ngOnInit(): void {
    if (!this.results()) {
      this.router.navigate(['/unregistered/form']);
    }
  }

  handleReset(): void {
    this.planningService.clear();
    this.mapService.clearRouteAndMarkers();
    this.mapService.centerMap([45.2671, 19.8335], 13);
    this.router.navigate(['/unregistered/form']);
  }
}
