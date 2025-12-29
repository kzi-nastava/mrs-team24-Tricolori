import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-navigation-driver',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navigation-driver.html',
  styleUrl: './navigation-driver.css'
})
export class NavigationDriver {
  isActive = false;

  toggleActive() {
    this.isActive = !this.isActive;
    // TODO: Update backend with driver's active status
    console.log('Driver active status:', this.isActive);
  }
}