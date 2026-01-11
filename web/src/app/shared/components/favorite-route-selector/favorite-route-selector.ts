import { Component } from '@angular/core';

interface FavoriteRoute {
  title: string;
  from: string;
  to: string;
}

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
      to: 'Laze Telečkog 13',
    },
    {
      title: 'Gym',
      from: 'Cara Dušana 10',
      to: 'Bulevar Oslobođenja 120',
    },
    {
      title: 'University',
      from: 'Studentska 5',
      to: 'Zmaj Jovina 22',
    }
  ];
}
