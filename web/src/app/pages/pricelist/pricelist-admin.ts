import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PricelistService, PriceConfigRequest } from '../../services/pricelist.service';

@Component({
  selector: 'app-pricelist-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pricelist-admin.html',
  styleUrl: './pricelist-admin.css'
})
export class PricelistAdmin implements OnInit {

  pricePerKm: number = 0;
  standardPrice: number = 0;
  luxuryPrice: number = 0;
  vanPrice: number = 0;

  initialLoading = true;
  loading = false;
  showSuccessModal = false;

  constructor(
    private pricelistService: PricelistService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadCurrentPricing();
  }

  loadCurrentPricing(): void {
    this.initialLoading = true;

    this.pricelistService.getCurrentPricing().subscribe({
      next: (response) => {

        this.pricePerKm = response.kmPrice;
        this.standardPrice = response.standardPrice;
        this.luxuryPrice = response.luxuryPrice;
        this.vanPrice = response.vanPrice;

        this.initialLoading = false;
        this.cdr.detectChanges(); 
      },
      error: (error) => {
        console.error('Error loading pricing:', error);
        this.initialLoading = false;
        this.cdr.detectChanges(); 
      }
    });
  }

  onSaveChanges(): void {
  const priceData: PriceConfigRequest = {
    kmPrice: this.pricePerKm,
    standardPrice: this.standardPrice,
    luxuryPrice: this.luxuryPrice,
    vanPrice: this.vanPrice
  };

  this.loading = true;
  this.cdr.detectChanges();  

  this.pricelistService.updatePricing(priceData).subscribe({
    next: () => {
      this.loading = false;
      this.showSuccessModal = true;
      this.cdr.detectChanges();  

      setTimeout(() => {
        this.showSuccessModal = false;
        this.cdr.detectChanges(); 
      }, 2000);
    },
    error: (error) => {
      console.error('Error updating prices:', error);
      this.loading = false;
      this.cdr.detectChanges(); 
      alert('Failed to update prices. Please try again.');
    }
  });
  }
}