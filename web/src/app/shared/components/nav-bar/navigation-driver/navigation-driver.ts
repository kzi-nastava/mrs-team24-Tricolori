import { Component } from '@angular/core';
import {NgIcon} from "@ng-icons/core";
import {RouterLink, RouterLinkActive} from "@angular/router";

@Component({
  selector: 'app-navigation-driver',
    imports: [
        NgIcon,
        RouterLink,
        RouterLinkActive
    ],
  templateUrl: './navigation-driver.html',
  styleUrl: './navigation-driver.css',
})
export class NavigationDriver {

}
