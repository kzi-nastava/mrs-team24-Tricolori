import { Component, effect, inject, input, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Stop, Route } from '../../../../../../model/route';

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
      pickup: this.createStopGroup(),
      stops: this.fb.array([]),
      destination: this.createStopGroup()
    });

    effect(() => {
      const route = this.selectedRoute();
      if (route) {
        this.routeForm.patchValue({
          pickup: route.pickup,
          destination: route.destination
        }, { emitEvent: false });

        this.populateStops(route.stops || []);
      }
    })
  }

  get pickup() { return this.routeForm.get("pickup")!; }
  get destination() { return this.routeForm.get("destination")!; }
  get stops() { return this.routeForm.get('stops') as FormArray; }

  addStop() {
    const newStopGroup = this.createStopGroup();
    this.stops.push(newStopGroup);
  }

  removeStop(index: number) {
    this.stops.removeAt(index);
  }

  populateStops(stopsData: Stop[]) {
    this.stops.clear();
    stopsData.forEach((stop, index) => {
      this.stops.push(this.createStopGroup(stop));
    });
  }

  getRoute() {
    const formData = this.routeForm.getRawValue();

    const pickupData = formData.pickup;
    const destinationData = formData.destination; 

    // This maps to RideRoute record on backend...
    return {
      pickup: {
        address: pickupData.address,
        location: { 
          longitude: pickupData.location.lng, 
          latitude: pickupData.location.lat 
        }
      },
      destination: {
        address: destinationData.address,
        location: { 
          longitude: destinationData.location.lng, 
          latitude: destinationData.location.lat 
        }
      },
      stops: (formData.stops || []).map((s: any) => ({
        address: s.address,
        location: { 
          longitude: s.location.lng, 
          latitude: s.location.lat 
        }
      }))
    };
  }

  private createStopGroup(stopData?: Stop): FormGroup {
    return this.fb.group({
      address: [stopData?.address || '', Validators.required],
      location: this.fb.group({
        lng: [stopData?.location?.lng || null],
        lat: [stopData?.location?.lat || null]
      })
    });
  }
}
