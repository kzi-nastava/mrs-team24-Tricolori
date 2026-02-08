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
  heroTruck
} from '@ng-icons/heroicons/outline';

interface Notification {
  id: number;
  type: 'panic' | 'ride_report' | 'payment_issue' | 'driver_issue' | 'system_alert' | 'new_registration';
  title: string;
  body: string;
  timestamp: Date;
  isRead: boolean;
  rideId?: number;
  vehicleId?: string;
  driverName?: string;
  passengerName?: string;
  location?: string;
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
      heroTruck
    })
  ],
  templateUrl: './admin-notifications.html',
  styleUrls: ['./admin-notifications.css']
})
export class AdminNotificationsComponent implements OnInit, OnDestroy {
  selectedNotification: Notification | null = null;
  showUnreadOnly = signal<boolean>(false);
  private audioContext: AudioContext | null = null;
  private lastPanicCount = 0;
  
  notifications = signal<Notification[]>([
    {
      id: 1,
      type: 'panic',
      title: '🚨 PANIC BUTTON ACTIVATED - Passenger Emergency',
      body: 'Passenger Ana Jovanović activated the panic button. Immediate assistance required. Last known location: Bulevar Oslobođenja 45.',
      timestamp: new Date(Date.now() - 2 * 60 * 1000), // 2 minutes ago
      isRead: false,
      rideId: 12345,
      vehicleId: 'NS-456-CD',
      driverName: 'Marko Petrović',
      passengerName: 'Ana Jovanović',
      location: 'Bulevar Oslobođenja 45, Novi Sad',
      acknowledged: false
    },
    {
      id: 2,
      type: 'panic',
      title: '🚨 PANIC BUTTON ACTIVATED - Driver Emergency',
      body: 'Driver Stefan Nikolić activated the panic button. Emergency response needed. Vehicle stopped at last known location.',
      timestamp: new Date(Date.now() - 15 * 60 * 1000), // 15 minutes ago
      isRead: false,
      rideId: 12344,
      vehicleId: 'BG-789-EF',
      driverName: 'Stefan Nikolić',
      passengerName: 'Milica Stojanović',
      location: 'Kisačka 71, Novi Sad',
      acknowledged: false
    },
    {
      id: 3,
      type: 'ride_report',
      title: 'Route Inconsistency Reported',
      body: 'Passenger reported unusual route deviation on ride #12343. Driver took a detour through residential area.',
      timestamp: new Date(Date.now() - 30 * 60 * 1000),
      isRead: false,
      rideId: 12343
    },
    {
      id: 4,
      type: 'payment_issue',
      title: 'Payment Processing Failed',
      body: 'Payment of 450.00 RSD failed for ride #12342. Card was declined. User notified.',
      timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000),
      isRead: true,
      rideId: 12342
    },
    {
      id: 5,
      type: 'driver_issue',
      title: 'Driver Rating Drop Alert',
      body: 'Driver Jelena Đorđević received multiple low ratings (2.5/5 average) in the last 24 hours. Review recommended.',
      timestamp: new Date(Date.now() - 5 * 60 * 60 * 1000),
      isRead: true
    },
    {
      id: 6,
      type: 'system_alert',
      title: 'High Demand Period Detected',
      body: 'Current ride requests have increased by 150% in the city center area. Consider surge pricing activation.',
      timestamp: new Date(Date.now() - 24 * 60 * 60 * 1000),
      isRead: true
    },
    {
      id: 7,
      type: 'new_registration',
      title: 'New Driver Registration',
      body: 'New driver Milan Jovanović submitted registration documents for review. Background check pending.',
      timestamp: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000),
      isRead: true
    }
  ]);

  panicNotifications = computed(() => {
    const filtered = this.showUnreadOnly() 
      ? this.notifications().filter(n => n.type === 'panic' && !n.isRead)
      : this.notifications().filter(n => n.type === 'panic');
    return filtered;
  });

  regularNotifications = computed(() => {
    const filtered = this.showUnreadOnly()
      ? this.notifications().filter(n => n.type !== 'panic' && !n.isRead)
      : this.notifications().filter(n => n.type !== 'panic');
    return filtered;
  });

  constructor(private router: Router) {}

  ngOnInit(): void {
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
    if (this.audioContext) {
      this.audioContext.close();
    }
  }

  panicCount(): number {
    return this.notifications().filter(n => n.type === 'panic' && !n.acknowledged).length;
  }

  unreadCount(): number {
    return this.notifications().filter(n => !n.isRead).length;
  }

  readCount(): number {
    return this.notifications().filter(n => n.isRead).length;
  }

  openNotification(notification: Notification): void {
    if (!notification.isRead) {
      this.notifications.update(notifications => 
        notifications.map(n => n.id === notification.id ? { ...n, isRead: true } : n)
      );
    }
    this.selectedNotification = notification;
  }

  closeModal(): void {
    this.selectedNotification = null;
  }

  markAllAsRead(): void {
    this.notifications.update(notifications =>
      notifications.map(n => ({ ...n, isRead: true }))
    );
  }

  clearAllNotifications(): void {
    if (confirm('Are you sure you want to clear all notifications? This action cannot be undone.')) {
      this.notifications.set([]);
    }
  }

  toggleFilter(): void {
    this.showUnreadOnly.update(value => !value);
  }

  acknowledgePanic(event: Event, notification: Notification): void {
    event.stopPropagation();
    this.notifications.update(notifications =>
      notifications.map(n => 
        n.id === notification.id ? { ...n, acknowledged: true, isRead: true } : n
      )
    );
  }

  acknowledgePanicFromModal(notification: Notification): void {
    this.notifications.update(notifications =>
      notifications.map(n => 
        n.id === notification.id ? { ...n, acknowledged: true, isRead: true } : n
      )
    );
    this.selectedNotification = { ...notification, acknowledged: true, isRead: true };
  }

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
      case 'panic':
        return 'heroExclamationTriangle';
      case 'ride_report':
        return 'heroExclamationTriangle';
      case 'payment_issue':
        return 'heroXCircle';
      case 'driver_issue':
        return 'heroUserPlus';
      case 'system_alert':
        return 'heroInformationCircle';
      case 'new_registration':
        return 'heroCheckCircle';
      default:
        return 'heroInformationCircle';
    }
  }

  getNotificationIconBg(type: string): string {
    switch (type) {
      case 'panic':
        return 'bg-red-600';
      case 'ride_report':
        return 'bg-orange-100';
      case 'payment_issue':
        return 'bg-red-100';
      case 'driver_issue':
        return 'bg-yellow-100';
      case 'system_alert':
        return 'bg-blue-100';
      case 'new_registration':
        return 'bg-green-100';
      default:
        return 'bg-gray-100';
    }
  }

  getNotificationIconColor(type: string): string {
    switch (type) {
      case 'panic':
        return 'text-white';
      case 'ride_report':
        return 'text-orange-600';
      case 'payment_issue':
        return 'text-red-600';
      case 'driver_issue':
        return 'text-yellow-600';
      case 'system_alert':
        return 'text-blue-600';
      case 'new_registration':
        return 'text-green-600';
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