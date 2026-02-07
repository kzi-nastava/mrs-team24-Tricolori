import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from '../../../services/auth.service';
import {ToastService} from '../../../services/toast.service';

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
  private toastService = inject(ToastService);

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (token) {
      this.authService.activateAccount(token).subscribe({
        next: (response) => {
          this.toastService.show('Account successfully activated!', "success");
          console.log("Account successfully activated!", response);
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 100);
        },
        error: (err) => {
          this.toastService.show(err, 'error');
          console.error("Error", err);
        }
      });
    }
  }

}
