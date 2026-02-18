import {Component, OnInit, OnDestroy, ChangeDetectorRef} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { NotificationService } from '../../../services/notification.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-navigation-admin',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navigation-admin.html',
  styleUrl: './navigation-admin.css'
})
export class NavigationAdmin implements OnInit, OnDestroy {
  unreadCount = 0;
  private unreadSubscription?: Subscription;

  constructor(
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.unreadSubscription = this.notificationService.unreadCount$.subscribe(
      count => {
        this.unreadCount = count;
        this.cdr.detectChanges();
      }
    );
  }

  ngOnDestroy(): void {
    this.unreadSubscription?.unsubscribe();
  }

}
