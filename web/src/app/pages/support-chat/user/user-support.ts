import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { heroPaperAirplane, heroUser } from '@ng-icons/heroicons/outline';
import { WebSocketService, ChatMessage } from '../../../services/websocket.service';
import { ChatService, ChatMessageDTO } from '../../../services/chat.service';
import { Subscription } from 'rxjs';

interface Message {
  id: number;
  text: string;
  timestamp: string;
  isFromUser: boolean;
}

@Component({
  selector: 'app-user-support',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NgIcon
  ],
  providers: [provideIcons({ heroPaperAirplane, heroUser })],
  templateUrl: './user-support.html',
  styleUrl: './user-support.css'
})
export class UserSupport implements OnInit, OnDestroy {
  messages: Message[] = [];
  newMessage: string = '';
  showAdminUnavailableDialog: boolean = false;
  
  private currentUserId: number = 0;
  private adminUserId: number = 0;
  private messageSubscription?: Subscription;

  constructor(
    private webSocketService: WebSocketService,
    private chatService: ChatService
  ) {}

  ngOnInit(): void {
    // Get current user ID from localStorage
    const personData = localStorage.getItem('person_data');
    if (personData) {
      const person = JSON.parse(personData);
      this.currentUserId = person.id;
    }

    // Fetch admin ID and then load messages
    this.chatService.getAdminId().subscribe({
      next: (response) => {
        this.adminUserId = response.adminId;
        this.loadMessages();
        this.connectWebSocket();
      },
      error: (error) => {
        console.error('Error fetching admin ID:', error);
      }
    });
  }

  ngOnDestroy(): void {
    this.messageSubscription?.unsubscribe();
    this.webSocketService.disconnect();
  }

  loadMessages(): void {
    this.chatService.getChatHistory(this.currentUserId, this.adminUserId).subscribe({
      next: (chatMessages: ChatMessageDTO[]) => {
        this.messages = chatMessages.map(msg => ({
          id: msg.id,
          text: msg.content,
          timestamp: this.formatTimestamp(new Date(msg.timestamp)),
          isFromUser: msg.senderId === this.currentUserId
        }));
        setTimeout(() => this.scrollToBottom(), 100);
      },
      error: (error) => {
        console.error('Error loading messages:', error);
      }
    });
  }

  connectWebSocket(): void {
    this.webSocketService.connect(this.currentUserId);
    
    this.messageSubscription = this.webSocketService.messages$.subscribe({
      next: (chatMessage: ChatMessage | null) => {
        if (chatMessage && 
            (chatMessage.senderId === this.adminUserId || chatMessage.receiverId === this.currentUserId)) {
          const message: Message = {
            id: chatMessage.id || this.messages.length + 1,
            text: chatMessage.content,
            timestamp: this.formatTimestamp(new Date(chatMessage.timestamp)),
            isFromUser: chatMessage.senderId === this.currentUserId
          };
          
          this.messages.push(message);
          setTimeout(() => this.scrollToBottom(), 100);
        }
      }
    });
  }

  sendMessage(): void {
    if (this.newMessage.trim()) {
      // Check if admin is available before sending
      this.chatService.checkAdminAvailable().subscribe({
        next: (response) => {
          if (response.available) {
            // Admin available, send the message
            this.webSocketService.sendMessage(
              this.currentUserId,
              this.adminUserId,
              this.newMessage
            );
            
            this.newMessage = '';
          } else {
            // No admin available, show dialog
            this.showAdminUnavailableDialog = true;
            this.newMessage = '';
          }
        },
        error: (error) => {
          console.error('Error checking admin availability:', error);
        }
      });
    }
  }

  closeDialog(): void {
    this.showAdminUnavailableDialog = false;
  }

  private formatTimestamp(date: Date): string {
    const now = new Date();
    const isToday = date.toDateString() === now.toDateString();
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    
    if (isToday) {
      return `Today, ${hours}:${minutes}`;
    } else {
      return `Yesterday, ${hours}:${minutes}`;
    }
  }

  private scrollToBottom(): void {
    const chatContainer = document.querySelector('.chat-messages');
    if (chatContainer) {
      chatContainer.scrollTop = chatContainer.scrollHeight;
    }
  }
}