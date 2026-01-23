import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { ProfileRequest, ProfileResponse } from '../../shared/model/profile.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ProfileService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/profiles`;

  getMyProfile() : Observable<ProfileResponse> {
    return this.http.get<ProfileResponse>(`${this.API_URL}/me`);
  }

  updateProfile(request: ProfileRequest) : Observable<ProfileResponse> {
    return this.http.put<ProfileResponse>(`${this.API_URL}/me`, request);
  }
}
