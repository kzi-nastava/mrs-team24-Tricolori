import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ReportService } from '../../../services/report.service';
import { DailyStatisticDTO, ReportResponse, ReportScope } from '../../../model/report';
import { NgIcon } from '@ng-icons/core';
import { BaseChartDirective } from 'ng2-charts';
import { CommonModule } from '@angular/common';
import { ChartConfiguration } from 'chart.js';

@Component({
  selector: 'app-admin-report',
  imports: [
    NgIcon,
    ReactiveFormsModule,
    BaseChartDirective, 
    CommonModule, 
  ],
  templateUrl: './admin-report.html',
  styleUrl: './admin-report.css',
})
export class AdminReport {
  private fb = inject(FormBuilder);
  private reportService = inject(ReportService);

  reportData = signal<ReportResponse | undefined>(undefined);
  isLoading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);
  
  scope = signal<ReportScope>('ALL');

  reportForm = this.fb.group({
    from: ['', [Validators.required]],
    to: ['', [Validators.required]],
    userEmail: [''] // Optional, depends on the scope...
  });

  barChartData = computed(() => this.mapChartData('count', '#00cc92', 'Total Rides'));
  lineChartData = computed(() => this.mapChartData('distance', '#00a2ff', 'Total Kilometers', true));
  moneyChartData = computed(() => this.mapChartData('money', '#6366f1', 'System Revenue/Spending'));

  public chartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } }
  };

  // Postavljanje scope-a i ažuriranje validacije
  setScope(newScope: ReportScope) {
    this.scope.set(newScope);
    const emailControl = this.reportForm.get('userEmail');
    
    if (newScope === 'INDIVIDUAL') {
      emailControl?.setValidators([Validators.required, Validators.email]);
    } else {
      emailControl?.clearValidators();
      emailControl?.setValue('');
    }
    emailControl?.updateValueAndValidity();

    // Clear old report...
    this.errorMessage.set(null);
    this.isLoading.set(false);
    this.reportData.set(undefined);
  }

  generateReport(): void {
    if (this.reportForm.invalid) {
      this.errorMessage.set(
        this.scope() === 'INDIVIDUAL' ?
        "Please select both dates and email." :
        "Please select both dates."
      )

      return;
    };

    const { from, to, userEmail } = this.reportForm.value;
    this.isLoading.set(true);
    this.errorMessage.set(null);

    // Creating date data :)
    const fromStr = this.reportService.formatLocalDateTime(new Date(from!));
    const toDate = new Date(to!);
    toDate.setHours(23, 59, 59);
    const toStr = this.reportService.formatLocalDateTime(toDate);

    this.reportService.getAdminReport(fromStr!, toStr!, this.scope(), userEmail!).subscribe({
      next: (res) => {
        this.reportData.set(res);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.message || "Error fetching comprehensive data.");
        this.isLoading.set(false);
        this.reportData.set(undefined);
      }
    });
  }

  private mapChartData(key: keyof DailyStatisticDTO, color: string, label: string, isLine = false): any {
    const stats = this.reportData()?.dailyStatistics || [];
    return {
      labels: stats.map(d => d.date),
      datasets: [{
        data: stats.map(d => d[key]),
        label: label,
        backgroundColor: isLine ? 'rgba(0, 162, 255, 0.1)' : color,
        borderColor: color,
        borderRadius: isLine ? 0 : 6,
        tension: isLine ? 0.4 : 0,
        fill: isLine ? 'origin' : false
      }]
    };
  }
}
