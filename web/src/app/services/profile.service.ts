import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { ProfileRequest, ProfileResponse } from '../model/profile.model';
import { Observable } from 'rxjs';

export interface ChangeDriverProfileDTO {
  firstName: string;
  lastName: string;
  phoneNum: string;
  homeAddress: string;
  pfpUrl: string;
}

export interface ChangeDataRequestResponse {
  id: number;
  driverId: number;
  email: string;
  oldValues: ChangeDriverProfileDTO;
  newValues: ChangeDriverProfileDTO;
  createdAt: string;
}

@Injectable({
  providedIn: 'root',
})
export class ProfileService {
  private http = inject(HttpClient);
  private readonly PROFILE_API_URL = `${environment.apiUrl}/profiles`;
  private readonly REQUEST_API_URL = `${environment.apiUrl}/change-requests`;

  getMyProfile() : Observable<ProfileResponse> {
    return this.http.get<ProfileResponse>(`${this.PROFILE_API_URL}/me`);
  }

  updateProfile(request: ProfileRequest) : Observable<ProfileResponse> {
    return this.http.put<ProfileResponse>(`${this.PROFILE_API_URL}/me`, request);
  }

  uploadPfp(data: FormData): Observable<{url: string}> {
    return this.http.post<{url: string}>(`${this.PROFILE_API_URL}/upload-pfp`, data);
  }


  // Change requests:
  getPendingRequests(): Observable<ChangeDataRequestResponse[]> {
    return this.http.get<ChangeDataRequestResponse[]>(`${this.REQUEST_API_URL}`);
  }

  approveRequest(requestId: number): Observable<void> {
    return this.http.put<void>(`${this.REQUEST_API_URL}/approve/${requestId}`, {});
  }

  rejectRequest(requestId: number): Observable<void> {
    return this.http.put<void>(`${this.REQUEST_API_URL}/reject/${requestId}`, {});
  }

  createChangeRequest(request: ProfileRequest): Observable<void> {
    return this.http.post<void>(`${this.REQUEST_API_URL}/create`, request);
  }
}
