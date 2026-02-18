import { Injectable } from '@angular/core';
import {Client, IMessage, StompSubscription} from '@stomp/stompjs';
import { BehaviorSubject, filter, first } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private stompClient: Client | null = null;
  private connectionStatus$ = new BehaviorSubject<boolean>(false);

  constructor() {}

  connect(): void {
    if (this.stompClient?.connected) return;

    const token = localStorage.getItem('access_token');

    this.stompClient = new Client({
      brokerURL: 'ws://localhost:8080/ws',
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: (frame) => {
        console.log('WebSocket Connected');
        this.connectionStatus$.next(true);
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
        this.connectionStatus$.next(false);
      },
      onWebSocketClose: () => {
        console.warn('WebSocket connection closed');
        this.connectionStatus$.next(false);
      }
    });

    this.stompClient.activate();
  }

  subscribe(topic: string, callback: (payload: any) => void): StompSubscription | undefined {
    let subscription: StompSubscription | undefined;

    if (this.connectionStatus$.value) {
      subscription = this.doSubscribe(topic, callback);
    } else {
      this.connectionStatus$.pipe(
        filter(status => status),
        first()
      ).subscribe(() => {
        this.doSubscribe(topic, callback);
      });
    }

    return subscription;
  }

  private doSubscribe(topic: string, callback: (payload: any) => void): StompSubscription | undefined {
    console.log(`Subscribing to: ${topic}`);
    return this.stompClient?.subscribe(topic, (message: IMessage) => {
      callback(JSON.parse(message.body));
    });
  }

  /** Method for sending data to backend */
  publish(destination: string, body: any): void {
    if (this.stompClient?.connected) {
      this.stompClient.publish({
        destination: destination,
        body: JSON.stringify(body)
      });
    } else {
      console.error('Cannot publish, STOMP not connected');
    }
  }

  disconnect(): void {
    if (this.stompClient) {
      this.stompClient.deactivate().then(r => {
        this.connectionStatus$.next(false);
        console.log('WebSocket Deactivated');
      });
    }
  }

}
