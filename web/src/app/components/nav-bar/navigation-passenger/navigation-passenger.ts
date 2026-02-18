import {Component, OnInit, OnDestroy, ChangeDetectorRef} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { NotificationService } from '../../../services/notification.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-navigation-passenger',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navigation-passenger.html',
  styleUrl: './navigation-passenger.css'
})
export class NavigationPassenger implements OnInit, OnDestroy {
  unreadCount = 0;
  private unreadSubscription?: Subscription;

  constructor(
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.unreadSubscription = this.notificationService.unreadCount$.subscribe(
      count => {
        this.unreadCount = count;
      }
    );
  }

  ngOnDestroy(): void {
    this.unreadSubscription?.unsubscribe();
    this.cdr.detectChanges();
  }

}
