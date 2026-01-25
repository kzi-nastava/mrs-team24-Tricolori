import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIcon } from '@ng-icons/core';
import { passwordMatchValidator, strongPasswordValidator } from '../../../shared/passwordValidator';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DriverPasswordSetupRequest, RegistrationTokenStatus } from '../../../shared/model/driver-registration';

@Component({
  selector: 'app-password-setup',
  imports: [
    ReactiveFormsModule,
    NgIcon
  ],
  templateUrl: './password-setup.html',
  styleUrl: './password-setup.css',
})
export class PasswordSetup implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);

  private token: string | null = null;

  hidePassword: boolean = true;
  hideConfirmedPassword: boolean = true;
  isTokenValid = signal(false);
  isSubmitting: boolean = false;

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get("token");

    if (!this.token) {
      this.router.navigate(['/error']); // Ili gde god želiš da baciš korisnika bez tokena
      return;
    }

    // Provera validnosti odmah pri učitavanju
    this.authService.verifyRegistrationToken(this.token).subscribe({
      next: (res) => {
        console.log(res)
        if (res === RegistrationTokenStatus.VALID) {
          this.isTokenValid.set(true);
        } else if (res === RegistrationTokenStatus.EXPIRED) {
          alert("Vaš link je istekao. Novi aktivacioni link je poslat na vaš email.");
          // TODO: if logged in /login should redirect to home...
          // this.router.navigate(['/login']);
        } else if (res === RegistrationTokenStatus.ACTIVE) {
          alert("Nalog je već aktiviran. Molimo ulogujte se.");
          //this.router.navigate(['/login']);
        } else {
          //this.router.navigate(['/login']);
          alert("Token je istekao ili ne postoji");
        }
      },
      error: (err) => {
        console.error("Greška pri verifikaciji:", err);
        alert("Link je nevažeći.");
        // TODO: create error page that displays error message.
        this.router.navigate(['/error']);
      }
    });
  }

  passwordForm = new FormGroup({
    password: new FormControl('', [Validators.required, strongPasswordValidator]),
    confirmedPassword: new FormControl('', [Validators.required])
  }, { validators: passwordMatchValidator })

  onSubmit() {
    if (this.passwordForm.invalid) {
      if (this.passwordForm.errors?.['passwordMismatch']) {
        alert('Passwords do not match!');
      } else if (this.passwordForm.get('password')?.errors?.['weakPassword']) {
        alert('Password is too weak!');
      } else {
        alert('Please fill in all fields correctly!');
      }
      return;
    }

    this.isSubmitting = true;
    
    const request: DriverPasswordSetupRequest = {
      token: this.token!,
      password: this.passwordForm.get('password')?.value!
    }

    this.authService.driverPasswordSetup(request).subscribe({
      next: (res) => {
        alert("Success! Your account is now active.");
        this.router.navigate(['/login']);
      },
      error: (err) => {
        alert(err.error || "An error occurred during activation.");
      }
    });
  }
}
