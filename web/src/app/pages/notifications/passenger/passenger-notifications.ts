import { Component, computed, signal, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { 
  heroBell, 
  heroXMark, 
  heroClock,
  heroXCircle,
  heroUserPlus,
  heroCheckCircle,
  heroExclamationTriangle,
  heroInformationCircle,
  heroTicket,
  heroArrowRight,
  heroFunnel,
  heroStar,
  heroChatBubbleLeftRight,
  heroDocumentText,
  heroUserCircle,
  heroFlag
} from '@ng-icons/heroicons/outline';
import { NotificationService, NotificationDto } from '../../../services/notification.service';
import { Subscription } from 'rxjs';

interface DisplayNotification {
  id: number;
  type: string;
  title: string;
  body: string;
  timestamp: Date;
  isRead: boolean;
  rideId?: number;
  actionUrl?: string;
  driverName?: string;
  passengerName?: string;
}

@Component({
  selector: 'app-passenger-notifications',
  standalone: true,
  imports: [CommonModule, NgIconComponent],
  providers: [
    provideIcons({ 
      heroBell, 
      heroXMark, 
      heroClock, 
      heroXCircle, 
      heroUserPlus,
      heroCheckCircle,
      heroExclamationTriangle,
      heroInformationCircle,
      heroTicket,
      heroArrowRight,
      heroFunnel,
      heroStar,
      heroChatBubbleLeftRight,
      heroDocumentText,
      heroUserCircle,
      heroFlag
    })
  ],
  templateUrl: './passenger-notifications.html',
  styleUrls: ['./passenger-notifications.css']
})
export class PassengerNotificationsComponent implements OnInit, OnDestroy {
  selectedNotification: DisplayNotification | null = null;
  showUnreadOnly = signal<boolean>(false);
  notifications = signal<DisplayNotification[]>([]);
  
  private notificationsSubscription?: Subscription;
  private unreadCountSubscription?: Subscription;

  filteredNotifications = computed(() => {
    if (this.showUnreadOnly()) {
      return this.notifications().filter(n => !n.isRead);
    }
    return this.notifications();
  });

  constructor(
    private router: Router,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    // Get user email from auth service or localStorage
    const userEmail = this.getUserEmail(); // You'll need to implement this
    
    // Load initial notifications
    this.loadNotifications();
    
    // Connect to WebSocket for real-time updates
    this.notificationService.connectWebSocket(userEmail);
    
    // Subscribe to notification updates
    this.notificationsSubscription = this.notificationService.notifications$.subscribe(
      (notifications) => {
        this.notifications.set(this.mapNotifications(notifications));
      }
    );
  }

  ngOnDestroy(): void {
    // Clean up subscriptions and disconnect WebSocket
    this.notificationsSubscription?.unsubscribe();
    this.unreadCountSubscription?.unsubscribe();
    this.notificationService.disconnectWebSocket();
  }

  private getUserEmail(): string {
    // TODO: Replace with your actual auth service
    // Example: return this.authService.getCurrentUser()?.email || '';
    return localStorage.getItem('userEmail') || '';
  }

  private loadNotifications(): void {
    this.notificationService.getAllNotifications().subscribe({
      next: (notifications) => {
        this.notificationService.setNotifications(notifications);
      },
      error: (error) => {
        console.error('Error loading notifications:', error);
      }
    });
  }

  private mapNotifications(dtos: NotificationDto[]): DisplayNotification[] {
    return dtos.map(dto => ({
      id: dto.id,
      type: this.mapNotificationType(dto.type),
      title: this.getNotificationTitle(dto.type),
      body: dto.content,
      timestamp: new Date(dto.time),
      isRead: dto.opened,
      rideId: dto.rideId,
      actionUrl: dto.actionUrl,
      driverName: dto.driverName,
      passengerName: dto.passengerName
    }));
  }

  private mapNotificationType(backendType: string): string {
    // Map backend NotificationType enum to frontend display types
    const typeMap: { [key: string]: string } = {
      // Passenger notifications
      'RIDE_STARTING': 'ride_starting',
      'RIDE_CANCELLED': 'ride_cancelled',
      'RIDE_REJECTED': 'ride_rejected',
      'ADDED_TO_RIDE': 'added_to_ride',
      'RIDE_COMPLETED': 'ride_completed',
      'RATING_REMINDER': 'rating_reminder',
      'RIDE_REMINDER': 'ride_reminder',
      
      // Driver notifications
      'UPCOMING_RIDE_REMINDER': 'upcoming_ride_reminder',
      'RATING_RECEIVED': 'rating_received',
      'RIDE_STARTED': 'ride_started',
      
      // Admin notifications
      'RIDE_REPORT': 'ride_report',
      'NEW_REGISTRATION': 'new_registration',
      'PROFILE_CHANGE_REQUEST': 'profile_change_request',
      
      // Chat
      'NEW_CHAT_MESSAGE': 'chat_message',
      
      // General
      'GENERAL': 'general'
    };
    return typeMap[backendType] || 'general';
  }

  private getNotificationTitle(backendType: string): string {
    const titleMap: { [key: string]: string } = {
      // Passenger notifications
      'RIDE_STARTING': 'Your ride is starting soon',
      'RIDE_CANCELLED': 'Ride cancelled',
      'RIDE_REJECTED': 'Ride request rejected',
      'ADDED_TO_RIDE': 'You were added to a shared ride',
      'RIDE_COMPLETED': 'Ride completed successfully',
      'RATING_REMINDER': 'Rate your recent ride',
      'RIDE_REMINDER': 'Upcoming ride reminder',
      
      // Driver notifications
      'UPCOMING_RIDE_REMINDER': 'Upcoming ride reminder',
      'RATING_RECEIVED': 'You received a rating',
      'RIDE_STARTED': 'Ride has started',
      
      // Admin notifications
      'RIDE_REPORT': 'Ride reported',
      'NEW_REGISTRATION': 'New driver registered',
      'PROFILE_CHANGE_REQUEST': 'Profile change request',
      
      // Chat
      'NEW_CHAT_MESSAGE': 'New support message',
      
      // General
      'GENERAL': 'Notification'
    };
    return titleMap[backendType] || 'Notification';
  }

  unreadCount(): number {
    return this.notifications().filter(n => !n.isRead).length;
  }

  readCount(): number {
    return this.notifications().filter(n => n.isRead).length;
  }

  openNotification(notification: DisplayNotification): void {
    if (!notification.isRead) {
      this.notificationService.markAsRead(notification.id).subscribe({
        next: () => {
          this.notificationService.updateNotificationAsRead(notification.id);
        },
        error: (error) => {
          console.error('Error marking notification as read:', error);
        }
      });
    }
    this.selectedNotification = notification;
  }

  closeModal(): void {
    this.selectedNotification = null;
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notificationService.updateAllNotificationsAsRead();
      },
      error: (error) => {
        console.error('Error marking all as read:', error);
      }
    });
  }

  clearAllNotifications(): void {
    if (confirm('Are you sure you want to clear all notifications? This action cannot be undone.')) {
      this.notificationService.deleteAllNotifications().subscribe({
        next: () => {
          this.notificationService.clearAllNotifications();
        },
        error: (error) => {
          console.error('Error clearing notifications:', error);
        }
      });
    }
  }

  toggleFilter(): void {
    this.showUnreadOnly.update(value => !value);
  }

  handleAction(notification: DisplayNotification): void {
    if (notification.actionUrl) {
      this.router.navigate([notification.actionUrl]);
    }
    this.closeModal();
  }

  getNotificationIcon(type: string): string {
    switch (type) {
      // Passenger notifications
      case 'ride_starting':
      case 'ride_reminder':
      case 'upcoming_ride_reminder':
      case 'ride_started':
        return 'heroClock';
      case 'ride_cancelled':
      case 'ride_rejected':
        return 'heroXCircle';
      case 'added_to_ride':
        return 'heroUserPlus';
      case 'ride_completed':
        return 'heroCheckCircle';
      case 'rating_reminder':
        return 'heroBell';
      
      // Driver notifications
      case 'rating_received':
        return 'heroStar';
      
      // Admin notifications
      case 'ride_report':
        return 'heroFlag';
      case 'new_registration':
        return 'heroUserCircle';
      case 'profile_change_request':
        return 'heroDocumentText';
      
      // Chat
      case 'chat_message':
        return 'heroChatBubbleLeftRight';
      
      // General
      case 'general':
      default:
        return 'heroInformationCircle';
    }
  }

  getNotificationIconBg(type: string): string {
    switch (type) {
      // Passenger notifications
      case 'ride_starting':
      case 'ride_reminder':
      case 'upcoming_ride_reminder':
      case 'ride_started':
        return 'bg-blue-100';
      case 'ride_cancelled':
      case 'ride_rejected':
        return 'bg-red-100';
      case 'added_to_ride':
        return 'bg-purple-100';
      case 'ride_completed':
        return 'bg-green-100';
      case 'rating_reminder':
        return 'bg-yellow-100';
      
      // Driver notifications
      case 'rating_received':
        return 'bg-amber-100';
      
      // Admin notifications
      case 'ride_report':
        return 'bg-orange-100';
      case 'new_registration':
        return 'bg-emerald-100';
      case 'profile_change_request':
        return 'bg-sky-100';
      
      // Chat
      case 'chat_message':
        return 'bg-indigo-100';
      
      // General
      case 'general':
      default:
        return 'bg-gray-100';
    }
  }

  getNotificationIconColor(type: string): string {
    switch (type) {
      // Passenger notifications
      case 'ride_starting':
      case 'ride_reminder':
      case 'upcoming_ride_reminder':
      case 'ride_started':
        return 'text-blue-600';
      case 'ride_cancelled':
      case 'ride_rejected':
        return 'text-red-600';
      case 'added_to_ride':
        return 'text-purple-600';
      case 'ride_completed':
        return 'text-green-600';
      case 'rating_reminder':
        return 'text-yellow-600';
      
      // Driver notifications
      case 'rating_received':
        return 'text-amber-600';
      
      // Admin notifications
      case 'ride_report':
        return 'text-orange-600';
      case 'new_registration':
        return 'text-emerald-600';
      case 'profile_change_request':
        return 'text-sky-600';
      
      // Chat
      case 'chat_message':
        return 'text-indigo-600';
      
      // General
      case 'general':
      default:
        return 'text-gray-600';
    }
  }

  getTimeAgo(timestamp: Date): string {
    const now = new Date();
    const diff = now.getTime() - timestamp.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    if (days < 7) return `${days}d ago`;
    
    return timestamp.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }

  formatFullDate(timestamp: Date): string {
    return timestamp.toLocaleString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}