import { Component } from '@angular/core';
import {RouterLink} from '@angular/router';
import {NgOptimizedImage} from '@angular/common';
import {NavigationDriver} from '../navigation-driver/navigation-driver';
import {ActionsDriver} from '../actions-driver/actions-driver';
import {UserRole} from '../../../model/user-role';

@Component({
  selector: 'app-nav-bar-base',
  imports: [
    RouterLink,
    NgOptimizedImage,
    NavigationDriver,
    ActionsDriver
  ],
  templateUrl: './nav-bar-base.html',
  styleUrl: './nav-bar-base.css',
})
export class NavBarBase {
  role: UserRole = 'driver';

  // setRole(newRole: UserRole) {
  //   this.role = newRole;
  // }
}
