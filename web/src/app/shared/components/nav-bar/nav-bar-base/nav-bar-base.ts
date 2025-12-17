import { Component } from '@angular/core';
import {RouterLink} from '@angular/router';
import {NgOptimizedImage} from '@angular/common';

@Component({
  selector: 'app-nav-bar-base',
  imports: [
    RouterLink,
    NgOptimizedImage
  ],
  templateUrl: './nav-bar-base.html',
  styleUrl: './nav-bar-base.css',
})
export class NavBarBase {

}
