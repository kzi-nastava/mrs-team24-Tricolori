import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';

@Component({
  selector: 'app-navigation-admin',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navigation-admin.html',
  styleUrl: './navigation-admin.css'
})
export class NavigationAdmin {
  constructor(private router: Router) {}

  onLogout() {
    localStorage.removeItem('userRole');
    localStorage.removeItem('authToken');
    this.router.navigate(['/login']);
  }
}