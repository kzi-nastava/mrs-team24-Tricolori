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

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = 'http://localhost:8080/api/v1/chat';

  constructor(private http: HttpClient) {}

  getChatHistory(userId1: number, userId2: number): Observable<ChatMessageDTO[]> {
    return this.http.get<ChatMessageDTO[]>(`${this.apiUrl}/history`, {
      params: { userId1: userId1.toString(), userId2: userId2.toString() }
    });
  }

  checkAdminAvailable(): Observable<{ available: boolean }> {
    return this.http.get<{ available: boolean }>(`${this.apiUrl}/admin-available`);
  }
}