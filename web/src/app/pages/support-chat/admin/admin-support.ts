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
  isFromAdmin: boolean;
}

interface ChatUser {
  id: number;
  name: string;
  hasUnread: boolean;
  lastMessage: string;
  color: string;
}

@Component({
  selector: 'app-admin-support',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NgIcon
  ],
  providers: [provideIcons({ heroPaperAirplane, heroUser })],
  templateUrl: './admin-support.html',
  styleUrl: './admin-support.css'
})
export class AdminSupport implements OnInit, OnDestroy {
  chatUsers: ChatUser[] = [];
  selectedUser: ChatUser | null = null;
  messages: Message[] = [];
  newMessage: string = '';
  
  private adminUserId: number = 0; // TODO: Get from auth service
  private messageSubscription?: Subscription;

  constructor(
    private webSocketService: WebSocketService,
    private chatService: ChatService
  ) {}

  ngOnInit(): void {
    this.loadChatUsers();
    this.connectWebSocket();
  }

  ngOnDestroy(): void {
    this.messageSubscription?.unsubscribe();
    this.webSocketService.disconnect();
  }

  loadChatUsers(): void {
    // TODO: Replace with actual API call to fetch users with active chats
    this.chatUsers = [
      {
        id: 1,
        name: 'Pera Perić',
        hasUnread: true,
        lastMessage: 'Can I get a refund?',
        color: '#00acc1'
      },
      {
        id: 2,
        name: 'Milan Marković',
        hasUnread: true,
        lastMessage: 'When will my payment be processed?',
        color: '#9333ea'
      },
      {
        id: 3,
        name: 'Ljubica Smiljanić',
        hasUnread: false,
        lastMessage: 'Thank you for your help!',
        color: '#e879f9'
      },
      {
        id: 4,
        name: 'Olivera Jović',
        hasUnread: false,
        lastMessage: 'How do I update my profile?',
        color: '#84cc16'
      }
    ];

    // Auto-select first user with unread messages
    const firstUnread = this.chatUsers.find(u => u.hasUnread);
    if (firstUnread) {
      this.selectUser(firstUnread);
    } else if (this.chatUsers.length > 0) {
      this.selectUser(this.chatUsers[0]);
    }
  }

  selectUser(user: ChatUser): void {
    this.selectedUser = user;
    this.loadMessages(user.id);
    
    // Mark as read
    user.hasUnread = false;
  }

  loadMessages(userId: number): void {
    this.chatService.getChatHistory(this.adminUserId, userId).subscribe({
      next: (chatMessages: ChatMessageDTO[]) => {
        this.messages = chatMessages.map(msg => ({
          id: msg.id,
          text: msg.content,
          timestamp: this.formatTimestamp(new Date(msg.timestamp)),
          isFromAdmin: msg.senderId === this.adminUserId
        }));
        setTimeout(() => this.scrollToBottom(), 100);
      },
      error: (error) => {
        console.error('Error loading messages:', error);
      }
    });
  }

  connectWebSocket(): void {
    this.webSocketService.connect(this.adminUserId);
    
    this.messageSubscription = this.webSocketService.messages$.subscribe({
      next: (chatMessage: ChatMessage | null) => {
        if (chatMessage && this.selectedUser) {
          // Only add message if it's for the currently selected user
          if (chatMessage.senderId === this.selectedUser.id || 
              chatMessage.receiverId === this.selectedUser.id) {
            const message: Message = {
              id: chatMessage.id || this.messages.length + 1,
              text: chatMessage.content,
              timestamp: this.formatTimestamp(new Date(chatMessage.timestamp)),
              isFromAdmin: chatMessage.senderId === this.adminUserId
            };
            
            this.messages.push(message);
            setTimeout(() => this.scrollToBottom(), 100);
          }

          // Update chat user's last message
          const chatUser = this.chatUsers.find(u => 
            u.id === chatMessage.senderId || u.id === chatMessage.receiverId
          );
          if (chatUser) {
            chatUser.lastMessage = chatMessage.content;
            if (chatMessage.senderId !== this.adminUserId && 
                chatUser.id !== this.selectedUser?.id) {
              chatUser.hasUnread = true;
            }
          }
        }
      }
    });
  }

  sendMessage(): void {
    if (this.newMessage.trim() && this.selectedUser) {
      this.webSocketService.sendMessage(
        this.adminUserId,
        this.selectedUser.id,
        this.newMessage
      );
      
      this.newMessage = '';
    }
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