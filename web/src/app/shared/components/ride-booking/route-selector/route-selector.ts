import { Component, effect, inject, input } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FavoriteRouteSelector } from '../favorite-route-selector/favorite-route-selector';
import { Route } from '../../../model/route';

@Component({
  selector: 'app-route-selector',
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './route-selector.html',
  styleUrl: './route-selector.css',
})

export class RouteSelector {
  private fb = inject(FormBuilder);
  routeForm: FormGroup;

  selectedRoute = input<Route>();

  constructor() {
    this.routeForm = this.fb.group({
      pickup: ['', Validators.required],
      stops: this.fb.array([]),
      destination: ['', Validators.required]
    });

    effect(() => {
      const route = this.selectedRoute();
      if (route) {
        this.routeForm.patchValue({
          pickup: route.from,
          destination: route.to
        })

        this.populateStops(route.stops || []);
      }
    })
  }

  get pickup() { return this.routeForm.get("pickup")!; }
  get destination() { return this.routeForm.get("destination")!; }
  get stops() {
    return this.routeForm.get('stops') as FormArray;
  }

  addStop() {
    this.stops.push(this.fb.control('', Validators.required));
  }

  removeStop(index: number) {
    this.stops.removeAt(index);
  }

  populateStops(stopsData: string[]) {
    // Empty any existing stop:
    this.stops.clear();
    
    stopsData.forEach(stopValue => {
      this.stops.push(this.fb.control(stopValue, Validators.required));
    });
  }
}
