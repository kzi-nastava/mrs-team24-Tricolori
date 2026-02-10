import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
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
    private router: Router,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    // Subscribe to unread count updates
    this.unreadSubscription = this.notificationService.unreadCount$.subscribe(
      count => {
        this.unreadCount = count;
      }
    );

    // Load initial unread count
    this.notificationService.getUnreadCount().subscribe({
      next: (count) => {
        this.unreadCount = count;
      },
      error: (error) => {
        console.error('Error loading unread count:', error);
      }
    });
  }

  ngOnDestroy(): void {
    this.unreadSubscription?.unsubscribe();
  }

  onLogout() {
    // Disconnect WebSocket before logout
    this.notificationService.disconnectWebSocket();
    
    localStorage.removeItem('userRole');
    localStorage.removeItem('authToken');
    this.router.navigate(['/login']);
  }
}