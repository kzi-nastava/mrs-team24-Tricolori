import { Component } from '@angular/core';

@Component({
  selector: 'app-driver-register',
  imports: [],
  templateUrl: './driver-register.html',
  styleUrl: './driver-register.css',
})

export class DriverRegister {
  step = 1;

  nextStep() {
    // TODO: validate if all fields are filled...
    this.step = 2;
  }

  prevStep() {
    this.step = 1;
  }
}
