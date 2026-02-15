import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Vehicle, VehicleLocationUpdate } from '../model/vehicle.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class VehicleService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl + '/vehicles';

  getActiveVehicles(): Observable<Vehicle[]> {
    return this.http.get<Vehicle[]>(`${this.apiUrl}/active`);
  }
  updateVehicleLocation(vehicleId: number, location: VehicleLocationUpdate): Observable<Vehicle> {
    return this.http.put<Vehicle>(`${this.apiUrl}/${vehicleId}/location`, location);
  }
}
