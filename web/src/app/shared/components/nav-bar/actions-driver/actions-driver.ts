import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-actions-driver',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './actions-driver.html',
  styleUrl: './actions-driver.css'
})
export class ActionsDriver {
  isActive = false;

  constructor(private authService: AuthService) {}

  toggleActive() {
    this.isActive = !this.isActive;
    console.log('Driver active status:', this.isActive);
  }

  logout() {
    this.authService.logout();
  }
}