import { Injectable, signal } from '@angular/core';
import * as L from 'leaflet';

export interface PlanningState {
  pickup: string;
  destination: string;
  distance: number;
  duration: number;
  routeGeometry: L.LatLng[];
}

@Injectable({ providedIn: 'root' })
export class RidePlanningService {

  estimateResults = signal<PlanningState | null>(null);

  setResults(data: PlanningState) {
    this.estimateResults.set(data);
  }

  clear() {
    this.estimateResults.set(null);
  }
}
