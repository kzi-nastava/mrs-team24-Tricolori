import { Component, inject } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

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

  routeForm = this.fb.group({
    pickup: ['Bulevar kralja Petra 3', Validators.required],
    stops: this.fb.array([]),
    destination: ['Laze Teleckog 13', Validators.required]
  });

  get stops() {
    return this.routeForm.get('stops') as FormArray;
  }

  addStop() {
    this.stops.push(this.fb.control('', Validators.required));
  }

  removeStop(index: number) {
    this.stops.removeAt(index);
  }
}
