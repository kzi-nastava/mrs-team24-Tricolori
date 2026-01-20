import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { LoginRequest, LoginResponse, PersonDto, PersonRole } from '../../shared/model/auth.model';
import { HttpClient } from '@angular/common/http';


@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router)

  private readonly API_URL = 'http://localhost:8080/api/v1/auth';

  private currentPersonSubject = new BehaviorSubject<PersonDto | null>(null);
  public currentPerson$ = this.currentPersonSubject.asObservable();

  constructor() {
    const storedUser = localStorage.getItem('currentUser');
    if (storedUser) {
      this.currentPersonSubject.next(JSON.parse(storedUser));
    }
  }

  login(loginRequest: LoginRequest): Observable<LoginResponse> {

    return this.http.post<LoginResponse>(`${this.API_URL}/login`, loginRequest).pipe(
      tap(response => {

        localStorage.setItem('access_token', response.accessToken);
        localStorage.setItem('person_data', JSON.stringify(response.personDto));

        this.currentPersonSubject.next(response.personDto);

        this.navigateByRole(response.personDto.role);
      })
    )
  }

  logout(): void {
    localStorage.clear();
    this.currentPersonSubject.next(null);
    this.router.navigate(['/login']);
  }

  getCurrentUser(): PersonDto | null {
    return this.currentPersonSubject.value;
  }

  getPersonRole(): PersonRole | null {
    return this.currentPersonSubject.value?.role || null;
  }

  isAuthenticated(): boolean {
    return this.currentPersonSubject.value !== null;
  }

  private navigateByRole(role: PersonRole): void {
    switch (role) {
      case 'ROLE_DRIVER':
        this.router.navigate(['/driver']);
        break;

      case 'ROLE_PASSENGER':
        this.router.navigate(['/home']);
        break;

      case 'ROLE_ADMIN':
        this.router.navigate(['/admin']);
        break;

      default:
        this.router.navigate(['/']);
    }
  }

}
