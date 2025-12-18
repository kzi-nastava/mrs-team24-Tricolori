import { Component } from '@angular/core';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {passwordMatchValidator, strongPasswordValidator} from '../../shared/passwordValidator';
import {RouterLink} from '@angular/router';
import {NgIcon} from '@ng-icons/core';

@Component({
  selector: 'app-register',
  imports: [
    FormsModule,
    ReactiveFormsModule,
    RouterLink,
    NgIcon,
  ],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  registerForm: FormGroup;
  hidePassword = true;
  hideRepeatPassword = true;
  selectedFileName: string | null = null;
  imgPreview: string | ArrayBuffer | null = null;

  constructor() {
    this.registerForm = new FormGroup({
      firstName: new FormControl('', Validators.required),
      lastName: new FormControl('', Validators.required),
      phone: new FormControl('', Validators.required),
      address: new FormControl('', Validators.required),
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required, strongPasswordValidator]),
      repeatPassword: new FormControl('', Validators.required)
    }, { validators: passwordMatchValidator });
  }

  onSubmit() {
    if (this.registerForm.valid) {
      console.log('Form Submitted', this.registerForm.value);
    }
  }

  onFileSelected(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      this.selectedFileName = file.name;

      // Create preview
      const reader = new FileReader();
      reader.onload = () => this.imgPreview = reader.result;
      reader.readAsDataURL(file);
    }
  }
}
