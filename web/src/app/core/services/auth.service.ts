import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of, delay } from 'rxjs';
import { Router } from '@angular/router';
import { UserRole } from '../../shared/model/user-role';

export interface User {
  id: number;
  email: string;
  name: string;
  role: UserRole;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  // Mock users database
  private mockUsers: User[] = [
    { id: 1, email: 'driver@test.com', name: 'John Driver', role: 'driver' },
    { id: 2, email: 'passenger@test.com', name: 'Jane Passenger', role: 'user' },
    { id: 3, email: 'admin@test.com', name: 'Admin User', role: 'admin' }
  ];

  constructor(private router: Router) {
    // Check if user is stored in localStorage
    const storedUser = localStorage.getItem('currentUser');
    if (storedUser) {
      this.currentUserSubject.next(JSON.parse(storedUser));
    }
  }

  // Mock login - accepts any password
login(email: string, password: string): Observable<User | null> {
  return new Observable(observer => {
    setTimeout(() => {
      const user = this.mockUsers.find(u => u.email === email);

      if (user) {
        // Store user
        localStorage.setItem('currentUser', JSON.stringify(user));
        this.currentUserSubject.next(user);

        // role based redirection
        this.navigateByRole(user.role);

        observer.next(user);
      } else {
        observer.error({ message: 'Invalid credentials' });
      }

      observer.complete();
    }, 500);
  });
}

  // Mock register - creates new user
  register(email: string, password: string, name: string, role: UserRole = 'user'): Observable<User> {
    return new Observable(observer => {
      setTimeout(() => {
        // Check if user already exists
        const existingUser = this.mockUsers.find(u => u.email === email);
        
        if (existingUser) {
          observer.error({ message: 'Email already exists' });
        } else {
          // Create new user
          const newUser: User = {
            id: this.mockUsers.length + 1,
            email,
            name,
            role
          };
          
          this.mockUsers.push(newUser);
          observer.next(newUser);
        }
        observer.complete();
      }, 500);
    });
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
    this.router.navigate(['/']);
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  getUserRole(): UserRole | null {
    return this.currentUserSubject.value?.role || null;
  }

  isAuthenticated(): boolean {
    return this.currentUserSubject.value !== null;
  }

  private navigateByRole(role: UserRole): void {
  switch (role) {
    case 'driver':
      this.router.navigate(['/driver']);
      break;

    case 'user': // passenger
      this.router.navigate(['/home']);
      break;

    case 'admin':
      this.router.navigate(['/admin']);
      break;

    default:
      this.router.navigate(['/']);
  }
}

}

