import { Component, inject, OnInit, output } from '@angular/core';
import { FavoriteRoute } from '../../../model/route';
import { MatDialogRef, MatDialogModule, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FavoriteRoutesService } from '../../../../core/services/favorite-routes.service';

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
  private data_ = inject(MAT_DIALOG_DATA);

  selectedRoute = output<FavoriteRoute>();

  favoriteRoutes = this.favRouteService.favoriteRoutes;

  ngOnInit(): void {
      const id = this.data_.userId;
      this.favRouteService.getFavoriteRoutes(id);
  }

  selectRoute(route: FavoriteRoute) {
    this.selectedRoute.emit(route);
    this.dialogRef.close(route);
  }

  close() {
    this.dialogRef.close();
  }
}
