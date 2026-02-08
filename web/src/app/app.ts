import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavBarBase } from './components/nav-bar/nav-bar-base/nav-bar-base';
import {Toast} from './components/toast/toast';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NavBarBase, Toast],
  templateUrl: './app.html',
  styleUrl: './app.css'
})

export class App {
  protected readonly title = signal('web');
}
