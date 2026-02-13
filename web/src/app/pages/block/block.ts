import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { NgIcon } from "@ng-icons/core";
import { ActivePersonStatus } from '../../model/block.model';
import { PersonService } from '../../services/person.service';
import { CommonModule, DatePipe } from '@angular/common';

@Component({
  selector: 'app-block',
  imports: [
    ReactiveFormsModule,
    NgIcon,
    CommonModule,
    DatePipe
],
  templateUrl: './block.html',
  styleUrl: './block.css',
})
export class Block {
  private fb = inject(FormBuilder);
  private personService = inject(PersonService);

  users = signal<ActivePersonStatus[]>([]);
  totalElements = signal(0);
  currentPage = signal(0);
  pageSize = 5;

  totalPages = computed(() => Math.ceil(this.totalElements() / this.pageSize));
  
  displayRange = computed(() => {
    if(this.totalElements() == 0) {
      return "No users to show";
    }

    const start = this.currentPage() * this.pageSize + 1;
    const end = Math.min((this.currentPage() + 1) * this.pageSize, this.totalElements());
    return `Showing ${start}-${end} out of ${this.totalElements()} users`;
  });

  filterForm = this.fb.group({
    id: [''],
    firstName: [''],
    lastName: [''],
    email: ['']
  });

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    const filters = this.filterForm.value;
    
    this.personService.getUsers(filters, this.currentPage(), this.pageSize)
      .subscribe({
        next: (response) => {
          this.users.set(response.content);
          this.totalElements.set(response.totalElements);
        },
        error: (err) => console.error('Greška pri učitavanju:', err)
      });
  }

  filterUsers() {
    this.currentPage.set(0);
    this.loadUsers();
  }

  resetFilters() {
    this.filterForm.reset();
    this.currentPage.set(0);
    this.loadUsers();
  }

  nextPage() {
    if ((this.currentPage() + 1) * this.pageSize < this.totalElements()) {
      this.currentPage.update(p => p + 1);
      this.loadUsers();
    }
  }

  prevPage() {
    if (this.currentPage() > 0) {
      this.currentPage.update(p => p - 1);
      this.loadUsers();
    }
  }
}
