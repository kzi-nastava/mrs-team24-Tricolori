import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {NavBarBase} from './shared/components/nav-bar/nav-bar-base/nav-bar-base';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NavBarBase],
  templateUrl: './app.html',
  styleUrl: './app.css'
})

export class App {
  protected readonly title = signal('web');
}
