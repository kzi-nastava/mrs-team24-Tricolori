import { Component } from '@angular/core';
import { FavoriteRoute } from '../../../model/route';

@Component({
  selector: 'app-favorite-route-selector',
  imports: [],
  templateUrl: './favorite-route-selector.html',
  styleUrl: './favorite-route-selector.css',
})

export class FavoriteRouteSelector {
  favoriteRoutes: FavoriteRoute[] = [
    {
      title: 'Work - Home',
      from: 'Bulevar kralja Petra 3',
      stops: [
        'A',
        'B',
        'C'
      ],
      to: 'Laze Telečkog 13',
    },
    {
      title: 'Gym',
      from: 'Cara Dušana 10',
      stops: [
        'G'
      ],
      to: 'Bulevar Oslobođenja 120',
    },
    {
      title: 'University',
      from: 'Studentska 5',
      to: 'Zmaj Jovina 22',
    }
  ];
}
