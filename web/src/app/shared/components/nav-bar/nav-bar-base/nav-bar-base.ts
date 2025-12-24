import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { NgOptimizedImage } from '@angular/common';
import { NavigationDriver } from '../navigation-driver/navigation-driver';
import { NavigationPassenger } from '../navigation-passenger/navigation-passenger';
import { NavigationAdmin } from '../navigation-admin/navigation-admin';
import { ActionsDriver } from '../actions-driver/actions-driver';
import { UserRole } from '../../../model/user-role';

@Component({
  selector: 'app-nav-bar-base',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    NgOptimizedImage,
    NavigationDriver,
    NavigationPassenger,
    NavigationAdmin,
    ActionsDriver
  ],
  templateUrl: './nav-bar-base.html',
  styleUrl: './nav-bar-base.css',
})
export class NavBarBase implements OnInit {
  role: UserRole = 'guest';

  constructor(private router: Router) {}

  ngOnInit() {
    this.role = this.getUserRole();
  }

  private getUserRole(): UserRole {
    const storedRole = localStorage.getItem('userRole');
    return (storedRole as UserRole) || 'driver';
  }

  onLogout() {
    localStorage.removeItem('userRole');
    localStorage.removeItem('authToken');
    this.router.navigate(['/login']);
  }
}