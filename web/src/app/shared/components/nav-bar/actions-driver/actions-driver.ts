import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-actions-driver',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './actions-driver.html',
  styleUrl: './actions-driver.css'
})
export class ActionsDriver {
  isActive = false;

  toggleActive() {
    this.isActive = !this.isActive;
    // TODO: Update backend with driver's active status
    console.log('Driver active status:', this.isActive);
  }

  logout() {
    // TODO: Implement logout logic
    console.log('Logging out...');
  }
}