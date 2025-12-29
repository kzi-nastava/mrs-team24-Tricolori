import {Component, signal} from '@angular/core';
import {NgIcon} from '@ng-icons/core';
import {NgClass} from '@angular/common';
import {RouterLink} from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-actions-driver',
  imports: [
    NgIcon,
    NgClass,
    RouterLink
  ],
  templateUrl: './actions-driver.html',
  styleUrl: './actions-driver.css',
})
export class ActionsDriver {
  active: boolean = true ;
  constructor(private authService: AuthService) {}

  toggleStatus() {
    this.active = !this.active;
  }

  protected logout() {
    this.authService.logout();
  }
}