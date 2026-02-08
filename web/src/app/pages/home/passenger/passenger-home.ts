import { Component } from '@angular/core';
import * as L from 'leaflet';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import {Map} from '../../../components/map/map';


@Component({
  selector: 'app-home-driver',
  standalone: true,
  imports: [CommonModule, RouterOutlet, Map],
  templateUrl: './passenger-home.html',
  styleUrl: './passenger-home.css'
})
export class HomePassenger {
}
