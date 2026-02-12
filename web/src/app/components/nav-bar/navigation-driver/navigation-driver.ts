import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { NotificationService } from '../../../services/notification.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-navigation-driver',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navigation-driver.html',
  styleUrl: './navigation-driver.css'
})
export class NavigationDriver implements OnInit, OnDestroy {
  unreadCount = 0;
  private unreadSubscription?: Subscription;

  constructor(
    private notificationService: NotificationService
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
  }

  onLogout() {
  }
}