// public-ride-tracking.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-public-ride-tracking',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="tracking-container">
      <div *ngIf="loading" class="loading">
        <p>Validating your tracking link...</p>
      </div>
      
      <div *ngIf="error" class="error">
        <h2>⚠️ Invalid or Expired Link</h2>
        <p>{{ error }}</p>
        <button (click)="goHome()">Go to Home</button>
      </div>
      
      <div *ngIf="showLoginPrompt" class="login-prompt">
        <h2>Welcome Back!</h2>
        <p>We see you have an account with us. Please log in to track your ride.</p>
        <button (click)="redirectToLogin()">Log In</button>
        <button (click)="continueAsGuest()" class="secondary">Continue as Guest</button>
      </div>
    </div>
  `,
  styles: [`
    .tracking-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      padding: 20px;
    }
    
    .loading, .error, .login-prompt {
      text-align: center;
      max-width: 500px;
      padding: 40px;
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    }
    
    button {
      margin: 10px;
      padding: 12px 24px;
      border: none;
      border-radius: 5px;
      background: #00acc1;
      color: white;
      cursor: pointer;
      font-size: 16px;
    }
    
    button.secondary {
      background: #757575;
    }
    
    button:hover {
      opacity: 0.9;
    }
  `]
})
export class PublicRideTrackingComponent implements OnInit {
  loading = true;
  error: string | null = null;
  showLoginPrompt = false;
  token: string | null = null;
  rideId: number | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.token = this.route.snapshot.queryParamMap.get('token');
    
    if (!this.token) {
      this.error = 'No tracking token provided';
      this.loading = false;
      return;
    }

    this.validateToken();
  }

  validateToken() {
    this.http.get<any>(`/api/tracking/validate?token=${this.token}`)
      .subscribe({
        next: (response) => {
          if (response.valid) {
            this.rideId = response.rideId;
            
            if (response.isRegistered) {
              this.showLoginPrompt = true;
              this.loading = false;
            } else {
              // Unregistered user - go directly to tracking
              this.continueAsGuest();
            }
          } else {
            this.error = 'Invalid tracking token';
            this.loading = false;
          }
        },
        error: () => {
          this.error = 'Failed to validate tracking link';
          this.loading = false;
        }
      });
  }

  redirectToLogin() {
    this.router.navigate(['/login'], {
      queryParams: { redirect: `/passenger/ride-tracking/${this.rideId}` }
    });
  }

  continueAsGuest() {
    // Navigate to a guest tracking page with the token
    this.router.navigate(['/guest/ride-tracking'], {
      queryParams: { token: this.token }
    });
  }

  goHome() {
    this.router.navigate(['/unregistered']);
  }
}