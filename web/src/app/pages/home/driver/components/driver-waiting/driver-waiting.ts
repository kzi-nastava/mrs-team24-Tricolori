import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-driver-waiting',
  standalone: true,
  templateUrl: './driver-waiting.html'
})
export class DriverWaiting implements OnInit {

  constructor(private router: Router) {}

  ngOnInit(): void {
    // TODO: Replace with real ride assignment logic
    // for testing send to ride assign after 5 seconds
    setTimeout(() => {
      const fakeRideId = 42;
      this.router.navigate(['/driver/home/ride-assign', fakeRideId]);
    }, 5000);
  }
}
