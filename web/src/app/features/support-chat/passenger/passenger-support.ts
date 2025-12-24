import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { heroPaperAirplane } from '@ng-icons/heroicons/outline';

interface Message {
  id: number;
  text: string;
  timestamp: string;
  isFromUser: boolean;
}

@Component({
  selector: 'app-passenger-support',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NgIcon
  ],
  providers: [provideIcons({ heroPaperAirplane })],
  templateUrl: './passenger-support.html',
  styleUrl: './passenger-support.css'
})
export class PassengerSupport {
  messages: Message[] = [];
  newMessage: string = '';

  constructor() {
    this.loadMessages();
  }

  loadMessages(): void {
    // TODO: Replace with actual API call to fetch chat history
    this.messages = [
      {
        id: 1,
        text: 'Hello! How can I help you today?',
        timestamp: 'Yesterday, 16:48',
        isFromUser: false
      },
      {
        id: 2,
        text: 'I have a question about my recent ride',
        timestamp: 'Yesterday, 17:53',
        isFromUser: true
      },
      {
        id: 3,
        text: 'Sure, I\'d be happy to help. What would you like to know?',
        timestamp: 'Today, 11:35',
        isFromUser: false
      },
      {
        id: 4,
        text: 'Can I get a refund for the cancelled ride?',
        timestamp: 'Today, 12:14',
        isFromUser: true
      }
    ];
  }

  sendMessage(): void {
    if (this.newMessage.trim()) {
      const message: Message = {
        id: this.messages.length + 1,
        text: this.newMessage,
        timestamp: this.getCurrentTimestamp(),
        isFromUser: true
      };
      
      this.messages.push(message);
      this.newMessage = '';
      
      // TODO: Send message to backend via API
      console.log('Message sent:', message);
      
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