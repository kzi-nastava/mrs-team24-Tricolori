import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-driver-waiting',
  standalone: true,
  templateUrl: './driver-waiting.html'
})
export class DriverWaiting implements OnInit {

  // TODO: remove content of this class when web sockets are introduced in project

  constructor(private router: Router) {}

  ngOnInit(): void {
    setTimeout(() => {
      const fakeRideId = 42;
      this.router.navigate(['/driver/home/ride-assign', fakeRideId]);
    }, 5000000);
  }

}
