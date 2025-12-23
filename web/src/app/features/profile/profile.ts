import { Component, ElementRef, ViewChild } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-profile',
  imports: [RouterLink],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile {
  editEnabled = false;

  firstName = "Joe";
  lastName = "Doe";
  phoneNumber = "+381601234567";
  homeAddress = "123 Main Street, Belgrade";

  @ViewChild('firstNameInput') firstNameInput!: ElementRef<HTMLInputElement>;
  @ViewChild('lastNameInput') lastNameInput!: ElementRef<HTMLInputElement>;
  @ViewChild('addressInput') addressInput!: ElementRef<HTMLInputElement>;
  @ViewChild('phoneInput') phoneInput!: ElementRef<HTMLInputElement>;

  resetChanges() {
    this.editEnabled = false;

    const changed =
      this.firstName !== this.firstNameInput.nativeElement.value ||
      this.lastName !== this.lastNameInput.nativeElement.value ||
      this.phoneNumber !== this.phoneInput.nativeElement.value ||
      this.homeAddress !== this.addressInput.nativeElement.value;

    if (changed) {
      this.firstNameInput.nativeElement.value = this.firstName;
      this.lastNameInput.nativeElement.value = this.lastName;
      this.phoneInput.nativeElement.value = this.phoneNumber;
      this.addressInput.nativeElement.value = this.homeAddress;

      // TODO: send change request...
    }
  }

}
