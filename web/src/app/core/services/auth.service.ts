import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { LoginRequest, LoginResponse, PersonDto, PersonRole, RegisterRequest } from '../../shared/model/auth.model';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private readonly API_URL = 'http://localhost:8080/api/v1/auth';
  
  private currentPersonSubject = new BehaviorSubject<PersonDto | null>(null);
  public currentPerson$ = this.currentPersonSubject.asObservable();

  constructor() {
    // Load user from localStorage on app startup
    const storedUser = localStorage.getItem('person_data');
    const storedToken = localStorage.getItem('access_token');
    
    console.log('🔄 AuthService initialized');
    console.log('📦 Stored user:', storedUser ? 'Found' : 'Not found');
    console.log('🔑 Stored token:', storedToken ? 'Found' : 'Not found');
    
    if (storedUser && storedToken) {
      this.currentPersonSubject.next(JSON.parse(storedUser));
      console.log('✅ User restored from localStorage');
    }
  }

  registerPassenger(data: RegisterRequest, pfp?: File) : Observable<string> {
    const formData = new FormData();

    const jsonBlob = new Blob([JSON.stringify(data)], {
      type: 'application/json'
    });

    formData.append('data', jsonBlob);

    if (pfp) {
      formData.append('image', pfp);
    }

    return this.http.post(`${ this.API_URL }/register-passenger`, formData, { responseType: 'text' });
  }

  activateAccount(token: string) : Observable<string> {
    return this.http.get(`${ this.API_URL }/activate`, {
      params: {token},
      responseType: 'text'
    });
  }

  login(loginRequest: LoginRequest): Observable<LoginResponse> {
    console.log('🔐 Attempting login for:', loginRequest.email);
    
    return this.http.post<LoginResponse>(`${this.API_URL}/login`, loginRequest).pipe(
      tap(response => {
        console.log('✅ Login successful, storing data...');
        
        // Store token first
        localStorage.setItem('access_token', response.accessToken);
        console.log('✅ Token stored:', response.accessToken.substring(0, 20) + '...');
        
        // Store person data
        localStorage.setItem('person_data', JSON.stringify(response.personDto));
        console.log('✅ Person data stored:', response.personDto.email);
        
        // Update BehaviorSubject
        this.currentPersonSubject.next(response.personDto);
        console.log('✅ BehaviorSubject updated');
        
        // Navigate by role
        this.navigateByRole(response.personDto.role);
      })
    );
  }

  logout(): void {
    console.log('🚪 Logging out...');
    localStorage.removeItem('access_token');
    localStorage.removeItem('person_data');
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
    const hasUser = this.currentPersonSubject.value !== null;
    const hasToken = !!localStorage.getItem('access_token');
    console.log('🔍 Auth check - User:', hasUser, 'Token:', hasToken);
    return hasUser && hasToken;
  }

  private navigateByRole(role: PersonRole): void {
    console.log('🧭 Navigating for role:', role);
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