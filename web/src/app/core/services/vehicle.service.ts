import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Vehicle } from '../../shared/model/vehicle.model';
import {environment} from '../../../environments/environment';


@Injectable({
  providedIn: 'root'
})
export class VehicleService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl + '/vehicles';

  getActiveVehicles(): Observable<Vehicle[]> {
    console.log('🌐 Making HTTP request to:', `${this.apiUrl}/active`);
    return this.http.get<Vehicle[]>(`${this.apiUrl}/active`).pipe(
      tap(response => console.log('📡 HTTP Response received:', response))
    );
  }
}