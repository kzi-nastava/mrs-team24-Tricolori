import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { heroPaperAirplane, heroUser } from '@ng-icons/heroicons/outline';

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
export class AdminSupport {
  chatUsers: ChatUser[] = [];
  selectedUser: ChatUser | null = null;
  messages: Message[] = [];
  newMessage: string = '';

  constructor() {
    this.loadChatUsers();
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
    // TODO: Replace with actual API call to fetch messages for selected user
    this.messages = [
      {
        id: 1,
        text: 'Hello! How can I help you today?',
        timestamp: 'Yesterday, 16:48',
        isFromAdmin: true
      },
      {
        id: 2,
        text: 'I have a question about my recent ride',
        timestamp: 'Yesterday, 17:53',
        isFromAdmin: false
      },
      {
        id: 3,
        text: 'Sure, I\'d be happy to help. What would you like to know?',
        timestamp: 'Today, 11:35',
        isFromAdmin: true
      },
      {
        id: 4,
        text: 'Can I get a refund for the cancelled ride?',
        timestamp: 'Today, 12:14',
        isFromAdmin: false
      }
    ];
  }

  sendMessage(): void {
    if (this.newMessage.trim() && this.selectedUser) {
      const message: Message = {
        id: this.messages.length + 1,
        text: this.newMessage,
        timestamp: this.getCurrentTimestamp(),
        isFromAdmin: true
      };
      
      this.messages.push(message);
      this.newMessage = '';
      
      // TODO: Send message to backend via API
      console.log('Admin message sent to user:', this.selectedUser.id, message);
      
      // Scroll to bottom after sending
      setTimeout(() => this.scrollToBottom(), 100);
    }
  }

  private getCurrentTimestamp(): string {
    const now = new Date();
    const hours = now.getHours().toString().padStart(2, '0');
    const minutes = now.getMinutes().toString().padStart(2, '0');
    return `Today, ${hours}:${minutes}`;
  }

  private scrollToBottom(): void {
    const chatContainer = document.querySelector('.chat-messages');
    if (chatContainer) {
      chatContainer.scrollTop = chatContainer.scrollHeight;
    }
  }
}