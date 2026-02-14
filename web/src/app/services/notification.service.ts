import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { Client, StompSubscription } from '@stomp/stompjs';

export interface NotificationDto {
  id: number;
  email: string;
  time: string;
  opened: boolean;
  content: string;
  type: string;
  rideId?: number;
  actionUrl?: string;
  driverName?: string;
  passengerName?: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = 'http://localhost:8080/api/v1/notifications';
  private wsUrl = 'ws://localhost:8080/ws';

  private stompClient: Client | null = null;
  private subscription: StompSubscription | null = null;

  // Observable streams for real-time updates
  private notificationsSubject = new BehaviorSubject<NotificationDto[]>([]);
  public notifications$ = this.notificationsSubject.asObservable();

  private unreadCountSubject = new BehaviorSubject<number>(0);
  public unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private http: HttpClient) {}

  // ==================== HTTP ENDPOINTS ====================

  getAllNotifications(): Observable<NotificationDto[]> {
    return this.http.get<NotificationDto[]>(this.apiUrl);
  }

  getUnreadNotifications(): Observable<NotificationDto[]> {
    return this.http.get<NotificationDto[]>(`${this.apiUrl}/unread`);
  }

  getUnreadCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/unread/count`);
  }

  markAsRead(id: number): Observable<NotificationDto> {
    return this.http.put<NotificationDto>(`${this.apiUrl}/${id}/read`, {});
  }

  markAllAsRead(): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/read-all`, {});
  }

  deleteNotification(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  deleteAllNotifications(): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/all`);
  }

  // ==================== WEBSOCKET CONNECTION ====================

  connectWebSocket(userEmail: string): void {
    if (this.stompClient?.connected) {
      console.log('WebSocket already connected');
      return;
    }

    this.stompClient = new Client({
      brokerURL: this.wsUrl,
      debug: (str) => {
        console.log('STOMP Debug:', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('WebSocket connected for notifications');
        this.subscribeToNotifications(userEmail);
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
      },
      onWebSocketError: (error) => {
        console.error('WebSocket error:', error);
      },
      onWebSocketClose: () => {
        console.log('WebSocket connection closed');
      }
    });

    this.stompClient.activate();
  }

  private subscribeToNotifications(userEmail: string): void {
    if (!this.stompClient?.connected) {
      console.error('Cannot subscribe - WebSocket not connected');
      return;
    }

    const topic = `/topic/notifications/${userEmail}`;
    console.log('Subscribing to:', topic);

    this.subscription = this.stompClient.subscribe(topic, (message) => {
      const notification: NotificationDto = JSON.parse(message.body);
      console.log('New notification received:', notification);

      if (notification.type === 'RIDE_PANIC') {
        this.playPanicSound();
      }

      // Add new notification to the list
      const currentNotifications = this.notificationsSubject.value;
      this.notificationsSubject.next([notification, ...currentNotifications]);

      // Update unread count
      if (!notification.opened) {
        this.unreadCountSubject.next(this.unreadCountSubject.value + 1);
      }
    });
  }

  disconnectWebSocket(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
      this.subscription = null;
    }

    if (this.stompClient?.connected) {
      this.stompClient.deactivate();
      this.stompClient = null;
      console.log('WebSocket disconnected');
    }
  }

  // ==================== HELPER METHODS ====================

  // Update local state after marking as read
  updateNotificationAsRead(id: number): void {
    const notifications = this.notificationsSubject.value.map(n =>
      n.id === id ? { ...n, opened: true } : n
    );
    this.notificationsSubject.next(notifications);
    this.updateUnreadCount();
  }

  // Update local state after marking all as read
  updateAllNotificationsAsRead(): void {
    const notifications = this.notificationsSubject.value.map(n =>
      ({ ...n, opened: true })
    );
    this.notificationsSubject.next(notifications);
    this.unreadCountSubject.next(0);
  }

  // Update local state after deleting a notification
  removeNotification(id: number): void {
    const notifications = this.notificationsSubject.value.filter(n => n.id !== id);
    this.notificationsSubject.next(notifications);
    this.updateUnreadCount();
  }

  // Update local state after deleting all notifications
  clearAllNotifications(): void {
    this.notificationsSubject.next([]);
    this.unreadCountSubject.next(0);
  }

  // Manually set notifications (for initial load)
  setNotifications(notifications: NotificationDto[]): void {
    this.notificationsSubject.next(notifications);
    this.updateUnreadCount();
  }

  // Recalculate unread count
  private updateUnreadCount(): void {
    const count = this.notificationsSubject.value.filter(n => !n.opened).length;
    this.unreadCountSubject.next(count);
  }

  private playPanicSound(): void {
      const audioContext = new (window.AudioContext || (window as any).webkitAudioContext)();

      const oscillator = audioContext.createOscillator();
      const gainNode = audioContext.createGain();

      oscillator.connect(gainNode);
      gainNode.connect(audioContext.destination);

      // Create siren-like sound
      oscillator.frequency.setValueAtTime(800, audioContext.currentTime);
      oscillator.frequency.exponentialRampToValueAtTime(400, audioContext.currentTime + 0.5);
      oscillator.frequency.exponentialRampToValueAtTime(800, audioContext.currentTime + 1);

      gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);
      gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 1);

      oscillator.start(audioContext.currentTime);
      oscillator.stop(audioContext.currentTime + 1);

      // Play multiple times for urgency
      setTimeout(() => {
        const osc2 = audioContext!.createOscillator();
        const gain2 = audioContext!.createGain();
        osc2.connect(gain2);
        gain2.connect(audioContext!.destination);
        osc2.frequency.setValueAtTime(800, audioContext!.currentTime);
        gain2.gain.setValueAtTime(0.3, audioContext!.currentTime);
        gain2.gain.exponentialRampToValueAtTime(0.01, audioContext!.currentTime + 0.5);
        osc2.start(audioContext!.currentTime);
        osc2.stop(audioContext!.currentTime + 0.5);
      }, 1200);
  }
}
