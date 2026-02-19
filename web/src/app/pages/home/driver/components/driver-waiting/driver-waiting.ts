import {Component, OnInit, OnDestroy, inject} from '@angular/core';
import { Router } from '@angular/router';
import { WebSocketService } from '../../../../../services/websocket.service';

@Component({
  selector: 'app-driver-waiting',
  standalone: true,
  templateUrl: './driver-waiting.html'
})
export class DriverWaiting implements OnInit, OnDestroy {

  private readonly rideAssignTopic = '/user/queue/ride-assigned';

  private router = inject(Router);
  private webSocketService = inject(WebSocketService);

  ngOnInit(): void {
    this.webSocketService.subscribe(
      this.rideAssignTopic,
      (rideId: number) => {
        console.log('Ride assigned:', rideId);
        this.router.navigate(['/driver/ride-assign/', rideId]);
      }
    );
  }

  ngOnDestroy(): void {
    this.webSocketService.unsubscribe(this.rideAssignTopic);
  }
}
