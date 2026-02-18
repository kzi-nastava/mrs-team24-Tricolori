import { Component, computed, signal, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
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
  showClearAllDialog = signal<boolean>(false);

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
    private route: ActivatedRoute,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    const userEmail = this.getUserEmail();

    this.loadNotifications();

    this.notificationService.subscribeToNotifications(userEmail);

    // Subscribe to notification updates
    this.notificationsSubscription = this.notificationService.notifications$.subscribe(
      (notifications) => {
        this.notifications.set(this.mapNotifications(notifications));
      }
    );

    // Check if we need to open a specific ride detail from query params
    this.route.queryParams.subscribe(params => {
      const rideId = params['rideId'];
      if (rideId) {
        // Navigate to history page with the ride detail modal
        this.navigateToRideDetail(parseInt(rideId));
      }
    });
  }

  ngOnDestroy(): void {
    // Clean up subscriptions and disconnect WebSocket
    this.notificationsSubscription?.unsubscribe();
    this.unreadCountSubscription?.unsubscribe();
    this.notificationService.unsubscribe();
  }

  private getUserEmail(): string {
    const personData = localStorage.getItem('person_data');
    if (personData) {
      const person = JSON.parse(personData);
      return person.email || '';
    }
    return '';
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
    const typeMap: { [key: string]: string } = {
      'RIDE_STARTING': 'ride_starting',
      'RIDE_CANCELLED': 'ride_cancelled',
      'RIDE_REJECTED': 'ride_rejected',
      'ADDED_TO_RIDE': 'added_to_ride',
      'RIDE_COMPLETED': 'ride_completed',
      'RATING_REMINDER': 'rating_reminder',
      'RIDE_REMINDER': 'ride_reminder',
      'UPCOMING_RIDE_REMINDER': 'upcoming_ride_reminder',
      'RATING_RECEIVED': 'rating_received',
      'RIDE_STARTED': 'ride_started',
      'RIDE_REPORT': 'ride_report',
      'NEW_REGISTRATION': 'new_registration',
      'PROFILE_CHANGE_REQUEST': 'profile_change_request',
      'NEW_CHAT_MESSAGE': 'chat_message',
      'GENERAL': 'general'
    };
    return typeMap[backendType] || 'general';
  }

  private getNotificationTitle(backendType: string): string {
    const titleMap: { [key: string]: string } = {
      'RIDE_STARTING': 'Your ride is starting soon',
      'RIDE_CANCELLED': 'Ride cancelled',
      'RIDE_REJECTED': 'Ride request rejected',
      'ADDED_TO_RIDE': 'You were added to a shared ride',
      'RIDE_COMPLETED': 'Ride completed successfully',
      'RATING_REMINDER': 'Rate your recent ride',
      'RIDE_REMINDER': 'Upcoming ride reminder',
      'UPCOMING_RIDE_REMINDER': 'Upcoming ride reminder',
      'RATING_RECEIVED': 'You received a rating',
      'RIDE_STARTED': 'Ride has started',
      'RIDE_REPORT': 'Ride reported',
      'NEW_REGISTRATION': 'New driver registered',
      'PROFILE_CHANGE_REQUEST': 'Profile change request',
      'NEW_CHAT_MESSAGE': 'New support message',
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
    this.showClearAllDialog.set(true);
  }

  confirmClearAll(): void {
    this.notificationService.deleteAllNotifications().subscribe({
      next: () => {
        this.notificationService.clearAllNotifications();
        this.showClearAllDialog.set(false);
      },
      error: (error) => {
        console.error('Error clearing notifications:', error);
        this.showClearAllDialog.set(false);
      }
    });
  }

  cancelClearAll(): void {
    this.showClearAllDialog.set(false);
  }

  toggleFilter(): void {
    this.showUnreadOnly.update(value => !value);
  }

  handleAction(notification: DisplayNotification): void {
    this.closeModal();

    if (!notification.actionUrl) {
      return;
    }

    // Handle different action types based on notification type
    switch (notification.type) {
      case 'chat_message':
        // Navigate directly to support chat
        this.router.navigate([notification.actionUrl]);
        break;

      case 'rating_reminder':
        // Navigate to rating page
        this.router.navigate([notification.actionUrl]);
        break;

      case 'ride_starting':
      case 'ride_reminder':
      case 'ride_started':
        // Navigate to ride tracking
        this.router.navigate([notification.actionUrl]);
        break;

      case 'ride_completed':
      case 'ride_cancelled':
      case 'added_to_ride':
        // Navigate to history with query param to open ride detail modal
        if (notification.actionUrl.includes('openRide=')) {
          // URL already contains query param
          const url = notification.actionUrl.split('?')[0];
          const params = new URLSearchParams(notification.actionUrl.split('?')[1]);
          this.router.navigate([url], {
            queryParams: Object.fromEntries(params)
          });
        } else if (notification.rideId) {
          // Fallback: construct URL with rideId
          this.router.navigate(['/passenger/history'], {
            queryParams: { openRide: notification.rideId }
          });
        }
        break;

      default:
        // For all other notifications, try to parse the actionUrl
        if (notification.actionUrl.includes('?')) {
          const [path, queryString] = notification.actionUrl.split('?');
          const params = new URLSearchParams(queryString);
          this.router.navigate([path], {
            queryParams: Object.fromEntries(params)
          });
        } else {
          this.router.navigate([notification.actionUrl]);
        }
    }
  }

  getActionButtonText(type: string): string {
    const buttonTextMap: { [key: string]: string } = {
      'chat_message': 'Open Chat',
      'rating_reminder': 'Rate Now',
      'ride_starting': 'Track Ride',
      'ride_reminder': 'Track Ride',
      'ride_started': 'Track Ride',
      'ride_completed': 'View Details',
      'ride_cancelled': 'View Details',
      'added_to_ride': 'View Details',
      'ride_rejected': 'View History',
      'upcoming_ride_reminder': 'View Details',
      'rating_received': 'View Details',
      'ride_report': 'View Report',
      'new_registration': 'View Users',
      'profile_change_request': 'Review Request'
    };
    return buttonTextMap[type] || 'View';
  }

  private navigateToRideDetail(rideId: number): void {
    // Navigate to history page with query parameter to open specific ride
    this.router.navigate(['/passenger/history'], {
      queryParams: { openRide: rideId }
    });
  }

  getNotificationIcon(type: string): string {
    switch (type) {
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
      case 'rating_received':
        return 'heroStar';
      case 'ride_report':
        return 'heroFlag';
      case 'new_registration':
        return 'heroUserCircle';
      case 'profile_change_request':
        return 'heroDocumentText';
      case 'chat_message':
        return 'heroChatBubbleLeftRight';
      case 'general':
      default:
        return 'heroInformationCircle';
    }
  }

  getNotificationIconBg(type: string): string {
    switch (type) {
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
      case 'rating_received':
        return 'bg-amber-100';
      case 'ride_report':
        return 'bg-orange-100';
      case 'new_registration':
        return 'bg-emerald-100';
      case 'profile_change_request':
        return 'bg-sky-100';
      case 'chat_message':
        return 'bg-indigo-100';
      case 'general':
      default:
        return 'bg-gray-100';
    }
  }

  getNotificationIconColor(type: string): string {
    switch (type) {
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
      case 'rating_received':
        return 'text-amber-600';
      case 'ride_report':
        return 'text-orange-600';
      case 'new_registration':
        return 'text-emerald-600';
      case 'profile_change_request':
        return 'text-sky-600';
      case 'chat_message':
        return 'text-indigo-600';
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
