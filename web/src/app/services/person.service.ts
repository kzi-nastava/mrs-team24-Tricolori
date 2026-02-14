import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../environments/environment";
import { Observable } from "rxjs";
import { ActivePersonStatus, BlockRequest } from "../model/block.model";

// This interface describes what Spring Boot returns...
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root',
})
export class PersonService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/persons`;

  getUsers(filters: any, page: number, size: number): Observable<PageResponse<ActivePersonStatus>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    Object.keys(filters).forEach((key) => {
      const value = filters[key];
      if (value !== null && value !== undefined && value !== '') {
        params = params.set(key, value);
      }
    });

    return this.http.get<PageResponse<ActivePersonStatus>>(`${this.API_URL}/statuses`, { params });
  }

  applyBlock(request: BlockRequest): Observable<void> {
    return this.http.patch<void>(`${this.API_URL}/block`, request);
  }


  removeBlock(email: string): Observable<void> {
    const params = new HttpParams().set('email', email);
    return this.http.delete<void>(`${this.API_URL}/unblock`, { params });
  }
}