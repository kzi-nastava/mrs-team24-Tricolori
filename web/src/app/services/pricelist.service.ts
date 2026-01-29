import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface PriceConfigResponse {
  standardPrice: number;
  luxuryPrice: number;
  vanPrice: number;
  kmPrice: number;
  createdAt: string;
}

export interface PriceConfigRequest {
  standardPrice: number;
  luxuryPrice: number;
  vanPrice: number;
  kmPrice: number;
}

@Injectable({
  providedIn: 'root'
})
export class PricelistService {
  private apiUrl = `${environment.apiUrl}/api/v1/pricelist`;

  constructor(private http: HttpClient) {}

  getCurrentPricing(): Observable<PriceConfigResponse> {
    return this.http.get<PriceConfigResponse>(this.apiUrl);
  }

  updatePricing(request: PriceConfigRequest): Observable<void> {
    return this.http.put<void>(this.apiUrl, request);
  }
}