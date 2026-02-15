import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import {
  heroArrowLeft, heroArrowPath, heroCalculator, heroClock,
  heroCurrencyDollar, heroInformationCircle, heroMapPin, heroSparkles, heroStar
} from '@ng-icons/heroicons/outline';

import { Vehicle } from '../../../model/vehicle.model';
import { MapService } from '../../../services/map.service';
import { VehicleService } from '../../../services/vehicle.service';
import { Map } from '../../../components/map/map';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-unregistered-home',
  standalone: true,
  imports: [CommonModule, Map, RouterOutlet, NgIconComponent],
  templateUrl: './unregistered-home.html',
  viewProviders: [provideIcons({
    heroArrowLeft, heroMapPin, heroCalculator, heroStar,
    heroSparkles, heroInformationCircle, heroCurrencyDollar,
    heroClock, heroArrowPath
  })]
})
export class UnregisteredHome implements OnInit {

  private mapService = inject(MapService);
  private vehicleService = inject(VehicleService);

  vehicles = signal<Vehicle[]>([]);
  loading = signal(false); 

  ngOnInit(): void {
    this.loadVehicles();
  }

  loadVehicles(): void {
    this.loading.set(true); 

    this.vehicleService.getActiveVehicles().subscribe({
      next: (v) => {
        this.vehicles.set(v);
        this.mapService.updateVehicleMarkers(v);
      },
      error: () => {
        this.loading.set(false); 
      },
      complete: () => {
        setTimeout(() => this.loading.set(false), 500);
      }
    });
  }

  availableDrivers = computed(() =>
    this.vehicles().filter(v => v.available).length
  );
}
