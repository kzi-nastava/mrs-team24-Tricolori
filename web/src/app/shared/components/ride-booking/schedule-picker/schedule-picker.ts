import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { NgIcon } from '@ng-icons/core';

@Component({
  selector: 'app-schedule-picker',
  imports: [
    DatePipe
  ],
  templateUrl: './schedule-picker.html',
  styleUrl: './schedule-picker.css',
})

export class SchedulePicker {
  private dialogRef = inject(MatDialogRef<SchedulePicker>);
  
  // Generišemo slotove za narednih 5 sati (po tvojoj specifikaciji)
  timeSlots = signal<Date[]>(this.generateTimeSlots());
  selectedTime = signal<Date | null>(null);
  isCustomMode = signal(false);
  customTimeValue = signal(''); // Čuva string "HH:mm" iz inputa

  toggleCustomMode() {
    this.isCustomMode.set(!this.isCustomMode());
    this.selectedTime.set(null);
  }

  onCustomTimeChange(event: Event) {
    const timeString = (event.target as HTMLInputElement).value;
    this.customTimeValue.set(timeString);
    
    if (timeString) {
      const [hours, minutes] = timeString.split(':').map(Number);
      const now = new Date();
      const selectedDate = new Date();
      
      selectedDate.setHours(hours, minutes, 0, 0);

      // Set day to "tomorrow" if the time is selected in the past
      if (selectedDate < now) {
        selectedDate.setDate(now.getDate() + 1);
      }

      this.selectedTime.set(selectedDate);
    }
  }

  generateTimeSlots(): Date[] {
    const slots: Date[] = [];
    const now = new Date();
    
    // Start from nearest quarter:
    let startTime = new Date(Math.ceil(now.getTime() / (15 * 60000)) * (15 * 60000));

     // 15 min slot x 20 slots = 5 hours:
    for (let i = 0; i < 20; i++) {
      slots.push(new Date(startTime.getTime() + i * 15 * 60000));
    }

    return slots;
  }

  selectTimeSlot(time: Date) {
    // Add this check for toggling selection:
    if(this.selectedTime() === time) {
      this.selectedTime.set(null);
    } else {
      this.selectedTime.set(time);
    }
  }

  confirmDate() {
    if (this.selectedTime()) {
      this.dialogRef.close(this.selectedTime());
    }
  }
}
