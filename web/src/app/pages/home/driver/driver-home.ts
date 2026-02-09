import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import {Map} from '../../../components/map/map';


@Component({
  selector: 'app-home-driver',
  standalone: true,
  imports: [CommonModule, RouterOutlet, Map],
  templateUrl: './driver-home.html'
})
export class HomeDriver {
}
