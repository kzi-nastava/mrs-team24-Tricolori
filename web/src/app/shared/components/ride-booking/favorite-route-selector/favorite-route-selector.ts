import { Component, inject, output } from '@angular/core';
import { FavoriteRoute } from '../../../model/route';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';

@Component({
  selector: 'app-favorite-route-selector',
  imports: [
    MatDialogModule
  ],
  templateUrl: './favorite-route-selector.html',
  styleUrl: './favorite-route-selector.css',
})

export class FavoriteRouteSelector {
  private dialogRef = inject(MatDialogRef<FavoriteRouteSelector>);

  selectedRoute = output<FavoriteRoute>();

  favoriteRoutes: FavoriteRoute[] = [
    {
      title: 'Work - Home',
      from: 'Bulevar kralja Petra 3',
      stops: [
        'A',
        'B',
        'C',
        'F',
        'K'
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
    },
    {
      title: 'University',
      from: 'Studentska 5',
      to: 'Zmaj Jovina 22',
    },
    {
      title: 'University',
      from: 'Studentska 5',
      to: 'Zmaj Jovina 22',
    }
  ];

  selectRoute(route: FavoriteRoute) {
    this.selectedRoute.emit(route);
    this.dialogRef.close(route);
  }

  close() {
    this.dialogRef.close();
  }
}
