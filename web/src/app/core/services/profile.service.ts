import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { ProfileResponse } from '../../shared/model/profile.model';

@Injectable({
  providedIn: 'root',
})
export class ProfileService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/profiles`;

  getMyProfile() {
    return this.http.get<ProfileResponse>(`${this.API_URL}/me`);
  }
}
