import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { heroPaperAirplane, heroUser } from '@ng-icons/heroicons/outline';
import { ChatService, ChatMessageDTO } from '../../../services/chat.service';
import { Subscription } from 'rxjs';
import { NgZone } from '@angular/core';

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
  private messagesSubscription?: Subscription;

  constructor(
    private chatService: ChatService,
    private ngZone: NgZone,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const personData = localStorage.getItem('person_data');
    if (personData) {
      const person = JSON.parse(personData);
      this.currentUserId = person.id;
    }

    this.chatService.getAdminId().subscribe({
      next: (response) => {
        this.adminUserId = response.adminId;
        this.loadMessages();
        this.subscribeToChat();
      },
      error: (error) => {
        console.error('Error fetching admin ID:', error);
      }
    });
  }

  ngOnDestroy(): void {
    this.messagesSubscription?.unsubscribe();
    this.chatService.unsubscribe();
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

        this.cdr.detectChanges();
        setTimeout(() => this.scrollToBottom(), 300);
      },
      error: (error) => {
        console.error('Error loading messages:', error);
      }
    });
  }

  subscribeToChat(): void {
    this.chatService.subscribeToChat(this.currentUserId);

    this.messagesSubscription = this.chatService.messages$.subscribe({
      next: (chatMessage: ChatMessageDTO | null) => {
        if (!chatMessage) return;

        this.ngZone.run(() => {
          if (
            (chatMessage.senderId === this.currentUserId && chatMessage.receiverId === this.adminUserId) ||
            (chatMessage.senderId === this.adminUserId && chatMessage.receiverId === this.currentUserId)
          ) {
            const message: Message = {
              id: chatMessage.id || Date.now(),
              text: chatMessage.content,
              timestamp: this.formatTimestamp(new Date(chatMessage.timestamp)),
              isFromUser: chatMessage.senderId === this.currentUserId
            };

            this.messages = [...this.messages, message];
            this.cdr.detectChanges();
            setTimeout(() => this.scrollToBottom(), 100);
          }
        });
      }
    });
  }

  sendMessage(): void {
    if (this.newMessage.trim()) {
      const messageText = this.newMessage;
      this.newMessage = '';

      this.chatService.checkAdminAvailable().subscribe({
        next: (response) => {
          if (response.available) {
            this.chatService.sendMessage(
              this.currentUserId,
              this.adminUserId,
              messageText
            );
          } else {
            this.showAdminUnavailableDialog = true;
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
