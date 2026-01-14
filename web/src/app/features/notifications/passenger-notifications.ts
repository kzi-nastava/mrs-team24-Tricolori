import { Component } from '@angular/core';
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
  heroInformationCircle
} from '@ng-icons/heroicons/outline';

interface Notification {
  id: number;
  type: 'ride_starting' | 'ride_cancelled' | 'added_to_ride' | 'ride_completed' | 'payment_processed' | 'driver_assigned' | 'rating_reminder';
  title: string;
  body: string;
  timestamp: Date;
  isRead: boolean;
  rideId?: number;
  actionUrl?: string;
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
      heroInformationCircle
    })
  ],
  templateUrl: './passenger-notifications.html',
  styleUrls: ['./passenger-notifications.css']
})
export class PassengerNotificationsComponent {
  selectedNotification: Notification | null = null;
  
  notifications: Notification[] = [
    {
      id: 1,
      type: 'ride_starting',
      title: 'Your ride is starting soon',
      body: 'Your driver Marko Petrović will arrive at Trg Slobode 1 in approximately 5 minutes. The vehicle is a white Toyota Corolla with license plate NS-123-AB.',
      timestamp: new Date(Date.now() - 10 * 60 * 1000), // 10 minutes ago
      isRead: false,
      rideId: 12345,
      actionUrl: '/passenger/ride-tracking/12345'
    },
    {
      id: 2,
      type: 'added_to_ride',
      title: 'You were added to a shared ride',
      body: 'Ana Jovanović added you to a shared ride from Bulevar Oslobođenja to Novi Sad Fair scheduled for tomorrow at 2:00 PM. Total cost will be split between 3 passengers.',
      timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000), // 2 hours ago
      isRead: false,
      rideId: 12344,
      actionUrl: '/passenger/ride-details/12344'
    },
    {
      id: 3,
      type: 'rating_reminder',
      title: 'Rate your recent ride',
      body: 'How was your ride with Stefan Nikolić? Your feedback helps us maintain quality service. You have 48 hours remaining to submit your rating.',
      timestamp: new Date(Date.now() - 5 * 60 * 60 * 1000), // 5 hours ago
      isRead: false,
      rideId: 12343,
      actionUrl: '/passenger/ride-rating/12343'
    },
    {
      id: 4,
      type: 'ride_cancelled',
      title: 'Ride cancelled',
      body: 'Your ride scheduled for December 14, 2024 at 4:00 PM from Spens to Petrovaradinska tvrđava has been cancelled by the driver due to unforeseen circumstances. Your payment has been refunded.',
      timestamp: new Date(Date.now() - 24 * 60 * 60 * 1000), // 1 day ago
      isRead: true,
      rideId: 12342
    },
    {
      id: 5,
      type: 'ride_completed',
      title: 'Ride completed successfully',
      body: 'Your ride from Grbavica to Spens has been completed. Total fare: 250.00 RSD. Thank you for riding with us!',
      timestamp: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000), // 2 days ago
      isRead: true,
      rideId: 12341,
      actionUrl: '/passenger/history'
    },
    {
      id: 6,
      type: 'payment_processed',
      title: 'Payment processed',
      body: 'Your payment of 325.80 RSD for ride #12340 has been successfully processed using your Visa card ending in 4242.',
      timestamp: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000), // 3 days ago
      isRead: true,
      rideId: 12340
    },
    {
      id: 7,
      type: 'driver_assigned',
      title: 'Driver assigned to your ride',
      body: 'Great news! Milan Stojanović has accepted your ride request. Check out their 4.9-star rating and get ready for your trip.',
      timestamp: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000), // 5 days ago
      isRead: true,
      rideId: 12339,
      actionUrl: '/passenger/ride-tracking/12339'
    }
  ];

  constructor(private router: Router) {}

  unreadCount(): number {
    return this.notifications.filter(n => !n.isRead).length;
  }

  readCount(): number {
    return this.notifications.filter(n => n.isRead).length;
  }

  openNotification(notification: Notification): void {
    if (!notification.isRead) {
      notification.isRead = true;
    }
    this.selectedNotification = notification;
  }

  closeModal(): void {
    this.selectedNotification = null;
  }

  markAllAsRead(): void {
    this.notifications.forEach(n => n.isRead = true);
  }

  handleAction(notification: Notification): void {
    if (notification.actionUrl) {
      this.router.navigate([notification.actionUrl]);
    }
    this.closeModal();
  }

  getNotificationIcon(type: string): string {
    switch (type) {
      case 'ride_starting':
        return 'heroClock';
      case 'ride_cancelled':
        return 'heroXCircle';
      case 'added_to_ride':
        return 'heroUserPlus';
      case 'ride_completed':
        return 'heroCheckCircle';
      case 'payment_processed':
        return 'heroCheckCircle';
      case 'driver_assigned':
        return 'heroInformationCircle';
      case 'rating_reminder':
        return 'heroBell';
      default:
        return 'heroInformationCircle';
    }
  }

  getNotificationIconBg(type: string): string {
    switch (type) {
      case 'ride_starting':
        return 'bg-blue-100';
      case 'ride_cancelled':
        return 'bg-red-100';
      case 'added_to_ride':
        return 'bg-purple-100';
      case 'ride_completed':
        return 'bg-green-100';
      case 'payment_processed':
        return 'bg-green-100';
      case 'driver_assigned':
        return 'bg-teal-100';
      case 'rating_reminder':
        return 'bg-yellow-100';
      default:
        return 'bg-gray-100';
    }
  }

  getNotificationIconColor(type: string): string {
    switch (type) {
      case 'ride_starting':
        return 'text-blue-600';
      case 'ride_cancelled':
        return 'text-red-600';
      case 'added_to_ride':
        return 'text-purple-600';
      case 'ride_completed':
        return 'text-green-600';
      case 'payment_processed':
        return 'text-green-600';
      case 'driver_assigned':
        return 'text-teal-600';
      case 'rating_reminder':
        return 'text-yellow-600';
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