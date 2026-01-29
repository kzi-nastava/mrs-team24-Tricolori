import { Component, OnInit } from '@angular/core';
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
  pricePerKm: number = 150;
  standardPrice: number = 150;
  luxuryPrice: number = 250;
  vanPrice: number = 225;
  
  loading: boolean = false;
  errorMessage: string = '';

  constructor(private pricelistService: PricelistService) {}

  ngOnInit() {
    this.loadCurrentPricing();
  }

  loadCurrentPricing() {
    this.loading = true;
    this.errorMessage = '';
    
    this.pricelistService.getCurrentPricing().subscribe({
      next: (response) => {
        this.pricePerKm = response.kmPrice;
        this.standardPrice = response.standardPrice;
        this.luxuryPrice = response.luxuryPrice;
        this.vanPrice = response.vanPrice;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading pricing:', error);
        this.errorMessage = 'Failed to load current pricing';
        this.loading = false;
      }
    });
  }

  onSaveChanges() {
    const priceData: PriceConfigRequest = {
      kmPrice: this.pricePerKm,
      standardPrice: this.standardPrice,
      luxuryPrice: this.luxuryPrice,
      vanPrice: this.vanPrice
    };
    
    this.loading = true;
    this.errorMessage = '';
    
    this.pricelistService.updatePricing(priceData).subscribe({
      next: () => {
        this.loading = false;
        alert('Prices updated successfully!');
      },
      error: (error) => {
        console.error('Error updating prices:', error);
        this.errorMessage = 'Failed to update prices';
        this.loading = false;
        alert('Failed to update prices. Please try again.');
      }
    });
  }
}