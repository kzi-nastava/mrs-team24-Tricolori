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
  heroMapPin,
  heroTruck,
  heroChatBubbleLeftRight,
  heroDocumentText,
  heroFlag
} from '@ng-icons/heroicons/outline';
import { NotificationService, NotificationDto } from '../../../services/notification.service';

interface EnrichedNotification extends NotificationDto {
  title: string;
  isPanic?: boolean;
  acknowledged?: boolean;
}

@Component({
  selector: 'app-admin-notifications',
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
      heroMapPin,
      heroTruck,
      heroChatBubbleLeftRight,
      heroDocumentText,
      heroFlag
    })
  ],
  templateUrl: './admin-notifications.html',
  styleUrls: ['./admin-notifications.css']
})
export class AdminNotificationsComponent implements OnInit, OnDestroy {
  selectedNotification: EnrichedNotification | null = null;
  showUnreadOnly = signal<boolean>(false);
  showClearAllDialog = signal<boolean>(false);
  private audioContext: AudioContext | null = null;
  private lastPanicCount = 0;
  
  notifications = signal<EnrichedNotification[]>([]);
  isLoading = signal<boolean>(true);

  // Computed panic notifications (will be separate - placeholder for now)
  panicNotifications = computed(() => {
    const filtered = this.showUnreadOnly() 
      ? this.notifications().filter(n => n.isPanic && !n.opened)
      : this.notifications().filter(n => n.isPanic);
    return filtered;
  });

  // Computed regular notifications
  regularNotifications = computed(() => {
    const filtered = this.showUnreadOnly()
      ? this.notifications().filter(n => !n.isPanic && !n.opened)
      : this.notifications().filter(n => !n.isPanic);
    return filtered;
  });

  constructor(
    private router: Router,
    private notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.loadNotifications();
    this.setupWebSocket();
    this.subscribeToNotificationUpdates();
    
    this.lastPanicCount = this.panicCount();
    
    // Check for new panic notifications periodically
    setInterval(() => {
      const currentPanicCount = this.panicCount();
      if (currentPanicCount > this.lastPanicCount) {
        this.playPanicSound();
      }
      this.lastPanicCount = currentPanicCount;
    }, 1000);
  }

  ngOnDestroy(): void {
    this.notificationService.disconnectWebSocket();
    if (this.audioContext) {
      this.audioContext.close();
    }
  }

  // ==================== DATA LOADING ====================

  loadNotifications(): void {
    this.isLoading.set(true);
    this.notificationService.getAllNotifications().subscribe({
      next: (notifications) => {
        const enriched = notifications.map(n => this.enrichNotification(n));
        this.notifications.set(enriched);
        this.notificationService.setNotifications(notifications);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Failed to load notifications:', error);
        this.isLoading.set(false);
      }
    });
  }

  private getUserEmail(): string {
      const personData = localStorage.getItem('person_data');
      if (personData) {
        const person = JSON.parse(personData);
        return person.email || '';
      }
      return '';
    }

  setupWebSocket(): void {
    const userEmail = this.getUserEmail(); 
    if (userEmail) {
      this.notificationService.connectWebSocket(userEmail);
    }
  }

  subscribeToNotificationUpdates(): void {
    // Listen for real-time notifications from WebSocket
    this.notificationService.notifications$.subscribe({
      next: (notifications) => {
        const enriched = notifications.map(n => this.enrichNotification(n));
        this.notifications.set(enriched);
      }
    });
  }

  // ==================== NOTIFICATION ENRICHMENT ====================

  enrichNotification(notification: NotificationDto): EnrichedNotification {
    return {
      ...notification,
      title: this.getNotificationTitle(notification),
      isPanic: false // Panics will be handled separately
    };
  }

  getNotificationTitle(notification: NotificationDto): string {
    switch (notification.type) {
      case 'RIDE_REPORT':
        return 'Ride Issue Reported';
      case 'NEW_REGISTRATION':
        return 'New Driver Registration';
      case 'PROFILE_CHANGE_REQUEST':
        return 'Profile Change Request';
      case 'NEW_CHAT_MESSAGE':
        return 'New Support Message';
      case 'RIDE_STARTING':
        return 'Ride Starting Soon';
      case 'RIDE_CANCELLED':
        return 'Ride Cancelled';
      case 'RIDE_REJECTED':
        return 'Ride Rejected';
      case 'ADDED_TO_RIDE':
        return 'Added to Shared Ride';
      case 'RIDE_COMPLETED':
        return 'Ride Completed';
      case 'RATING_REMINDER':
        return 'Rating Reminder';
      case 'RIDE_REMINDER':
        return 'Upcoming Ride Reminder';
      case 'UPCOMING_RIDE_REMINDER':
        return 'Upcoming Ride Reminder';
      case 'RATING_RECEIVED':
        return 'New Rating Received';
      case 'RIDE_STARTED':
        return 'Ride Started';
      default:
        return 'Notification';
    }
  }

  // ==================== COMPUTED VALUES ====================

  panicCount(): number {
    return this.notifications().filter(n => n.isPanic && !n.acknowledged).length;
  }

  unreadCount(): number {
    return this.notifications().filter(n => !n.opened).length;
  }

  readCount(): number {
    return this.notifications().filter(n => n.opened).length;
  }

  // ==================== USER ACTIONS ====================

  openNotification(notification: EnrichedNotification): void {
    if (!notification.opened) {
      this.notificationService.markAsRead(notification.id).subscribe({
        next: () => {
          this.notificationService.updateNotificationAsRead(notification.id);
          this.notifications.update(notifications => 
            notifications.map(n => n.id === notification.id ? { ...n, opened: true } : n)
          );
        },
        error: (error) => {
          console.error('Failed to mark notification as read:', error);
        }
      });
    }
    this.selectedNotification = { ...notification, opened: true };
    
    // Navigate to action URL if present
    if (notification.actionUrl && notification.actionUrl !== '#') {
      // Optional: Auto-navigate or show link in modal
    }
  }

  closeModal(): void {
    this.selectedNotification = null;
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notificationService.updateAllNotificationsAsRead();
        this.notifications.update(notifications =>
          notifications.map(n => ({ ...n, opened: true }))
        );
      },
      error: (error) => {
        console.error('Failed to mark all as read:', error);
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
        this.notifications.set([]);
        this.showClearAllDialog.set(false);
      },
      error: (error) => {
        console.error('Failed to delete all notifications:', error);
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

  acknowledgePanic(event: Event, notification: EnrichedNotification): void {
    event.stopPropagation();
    // This will be implemented when panic functionality is added
    this.notifications.update(notifications =>
      notifications.map(n => 
        n.id === notification.id ? { ...n, acknowledged: true, opened: true } : n
      )
    );
  }

  acknowledgePanicFromModal(notification: EnrichedNotification): void {
    // This will be implemented when panic functionality is added
    this.notifications.update(notifications =>
      notifications.map(n => 
        n.id === notification.id ? { ...n, acknowledged: true, opened: true } : n
      )
    );
    this.selectedNotification = { ...notification, acknowledged: true, opened: true };
  }

  getActionButtonText(type: string): string {
    const buttonTextMap: { [key: string]: string } = {
      'NEW_CHAT_MESSAGE': 'Open Chat',
      'RIDE_REPORT': 'View Report',
      'NEW_REGISTRATION': 'View Details',
      'PROFILE_CHANGE_REQUEST': 'Review Request',
      'RIDE_STARTING': 'Track Ride',
      'RIDE_CANCELLED': 'View Details',
      'RIDE_REJECTED': 'View History',
      'ADDED_TO_RIDE': 'View Details',
      'RIDE_COMPLETED': 'View Details',
      'RATING_REMINDER': 'Rate Now',
      'RIDE_REMINDER': 'Track Ride',
      'UPCOMING_RIDE_REMINDER': 'View Details',
      'RATING_RECEIVED': 'View Details',
      'RIDE_STARTED': 'Track Ride'
    };
    return buttonTextMap[type] || 'View Details';
  }

  // ==================== HELPER METHODS ====================

  playPanicSound(): void {
    // Create audio context and play emergency sound
    if (!this.audioContext) {
      this.audioContext = new (window.AudioContext || (window as any).webkitAudioContext)();
    }

    const oscillator = this.audioContext.createOscillator();
    const gainNode = this.audioContext.createGain();

    oscillator.connect(gainNode);
    gainNode.connect(this.audioContext.destination);

    // Create siren-like sound
    oscillator.frequency.setValueAtTime(800, this.audioContext.currentTime);
    oscillator.frequency.exponentialRampToValueAtTime(400, this.audioContext.currentTime + 0.5);
    oscillator.frequency.exponentialRampToValueAtTime(800, this.audioContext.currentTime + 1);

    gainNode.gain.setValueAtTime(0.3, this.audioContext.currentTime);
    gainNode.gain.exponentialRampToValueAtTime(0.01, this.audioContext.currentTime + 1);

    oscillator.start(this.audioContext.currentTime);
    oscillator.stop(this.audioContext.currentTime + 1);

    // Play multiple times for urgency
    setTimeout(() => {
      const osc2 = this.audioContext!.createOscillator();
      const gain2 = this.audioContext!.createGain();
      osc2.connect(gain2);
      gain2.connect(this.audioContext!.destination);
      osc2.frequency.setValueAtTime(800, this.audioContext!.currentTime);
      gain2.gain.setValueAtTime(0.3, this.audioContext!.currentTime);
      gain2.gain.exponentialRampToValueAtTime(0.01, this.audioContext!.currentTime + 0.5);
      osc2.start(this.audioContext!.currentTime);
      osc2.stop(this.audioContext!.currentTime + 0.5);
    }, 1200);
  }

  getNotificationIcon(type: string): string {
    switch (type) {
      case 'RIDE_REPORT':
        return 'heroExclamationTriangle';
      case 'NEW_REGISTRATION':
        return 'heroUserPlus';
      case 'PROFILE_CHANGE_REQUEST':
        return 'heroDocumentText';
      case 'NEW_CHAT_MESSAGE':
        return 'heroChatBubbleLeftRight';
      case 'RIDE_CANCELLED':
        return 'heroXCircle';
      case 'RIDE_REJECTED':
        return 'heroXCircle';
      case 'RIDE_COMPLETED':
        return 'heroCheckCircle';
      case 'RATING_RECEIVED':
        return 'heroCheckCircle';
      case 'RIDE_STARTING':
      case 'RIDE_STARTED':
        return 'heroFlag';
      case 'ADDED_TO_RIDE':
      case 'RIDE_REMINDER':
      case 'UPCOMING_RIDE_REMINDER':
        return 'heroClock';
      case 'RATING_REMINDER':
        return 'heroInformationCircle';
      default:
        return 'heroInformationCircle';
    }
  }

  getNotificationIconBg(type: string): string {
    switch (type) {
      case 'RIDE_REPORT':
        return 'bg-orange-100';
      case 'NEW_REGISTRATION':
        return 'bg-green-100';
      case 'PROFILE_CHANGE_REQUEST':
        return 'bg-blue-100';
      case 'NEW_CHAT_MESSAGE':
        return 'bg-purple-100';
      case 'RIDE_CANCELLED':
      case 'RIDE_REJECTED':
        return 'bg-red-100';
      case 'RIDE_COMPLETED':
      case 'RATING_RECEIVED':
        return 'bg-green-100';
      case 'RIDE_STARTING':
      case 'RIDE_STARTED':
        return 'bg-teal-100';
      case 'ADDED_TO_RIDE':
      case 'RIDE_REMINDER':
      case 'UPCOMING_RIDE_REMINDER':
        return 'bg-yellow-100';
      case 'RATING_REMINDER':
        return 'bg-blue-100';
      default:
        return 'bg-gray-100';
    }
  }

  getNotificationIconColor(type: string): string {
    switch (type) {
      case 'RIDE_REPORT':
        return 'text-orange-600';
      case 'NEW_REGISTRATION':
        return 'text-green-600';
      case 'PROFILE_CHANGE_REQUEST':
        return 'text-blue-600';
      case 'NEW_CHAT_MESSAGE':
        return 'text-purple-600';
      case 'RIDE_CANCELLED':
      case 'RIDE_REJECTED':
        return 'text-red-600';
      case 'RIDE_COMPLETED':
      case 'RATING_RECEIVED':
        return 'text-green-600';
      case 'RIDE_STARTING':
      case 'RIDE_STARTED':
        return 'text-teal-600';
      case 'ADDED_TO_RIDE':
      case 'RIDE_REMINDER':
      case 'UPCOMING_RIDE_REMINDER':
        return 'text-yellow-600';
      case 'RATING_REMINDER':
        return 'text-blue-600';
      default:
        return 'text-gray-600';
    }
  }

  getTimeAgo(timestamp: string): string {
    const now = new Date();
    const notificationDate = new Date(timestamp);
    const diff = now.getTime() - notificationDate.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    if (days < 7) return `${days}d ago`;
    
    return notificationDate.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }

  formatFullDate(timestamp: string): string {
    const date = new Date(timestamp);
    return date.toLocaleString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}