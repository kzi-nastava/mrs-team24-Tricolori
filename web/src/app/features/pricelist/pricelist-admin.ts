import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-pricelist-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pricelist-admin.html',
  styleUrl: './pricelist-admin.css'
})
export class PricelistAdmin {
  pricePerKm: number = 150;
  standardPrice: number = 150;
  luxuryPrice: number = 250;
  vanPrice: number = 225;

  onSaveChanges() {
    const priceData = {
      pricePerKm: this.pricePerKm,
      startPrices: {
        standard: this.standardPrice,
        luxury: this.luxuryPrice,
        van: this.vanPrice
      }
    };
    
    console.log('Saving price changes:', priceData);
    
    // Call a service, for example:
    // this.priceService.updatePrices(priceData).subscribe(...);
    
    // Show success message
    alert('Prices updated successfully!');
  }
}