import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NgOptimizedImage } from '@angular/common';
import { NavigationDriver } from '../navigation-driver/navigation-driver';
import { ActionsDriver } from '../actions-driver/actions-driver';
import { NavigationPassenger } from '../navigation-passenger/navigation-passenger';
import { NavigationAdmin } from '../navigation-admin/navigation-admin';
import { AuthService} from '../../../services/auth.service';
import {PersonRole} from '../../../model/auth.model';

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
  role: PersonRole = 'ROLE_GUEST';

  constructor(private authService: AuthService) {}

  ngOnInit() {
    // Subscribe to user changes
    this.authService.currentPerson$.subscribe(person => {
      this.role = person?.role || 'ROLE_GUEST';
    });
  }

  onLogout() {
    this.authService.logout();
}
}

