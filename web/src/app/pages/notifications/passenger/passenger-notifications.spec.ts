import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { PassengerNotificationsComponent } from './passenger-notifications';

describe('PassengerNotificationsComponent', () => {
  let component: PassengerNotificationsComponent;
  let fixture: ComponentFixture<PassengerNotificationsComponent>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [PassengerNotificationsComponent],
      providers: [
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PassengerNotificationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('unreadCount', () => {
    it('should return the correct number of unread notifications', () => {
      const unreadCount = component.unreadCount();
      const actualUnread = component.notifications.filter(n => !n.isRead).length;
      expect(unreadCount).toBe(actualUnread);
    });

    it('should return 0 when all notifications are read', () => {
      component.notifications.forEach(n => n.isRead = true);
      expect(component.unreadCount()).toBe(0);
    });
  });

  describe('readCount', () => {
    it('should return the correct number of read notifications', () => {
      const readCount = component.readCount();
      const actualRead = component.notifications.filter(n => n.isRead).length;
      expect(readCount).toBe(actualRead);
    });

    it('should return 0 when no notifications are read', () => {
      component.notifications.forEach(n => n.isRead = false);
      expect(component.readCount()).toBe(0);
    });
  });

  describe('openNotification', () => {
    it('should mark notification as read when opened', () => {
      const unreadNotification = component.notifications.find(n => !n.isRead);
      if (unreadNotification) {
        component.openNotification(unreadNotification);
        expect(unreadNotification.isRead).toBe(true);
      }
    });

    it('should set selectedNotification', () => {
      const notification = component.notifications[0];
      component.openNotification(notification);
      expect(component.selectedNotification).toBe(notification);
    });

    it('should not change read status if already read', () => {
      const readNotification = component.notifications.find(n => n.isRead);
      if (readNotification) {
        component.openNotification(readNotification);
        expect(readNotification.isRead).toBe(true);
      }
    });
  });

  describe('closeModal', () => {
    it('should clear selectedNotification', () => {
      component.selectedNotification = component.notifications[0];
      component.closeModal();
      expect(component.selectedNotification).toBeNull();
    });
  });

  describe('markAllAsRead', () => {
    it('should mark all notifications as read', () => {
      component.notifications.forEach(n => n.isRead = false);
      component.markAllAsRead();
      expect(component.notifications.every(n => n.isRead)).toBe(true);
    });

    it('should not affect already read notifications', () => {
      component.markAllAsRead();
      const allRead = component.notifications.every(n => n.isRead);
      expect(allRead).toBe(true);
    });
  });

  describe('handleAction', () => {
    it('should navigate to actionUrl when present', () => {
      const notification = component.notifications.find(n => n.actionUrl);
      if (notification) {
        component.handleAction(notification);
        expect(mockRouter.navigate).toHaveBeenCalledWith([notification.actionUrl]);
      }
    });

    it('should close modal after handling action', () => {
      const notification = component.notifications.find(n => n.actionUrl);
      if (notification) {
        component.selectedNotification = notification;
        component.handleAction(notification);
        expect(component.selectedNotification).toBeNull();
      }
    });

    it('should not navigate when actionUrl is not present', () => {
      const notification = component.notifications.find(n => !n.actionUrl);
      if (notification) {
        component.handleAction(notification);
        expect(mockRouter.navigate).not.toHaveBeenCalled();
      }
    });
  });

  describe('getNotificationIcon', () => {
    it('should return correct icon for ride_starting', () => {
      expect(component.getNotificationIcon('ride_starting')).toBe('heroClock');
    });

    it('should return correct icon for ride_cancelled', () => {
      expect(component.getNotificationIcon('ride_cancelled')).toBe('heroXCircle');
    });

    it('should return correct icon for added_to_ride', () => {
      expect(component.getNotificationIcon('added_to_ride')).toBe('heroUserPlus');
    });

    it('should return default icon for unknown type', () => {
      expect(component.getNotificationIcon('unknown_type')).toBe('heroInformationCircle');
    });
  });

  describe('getNotificationIconBg', () => {
    it('should return correct background for ride_starting', () => {
      expect(component.getNotificationIconBg('ride_starting')).toBe('bg-blue-100');
    });

    it('should return correct background for ride_cancelled', () => {
      expect(component.getNotificationIconBg('ride_cancelled')).toBe('bg-red-100');
    });

    it('should return default background for unknown type', () => {
      expect(component.getNotificationIconBg('unknown_type')).toBe('bg-gray-100');
    });
  });

  describe('getNotificationIconColor', () => {
    it('should return correct color for ride_starting', () => {
      expect(component.getNotificationIconColor('ride_starting')).toBe('text-blue-600');
    });

    it('should return correct color for ride_cancelled', () => {
      expect(component.getNotificationIconColor('ride_cancelled')).toBe('text-red-600');
    });

    it('should return default color for unknown type', () => {
      expect(component.getNotificationIconColor('unknown_type')).toBe('text-gray-600');
    });
  });

  describe('getTimeAgo', () => {
    it('should return "Just now" for very recent timestamps', () => {
      const now = new Date();
      expect(component.getTimeAgo(now)).toBe('Just now');
    });

    it('should return minutes ago for recent timestamps', () => {
      const fiveMinutesAgo = new Date(Date.now() - 5 * 60 * 1000);
      expect(component.getTimeAgo(fiveMinutesAgo)).toBe('5m ago');
    });

    it('should return hours ago for timestamps within 24 hours', () => {
      const twoHoursAgo = new Date(Date.now() - 2 * 60 * 60 * 1000);
      expect(component.getTimeAgo(twoHoursAgo)).toBe('2h ago');
    });

    it('should return days ago for timestamps within a week', () => {
      const threeDaysAgo = new Date(Date.now() - 3 * 24 * 60 * 60 * 1000);
      expect(component.getTimeAgo(threeDaysAgo)).toBe('3d ago');
    });

    it('should return formatted date for timestamps older than a week', () => {
      const tenDaysAgo = new Date(Date.now() - 10 * 24 * 60 * 60 * 1000);
      const result = component.getTimeAgo(tenDaysAgo);
      expect(result).toMatch(/\w+ \d+/); // e.g., "Jan 5"
    });
  });

  describe('formatFullDate', () => {
    it('should format date with full details', () => {
      const testDate = new Date('2024-12-15T14:30:00');
      const result = component.formatFullDate(testDate);
      expect(result).toContain('2024');
      expect(result).toContain('December');
      expect(result).toContain('15');
    });

    it('should include time in formatted string', () => {
      const testDate = new Date('2024-12-15T14:30:00');
      const result = component.formatFullDate(testDate);
      expect(result).toMatch(/\d{1,2}:\d{2}/); // Matches time format
    });
  });

  describe('initial state', () => {
    it('should have notifications array populated', () => {
      expect(component.notifications.length).toBeGreaterThan(0);
    });

    it('should have some unread notifications initially', () => {
      expect(component.unreadCount()).toBeGreaterThan(0);
    });

    it('should not have a selected notification initially', () => {
      expect(component.selectedNotification).toBeNull();
    });
  });

  describe('notification types', () => {
    it('should have different notification types', () => {
      const types = new Set(component.notifications.map(n => n.type));
      expect(types.size).toBeGreaterThan(1);
    });

    it('should have notifications with required properties', () => {
      component.notifications.forEach(notification => {
        expect(notification.id).toBeDefined();
        expect(notification.type).toBeDefined();
        expect(notification.title).toBeDefined();
        expect(notification.body).toBeDefined();
        expect(notification.timestamp).toBeDefined();
        expect(notification.isRead).toBeDefined();
      });
    });
  });
});