import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { heroPaperAirplane, heroUser } from '@ng-icons/heroicons/outline';
import { WebSocketService, ChatMessage } from '../../../services/websocket.service';
import { ChatService, ChatMessageDTO, ChatUserDTO } from '../../../services/chat.service';
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
  role: 'ROLE_PASSENGER' | 'ROLE_DRIVER' | 'ROLE_ADMIN';
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
  
  private adminUserId: number = 0;
  private messageSubscription?: Subscription;

  // Color schemes for different roles
  private driverColors = [
    '#00acc1', // Cyan
    '#0088a3', // Darker cyan
    '#00bcd4', // Light cyan
    '#0097a7'  // Teal
  ];

  private passengerColors = [
    '#9333ea', // Purple
    '#a855f7', // Light purple
    '#7c3aed', // Violet
    '#8b5cf6'  // Medium purple
  ];

  constructor(
    private webSocketService: WebSocketService,
    private chatService: ChatService
  ) {}

  ngOnInit(): void {
    // Get admin ID from localStorage
    const personData = localStorage.getItem('person_data');
    if (personData) {
      const person = JSON.parse(personData);
      this.adminUserId = person.id;
    }

    this.loadChatUsers();
    this.connectWebSocket();
  }

  ngOnDestroy(): void {
    this.messageSubscription?.unsubscribe();
    this.webSocketService.disconnect();
  }

  loadChatUsers(): void {
    this.chatService.getActiveChats(this.adminUserId).subscribe({
      next: (users: ChatUserDTO[]) => {
        // Separate drivers and passengers for color assignment
        let driverIndex = 0;
        let passengerIndex = 0;

        this.chatUsers = users.map(user => {
          let color: string;
          
          if (user.role === 'ROLE_DRIVER') {
            color = this.driverColors[driverIndex % this.driverColors.length];
            driverIndex++;
          } else {
            color = this.passengerColors[passengerIndex % this.passengerColors.length];
            passengerIndex++;
          }

          return {
            id: user.id,
            name: `${user.firstName} ${user.lastName}`,
            role: user.role,
            hasUnread: user.hasUnread,
            lastMessage: user.lastMessage || 'No messages',
            color: color
          };
        });

        // Auto-select first user with unread messages
        const firstUnread = this.chatUsers.find(u => u.hasUnread);
        if (firstUnread) {
          this.selectUser(firstUnread);
        } else if (this.chatUsers.length > 0) {
          this.selectUser(this.chatUsers[0]);
        }
      },
      error: (error) => {
        console.error('Error loading chat users:', error);
      }
    });
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

  getRoleBadgeText(role: string): string {
    return role === 'ROLE_DRIVER' ? 'Driver' : 'Passenger';
  }

  getRoleBadgeClass(role: string): string {
    if (role === 'ROLE_DRIVER') {
      return 'bg-cyan-100 text-cyan-800';
    } else {
      return 'bg-purple-100 text-purple-800';
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