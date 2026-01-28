import { Component, inject, OnInit, signal } from '@angular/core';
import { ChangeDataRequestResponse, ProfileService } from '../../../services/profile.service';


@Component({
  selector: 'app-change-requests',
  imports: [],
  templateUrl: './change-requests.html',
  styleUrl: './change-requests.css',
})
export class ChangeRequests implements OnInit {
  private profileService = inject(ProfileService);
  
  // Inicijalizuj signal
  requests = signal<ChangeDataRequestResponse[]>([]);

  ngOnInit() {
    this.loadRequests();
  }

  loadRequests() {
    this.profileService.getPendingRequests().subscribe({
      next: res => {
        console.log("Stigli podaci:", res);
        this.requests.set(res); // Postavi vrednost signala
      },
      error: err => console.log(err)
    });
  }

  approve(id: number) {
    this.profileService.approveRequest(id).subscribe(() => {
      // Filtriraj niz unutar signala (update metoda uzima trenutnu vrednost i menja je)
      this.requests.update((prev: any) => prev.filter((r: any) => r.id !== id));
    });
  }

  reject(id: number) {
    this.profileService.rejectRequest(id).subscribe(() => {
      this.requests.update((prev: any) => prev.filter((r: any) => r.id !== id));
    });
  }

  // Koristi any ili specifičan tip, ali pazi na null-check
  hasChanged(req: ChangeDataRequestResponse, field: string): boolean {
    const oldVal = (req.oldValues as any)[field];
    const newVal = (req.newValues as any)[field];
    return oldVal !== newVal;
  }
}
