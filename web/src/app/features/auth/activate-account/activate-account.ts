import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from '../../../core/services/auth.service';

@Component({
  selector: 'app-activate-account',
  imports: [],
  templateUrl: './activate-account.html',
  styleUrl: './activate-account.css',
})
export class ActivateAccount implements OnInit {

  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);
  private router = inject(Router);

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (token) {
      this.authService.activateAccount(token).subscribe({
        next: (response) => {
          console.log("Success", response);
          setTimeout(() => {
            this.router.navigate(['/login'], { queryParams: { activated: true }})
          }, 100);
        },
        error: (err) => {
          console.error("Error", err);
        }
      });
    }
  }

}
