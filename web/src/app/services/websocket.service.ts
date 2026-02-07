import { Injectable } from '@angular/core';
import SockJS from 'sockjs-client';
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

      webSocketFactory: () =>
        new SockJS('http://localhost:8080/ws'),

      debug: (str) => {
        console.log(str);
      },

      reconnectDelay: 5000,

      onConnect: () => {
        console.log('WebSocket Connected');

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
        console.error('Broker error:', frame);
      }
    });

    this.stompClient.activate();
  }

  sendMessage(
    senderId: number,
    receiverId: number,
    content: string
  ): void {

    if (!this.stompClient?.connected) return;

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
      console.log('WebSocket Disconnected');
    }
  }
}
