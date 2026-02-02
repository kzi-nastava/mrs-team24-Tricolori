import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class DriverDailyLogService {
  private http = inject(HttpClient);

  readonly API_URL = `${environment.apiUrl}/driver-daily-logs`;

  changeStatus(active: boolean) : Observable<void> {
    return this.http.patch<void>(`${this.API_URL}/status`, { active: active });
  }

}
