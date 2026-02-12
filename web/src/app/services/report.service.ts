import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PersonalReportResponse } from '../model/report';

@Injectable({
  providedIn: 'root',
})
export class ReportService {
  readonly API_URL = `${environment.apiUrl}/reports`;

  constructor(private http: HttpClient) {}

  getPersonalReport(from: string, to: string): Observable<PersonalReportResponse> {
    const params = new HttpParams()
      .set('from', from)
      .set('to', to);
    
    return this.http.get<PersonalReportResponse>(`${this.API_URL}/personal`, { params });
  }

  formatLocalDateTime(date: Date | null): string | null {
    if (!date) return null;
    const pad = (n: number) => n < 10 ? '0' + n : n;
    return date.getFullYear() + '-' +
      pad(date.getMonth() + 1) + '-' +
      pad(date.getDate()) + 'T' +
      pad(date.getHours()) + ':' +
      pad(date.getMinutes()) + ':' +
      pad(date.getSeconds());
  }
}
