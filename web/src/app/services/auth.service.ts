import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import {
  ForgotPasswordRequest,
  LoginRequest,
  LoginResponse,
  PersonDto,
  PersonRole,
  RegisterRequest,
  ResetPasswordRequest
} from '../model/auth.model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { AdminDriverRegistrationRequest, DriverPasswordSetupRequest } from '../model/driver-registration';
import { NotificationService } from './notification.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private notificationService = inject(NotificationService);
  private readonly API_URL = `${environment.apiUrl}/auth`;

  private currentPersonSubject = new BehaviorSubject<PersonDto | null>(null);
  public currentPerson$ = this.currentPersonSubject.asObservable();

  constructor() {
    const storedUser = localStorage.getItem('person_data');
    const storedToken = localStorage.getItem('access_token');

    if (storedUser && storedToken) {
      const person = JSON.parse(storedUser);
      this.currentPersonSubject.next(person);
      this.initializeNotifications(person);
    }
  }

  registerPassenger(data: RegisterRequest, pfp?: File): Observable<string> {
    const formData = new FormData();

    const jsonBlob = new Blob([JSON.stringify(data)], {
      type: 'application/json'
    });

    formData.append('data', jsonBlob);

    if (pfp) {
      formData.append('image', pfp);
    }

    return this.http.post(`${this.API_URL}/register-passenger`, formData, { responseType: 'text' });
  }

  activateAccount(token: string): Observable<string> {
    return this.http.get(`${this.API_URL}/activate`, {
      params: { token },
      responseType: 'text'
    });
  }

  verifyRegistrationToken(token: string): Observable<string> {
    return this.http.get(`${this.API_URL}/verify-token/${token}`, {
      responseType: 'text'
    });
  }

  registerDriver(dataRequest: AdminDriverRegistrationRequest, pfpFile: File | null | undefined): Observable<string> {
    const formData = new FormData();

    const jsonBlob = new Blob([JSON.stringify(dataRequest)], {
      type: 'application/json'
    });
    formData.append('data', jsonBlob);

    if (pfpFile) {
      formData.append('image', pfpFile);
    }

    console.log(formData);
    return this.http.post(`${this.API_URL}/register-driver`, formData, { responseType: 'text' });
  }

  driverPasswordSetup(request: DriverPasswordSetupRequest): Observable<string> {
    return this.http.post(`${this.API_URL}/driver-activate`, request, { responseType: 'text' });
  }

  login(loginRequest: LoginRequest): Observable<LoginResponse> {
    console.log('🔐 Attempting login for:', loginRequest.email);

    return this.http.post<LoginResponse>(`${this.API_URL}/login`, loginRequest).pipe(
      tap(response => {
        console.log('✅ Login successful, storing data...');

        localStorage.setItem('access_token', response.accessToken);
        console.log('✅ Token stored:', response.accessToken.substring(0, 20) + '...');

        localStorage.setItem('person_data', JSON.stringify(response.personDto));
        console.log('✅ Person data stored:', response.personDto.email);

        this.currentPersonSubject.next(response.personDto);
        console.log('✅ BehaviorSubject updated');

        this.initializeNotifications(response.personDto);

        this.navigateByRole(response.personDto.role);
      })
    );
  }

  logout(): void {
    console.log('🚪 Logging out...');
    
    this.notificationService.disconnectWebSocket();
    
    localStorage.removeItem('access_token');
    localStorage.removeItem('person_data');
    this.currentPersonSubject.next(null);
    this.router.navigate(['/login']);
  }

  forgotPassword(request: ForgotPasswordRequest): Observable<string> {
    return this.http.post(`${this.API_URL}/forgot-password`, request, { responseType: 'text' });
  }

  resetPassword(request: ResetPasswordRequest): Observable<string> {
    return this.http.post(`${this.API_URL}/reset-password`, request, { responseType: 'text' });
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

  private initializeNotifications(person: PersonDto): void {
    if (person.email) {
      this.notificationService.connectWebSocket(person.email);
      
      this.notificationService.getUnreadCount().subscribe({
        next: (count) => {
          console.log('📬 Initial unread count:', count);
        },
        error: (error) => {
          console.error('❌ Error loading unread count:', error);
        }
      });
    }
  }

  private navigateByRole(role: PersonRole): void {
    console.log('🧭 Navigating for role:', role);
    switch (role) {
      case 'ROLE_DRIVER':
        this.router.navigate(['/driver']);
        break;
      case 'ROLE_PASSENGER':
        this.router.navigate(['/passenger']);
        break;
      case 'ROLE_ADMIN':
        this.router.navigate(['/admin']);
        break;
      default:
        this.router.navigate(['/']);
    }
  }
}