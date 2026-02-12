import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { NotificationService } from '../../../services/notification.service'; 
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-navigation-admin',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navigation-admin.html',
  styleUrl: './navigation-admin.css'
})
export class NavigationAdmin implements OnInit, OnDestroy {
  unreadCount = 0;
  private unreadSubscription?: Subscription;
  private isWebSocketConnected = false;

  constructor(
    private router: Router,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    const userEmail = this.getUserEmail();
    
    if (userEmail) {
      if (!this.isWebSocketConnected) {
        this.notificationService.connectWebSocket(userEmail);
        this.isWebSocketConnected = true;
      }

      // Subscribe to real-time unread count updates
      this.unreadSubscription = this.notificationService.unreadCount$.subscribe(
        count => {
          this.unreadCount = count;
        }
      );

      // Load initial unread count from API
      this.notificationService.getUnreadCount().subscribe({
        next: (count) => {
          this.unreadCount = count;
        },
        error: (error) => {
          console.error('Error loading unread count:', error);
        }
      });
    }
  }

  ngOnDestroy(): void {
    this.unreadSubscription?.unsubscribe();
  }

  private getUserEmail(): string {
    const personData = localStorage.getItem('person_data');
    if (personData) {
      const person = JSON.parse(personData);
      return person.email || '';
    }
    return '';
  }

  onLogout() {
    this.notificationService.disconnectWebSocket();
    this.isWebSocketConnected = false;

  }
}