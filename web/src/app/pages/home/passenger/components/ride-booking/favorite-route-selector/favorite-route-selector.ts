import { Component, inject, OnInit, output, signal } from '@angular/core';
import { FavoriteRoute } from '../../../../../../model/route';
import { MatDialogRef, MatDialogModule, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FavoriteRoutesService } from '../../../../../../services/favorite-routes.service';

@Component({
  selector: 'app-favorite-route-selector',
  imports: [
    MatDialogModule
  ],
  templateUrl: './favorite-route-selector.html',
  styleUrl: './favorite-route-selector.css',
})

export class FavoriteRouteSelector implements OnInit {
  private dialogRef = inject(MatDialogRef<FavoriteRouteSelector>);
  private favRouteService = inject(FavoriteRoutesService);

  selectedRoute = output<FavoriteRoute>();

  favoriteRoutes = signal<FavoriteRoute[]>([]);

  ngOnInit(): void {
    this.loadFavorites();
  }

  loadFavorites(): void {
    this.favRouteService.getFavoriteRoutes().subscribe({
      next: (routes) => this.favoriteRoutes.set(routes),
      error: (err) => console.error("Greška pri učitavanju favorita:", err)
    });
  }

  selectRoute(route: FavoriteRoute) {
    this.selectedRoute.emit(route);
    this.dialogRef.close(route);
  }

  close() {
    this.dialogRef.close();
  }
}
