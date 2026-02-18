import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { WebSocketService } from './websocket.service';
import { environment } from '../../environments/environment';

export interface ChatMessageDTO {
  id: number;
  senderId: number;
  receiverId: number;
  content: string;
  timestamp: string;
}

export interface ChatUserDTO {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: 'ROLE_PASSENGER' | 'ROLE_DRIVER' | 'ROLE_ADMIN';
  lastMessage: string;
  lastMessageTime: string;
  hasUnread: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  private readonly API_URL = `${environment.apiUrl}/chats`;
  private currentTopic: string | undefined;

  private messagesSubject = new BehaviorSubject<ChatMessageDTO | null>(null);
  public messages$ = this.messagesSubject.asObservable();

  constructor(
    private http: HttpClient,
    private webSocketService: WebSocketService
  ) {}

  // ==================== HTTP ENDPOINTS ====================

  getChatHistory(userId1: number, userId2: number): Observable<ChatMessageDTO[]> {
    return this.http.get<ChatMessageDTO[]>(`${this.API_URL}/history`, {
      params: { userId1: userId1.toString(), userId2: userId2.toString() }
    });
  }

  checkAdminAvailable(): Observable<{ available: boolean }> {
    return this.http.get<{ available: boolean }>(`${this.API_URL}/admin-available`);
  }

  getActiveChats(adminId: number): Observable<ChatUserDTO[]> {
    return this.http.get<ChatUserDTO[]>(`${this.API_URL}/active-chats`, {
      params: { adminId: adminId.toString() }
    });
  }

  getAdminId(): Observable<{ adminId: number }> {
    return this.http.get<{ adminId: number }>(`${this.API_URL}/admin-id`);
  }

  // ==================== WEBSOCKET ====================

  subscribeToChat(userId: number): void {
    const topic = `/topic/chat/${userId}`;
    this.currentTopic = topic;

    this.webSocketService.subscribe(topic, (message: ChatMessageDTO) => {
      this.messagesSubject.next(message);
    });
  }

  sendMessage(senderId: number, receiverId: number, content: string): void {
    this.webSocketService.publish('/app/chat.send', {
      senderId,
      receiverId,
      content
    });
  }

  unsubscribe(): void {
    if (this.currentTopic) {
      this.webSocketService.unsubscribe(this.currentTopic);
      this.currentTopic = undefined;
    }
  }

}
