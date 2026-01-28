import { Component, AfterViewInit, OnDestroy, inject, input, effect } from '@angular/core';
import { MapService } from '../../services/map.service';
import { Vehicle } from '../../model/vehicle.model';

@Component({
  selector: 'app-map',
  standalone: true,
  templateUrl: './map.html',
  styleUrl: './map.css',
})
export class Map implements AfterViewInit, OnDestroy {
  private mapService = inject(MapService);

  // Signal-based input for reactive data flow
  vehicles = input<Vehicle[]>([]);

  constructor() {
    /**
     * Effect tracks changes to the vehicles signal.
     * Whenever the list updates, we notify the service to redraw markers.
     */
    effect(() => {
      const currentVehicles = this.vehicles();
      this.refreshMarkers(currentVehicles);
    });
  }

  ngAfterViewInit(): void {
    // Initialize the Leaflet instance on the 'map' div
    this.mapService.initMap('map');

    // If vehicles were already provided before init, render them now
    if (this.vehicles().length > 0) {
      this.refreshMarkers(this.vehicles());
    }
  }

  ngOnDestroy(): void {
    // Clean up Leaflet instance to prevent memory leaks or "container already init" errors
    this.mapService.destroyMap();
  }

  /**
   * Passes the vehicle list to the service for rendering
   */
  private refreshMarkers(vehicles: Vehicle[]): void {
    // Check if map is ready before attempting to draw
    if (this.mapService.getMap()) {
      this.mapService.updateVehicleMarkers(vehicles);
    }
  }
}
