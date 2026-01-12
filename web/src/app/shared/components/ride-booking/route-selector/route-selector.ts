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

  // Glavna forma za rutu
  routeForm = this.fb.group({
    pickup: ['Bulevar kralja Petra 3', Validators.required],
    stops: this.fb.array([]), // Dinamički niz za stanice
    destination: ['Laze Teleckog 13', Validators.required]
  });

  get stops() {
    return this.routeForm.get('stops') as FormArray;
  }

  addStop() {
    // Dodajemo novu kontrolu u niz
    this.stops.push(this.fb.control('', Validators.required));
  }

  removeStop(index: number) {
    this.stops.removeAt(index);
  }
}
