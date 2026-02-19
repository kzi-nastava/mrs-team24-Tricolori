import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { WebSocketService } from './websocket.service';
import { environment } from '../../environments/environment';

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

  private readonly API_URL = `${environment.apiUrl}/notifications`;
  private currentTopic: string | undefined;

  private notificationsSubject = new BehaviorSubject<NotificationDto[]>([]);
  public notifications$ = this.notificationsSubject.asObservable();

  private unreadCountSubject = new BehaviorSubject<number>(0);
  public unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(
    private http: HttpClient,
    private webSocketService: WebSocketService
  ) {}

  // ==================== HTTP ENDPOINTS ====================

  getAllNotifications(): Observable<NotificationDto[]> {
    return this.http.get<NotificationDto[]>(this.API_URL);
  }

  getUnreadNotifications(): Observable<NotificationDto[]> {
    return this.http.get<NotificationDto[]>(`${this.API_URL}/unread`);
  }

  getUnreadCount(): Observable<number> {
    return this.http.get<number>(`${this.API_URL}/unread/count`);
  }

  markAsRead(id: number): Observable<NotificationDto> {
    return this.http.put<NotificationDto>(`${this.API_URL}/${id}/read`, {});
  }

  markAllAsRead(): Observable<void> {
    return this.http.put<void>(`${this.API_URL}/read-all`, {});
  }

  deleteNotification(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  deleteAllNotifications(): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/all`);
  }

  // ==================== WEBSOCKET ====================

  subscribeToNotifications(userEmail: string): void {
    const topic = `/topic/notifications/${userEmail}`;
    this.currentTopic = topic;

    this.webSocketService.subscribe(topic, (notification: NotificationDto) => {
      console.log('New notification received:', notification);

      if (notification.type === 'RIDE_PANIC') {
        this.playPanicSound();
      }

      const current = this.notificationsSubject.value;
      this.notificationsSubject.next([notification, ...current]);

      if (!notification.opened) {
        this.unreadCountSubject.next(this.unreadCountSubject.value + 1);
      }
    });
  }

  unsubscribe(): void {
    if (this.currentTopic) {
      this.webSocketService.unsubscribe(this.currentTopic);
      this.currentTopic = undefined;
    }
  }

  // ==================== LOCAL STATE HELPERS ====================

  updateNotificationAsRead(id: number): void {
    const notifications = this.notificationsSubject.value.map(n =>
      n.id === id ? { ...n, opened: true } : n
    );
    this.notificationsSubject.next(notifications);
    this.recalculateUnreadCount();
  }

  updateAllNotificationsAsRead(): void {
    const notifications = this.notificationsSubject.value.map(n =>
      ({ ...n, opened: true })
    );
    this.notificationsSubject.next(notifications);
    this.unreadCountSubject.next(0);
  }

  removeNotification(id: number): void {
    const notifications = this.notificationsSubject.value.filter(n => n.id !== id);
    this.notificationsSubject.next(notifications);
    this.recalculateUnreadCount();
  }

  clearAllNotifications(): void {
    this.notificationsSubject.next([]);
    this.unreadCountSubject.next(0);
  }

  setNotifications(notifications: NotificationDto[]): void {
    this.notificationsSubject.next(notifications);
    this.recalculateUnreadCount();
  }

  private recalculateUnreadCount(): void {
    const count = this.notificationsSubject.value.filter(n => !n.opened).length;
    this.unreadCountSubject.next(count);
  }

  private playPanicSound(): void {
    const audioContext = new (window.AudioContext || (window as any).webkitAudioContext)();

    const oscillator = audioContext.createOscillator();
    const gainNode = audioContext.createGain();

    oscillator.connect(gainNode);
    gainNode.connect(audioContext.destination);

    oscillator.frequency.setValueAtTime(800, audioContext.currentTime);
    oscillator.frequency.exponentialRampToValueAtTime(400, audioContext.currentTime + 0.5);
    oscillator.frequency.exponentialRampToValueAtTime(800, audioContext.currentTime + 1);

    gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);
    gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 1);

    oscillator.start(audioContext.currentTime);
    oscillator.stop(audioContext.currentTime + 1);

    setTimeout(() => {
      const osc2 = audioContext.createOscillator();
      const gain2 = audioContext.createGain();
      osc2.connect(gain2);
      gain2.connect(audioContext.destination);
      osc2.frequency.setValueAtTime(800, audioContext.currentTime);
      gain2.gain.setValueAtTime(0.3, audioContext.currentTime);
      gain2.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.5);
      osc2.start(audioContext.currentTime);
      osc2.stop(audioContext.currentTime + 0.5);
    }, 1200);
  }
}
