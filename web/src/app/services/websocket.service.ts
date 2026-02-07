import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { BehaviorSubject } from 'rxjs';

export interface ChatMessage {
  id: number | null;
  senderId: number;
  receiverId: number;
  content: string;
  timestamp: Date;
}

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {

  private stompClient: Client | null = null;

  private messageSubject =
    new BehaviorSubject<ChatMessage | null>(null);

  public messages$ =
    this.messageSubject.asObservable();

  constructor() {}

  connect(userId: number): void {

    this.stompClient = new Client({

      // ✅ Direktan WebSocket (NO SockJS)
      brokerURL: 'ws://localhost:8080/ws',

      // Debug logs (možeš ugasiti u prod)
      debug: (str) => {
        console.log('[STOMP]', str);
      },

      // Auto reconnect
      reconnectDelay: 5000,

      // Heartbeat (best practice)
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      onConnect: () => {
        console.log('✅ WebSocket Connected');

        this.stompClient?.subscribe(
          `/topic/chat/${userId}`,
          (message: IMessage) => {

            const chatMessage: ChatMessage =
              JSON.parse(message.body);

            this.messageSubject.next(chatMessage);
          }
        );
      },

      onStompError: (frame) => {
        console.error('❌ Broker error:', frame.headers['message']);
        console.error('Details:', frame.body);
      },

      onWebSocketClose: () => {
        console.warn('⚠️ WebSocket closed');
      }
    });

    this.stompClient.activate();
  }

  sendMessage(
    senderId: number,
    receiverId: number,
    content: string
  ): void {

    if (!this.stompClient?.connected) {
      console.warn('STOMP not connected');
      return;
    }

    const message = {
      senderId,
      receiverId,
      content
    };

    this.stompClient.publish({
      destination: '/app/chat.send',
      body: JSON.stringify(message)
    });
  }

  disconnect(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
      console.log('🔌 WebSocket Disconnected');
    }
  }
}
