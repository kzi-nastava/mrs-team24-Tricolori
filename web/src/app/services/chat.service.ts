import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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
  private apiUrl = 'http://localhost:8080/api/v1/chats';

  constructor(private http: HttpClient) {}

  getChatHistory(userId1: number, userId2: number): Observable<ChatMessageDTO[]> {
    return this.http.get<ChatMessageDTO[]>(`${this.apiUrl}/history`, {
      params: { userId1: userId1.toString(), userId2: userId2.toString() }
    });
  }

  checkAdminAvailable(): Observable<{ available: boolean }> {
    return this.http.get<{ available: boolean }>(`${this.apiUrl}/admin-available`);
  }

  getActiveChats(adminId: number): Observable<ChatUserDTO[]> {
    return this.http.get<ChatUserDTO[]>(`${this.apiUrl}/active-chats`, {
      params: { adminId: adminId.toString() }
    });
  }

  getAdminId(): Observable<{ adminId: number }> {
  return this.http.get<{ adminId: number }>(`${this.apiUrl}/admin-id`);
}
}