import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NgOptimizedImage } from '@angular/common';
import { NavigationDriver } from '../navigation-driver/navigation-driver';
import { ActionsDriver } from '../actions-driver/actions-driver';
import { NavigationPassenger } from '../navigation-passenger/navigation-passenger';
import { NavigationAdmin } from '../navigation-admin/navigation-admin';
import { AuthService} from '../../../../core/services/auth.service';
import { UserRole } from '../../../model/user-role';

@Component({
  selector: 'app-nav-bar-base',
  standalone: true,
  imports: [
    RouterLink,
    NgOptimizedImage,
    NavigationDriver,
    ActionsDriver,
    NavigationPassenger,
    NavigationAdmin
  ],
  templateUrl: './nav-bar-base.html',
  styleUrl: './nav-bar-base.css',
})
export class NavBarBase implements OnInit {
  role: UserRole = 'guest';

  constructor(private authService: AuthService) {}

  ngOnInit() {
    // Subscribe to user changes
    this.authService.currentUser$.subscribe(user => {
      this.role = user?.role || 'guest';
    });
  }
  
  onLogout() {
    this.authService.logout();
}
}

