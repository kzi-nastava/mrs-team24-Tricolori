import { Component, computed, signal } from '@angular/core';
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
  heroCurrencyDollar,
  heroStar
} from '@ng-icons/heroicons/outline';

interface Notification {
  id: number;
  type: 'new_ride_request' | 'ride_cancelled_by_passenger' | 'passenger_added' | 'ride_completed' | 'payment_received' | 'rating_received' | 'ride_reminder' | 'earnings_summary';
  title: string;
  body: string;
  timestamp: Date;
  isRead: boolean;
  rideId?: number;
  actionUrl?: string;
}

@Component({
  selector: 'app-driver-notifications',
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
      heroCurrencyDollar,
      heroStar
    })
  ],
  templateUrl: './driver-notifications.html',
})
export class DriverNotifications {
  selectedNotification: Notification | null = null;
  showUnreadOnly = signal<boolean>(false);
  
  notifications = signal<Notification[]>([
    {
      id: 1,
      type: 'new_ride_request',
      title: 'New ride request',
      body: 'Ana Marković has requested a ride from Bulevar Oslobođenja 46 to Novi Sad Airport. Pickup time: 3:30 PM. Distance: 8.5 km. Estimated fare: 450.00 RSD.',
      timestamp: new Date(Date.now() - 5 * 60 * 1000), // 5 minutes ago
      isRead: false,
      rideId: 15678,
      actionUrl: '/driver/ride-requests/15678'
    },
    {
      id: 2,
      type: 'ride_reminder',
      title: 'Upcoming ride reminder',
      body: 'You have a ride scheduled in 30 minutes. Pickup location: Trg Slobode 1. Passenger: Petar Jovanović. Make sure to arrive on time!',
      timestamp: new Date(Date.now() - 15 * 60 * 1000), // 15 minutes ago
      isRead: false,
      rideId: 15677,
      actionUrl: '/driver/ride-tracking/15677'
    },
    {
      id: 3,
      type: 'passenger_added',
      title: 'Passenger added to your ride',
      body: 'Stefan Nikolić has joined your shared ride from Grbavica to Spens. Total passengers: 3. Updated fare: 675.00 RSD.',
      timestamp: new Date(Date.now() - 45 * 60 * 1000), // 45 minutes ago
      isRead: false,
      rideId: 15676,
      actionUrl: '/driver/ride-details/15676'
    },
    {
      id: 4,
      type: 'rating_received',
      title: 'You received a 5-star rating!',
      body: 'Marija Popović rated you 5 stars with the comment: "Excellent driver! Very professional and friendly. The car was clean and the ride was smooth." Keep up the great work!',
      timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000), // 2 hours ago
      isRead: false,
      rideId: 15675,
      actionUrl: '/driver/ratings'
    },
    {
      id: 5,
      type: 'ride_cancelled_by_passenger',
      title: 'Ride cancelled by passenger',
      body: 'Nikola Đorđević cancelled the ride scheduled for December 14, 2024 at 5:00 PM from Petrovaradinska tvrđava to Novi Sad Train Station. Cancellation fee of 50.00 RSD has been credited to your account.',
      timestamp: new Date(Date.now() - 6 * 60 * 60 * 1000), // 6 hours ago
      isRead: true,
      rideId: 15674
    },
    {
      id: 6,
      type: 'ride_completed',
      title: 'Ride completed',
      body: 'Your ride from Spens to Futog has been completed successfully. Total distance: 12.3 km. Duration: 18 minutes. Fare earned: 520.00 RSD.',
      timestamp: new Date(Date.now() - 24 * 60 * 60 * 1000), // 1 day ago
      isRead: true,
      rideId: 15673,
      actionUrl: '/driver/history'
    },
    {
      id: 7,
      type: 'payment_received',
      title: 'Payment received',
      body: 'Payment of 385.50 RSD for ride #15672 has been successfully transferred to your account. Current balance: 2,450.75 RSD.',
      timestamp: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000), // 2 days ago
      isRead: true,
      rideId: 15672
    },
    {
      id: 8,
      type: 'earnings_summary',
      title: 'Weekly earnings summary',
      body: 'Congratulations! You completed 24 rides this week and earned 8,450.00 RSD. Your average rating is 4.8 stars. You\'re in the top 15% of drivers in Novi Sad!',
      timestamp: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000), // 3 days ago
      isRead: true,
      actionUrl: '/driver/earnings'
    },
    {
      id: 9,
      type: 'ride_completed',
      title: 'Ride completed',
      body: 'Your shared ride from Liman to Centar has been completed. You transported 3 passengers over 6.8 km. Total fare earned: 780.00 RSD.',
      timestamp: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000), // 5 days ago
      isRead: true,
      rideId: 15671,
      actionUrl: '/driver/history'
    }
  ]);

  filteredNotifications = computed(() => {
    if (this.showUnreadOnly()) {
      return this.notifications().filter(n => !n.isRead);
    }
    return this.notifications();
  });

  constructor(private router: Router) {}

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

  handleAction(notification: Notification): void {
    if (notification.actionUrl) {
      this.router.navigate([notification.actionUrl]);
    }
    this.closeModal();
  }

  getNotificationIcon(type: string): string {
    switch (type) {
      case 'new_ride_request':
        return 'heroBell';
      case 'ride_cancelled_by_passenger':
        return 'heroXCircle';
      case 'passenger_added':
        return 'heroUserPlus';
      case 'ride_completed':
        return 'heroCheckCircle';
      case 'payment_received':
        return 'heroCurrencyDollar';
      case 'rating_received':
        return 'heroStar';
      case 'ride_reminder':
        return 'heroClock';
      case 'earnings_summary':
        return 'heroCurrencyDollar';
      default:
        return 'heroInformationCircle';
    }
  }

  getNotificationIconBg(type: string): string {
    switch (type) {
      case 'new_ride_request':
        return 'bg-blue-100';
      case 'ride_cancelled_by_passenger':
        return 'bg-red-100';
      case 'passenger_added':
        return 'bg-purple-100';
      case 'ride_completed':
        return 'bg-green-100';
      case 'payment_received':
        return 'bg-emerald-100';
      case 'rating_received':
        return 'bg-yellow-100';
      case 'ride_reminder':
        return 'bg-orange-100';
      case 'earnings_summary':
        return 'bg-teal-100';
      default:
        return 'bg-gray-100';
    }
  }

  getNotificationIconColor(type: string): string {
    switch (type) {
      case 'new_ride_request':
        return 'text-blue-600';
      case 'ride_cancelled_by_passenger':
        return 'text-red-600';
      case 'passenger_added':
        return 'text-purple-600';
      case 'ride_completed':
        return 'text-green-600';
      case 'payment_received':
        return 'text-emerald-600';
      case 'rating_received':
        return 'text-yellow-600';
      case 'ride_reminder':
        return 'text-orange-600';
      case 'earnings_summary':
        return 'text-teal-600';
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