import { Component, computed, inject, signal } from '@angular/core';
import { NgIcon } from "@ng-icons/core";

import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { ReportService } from '../../../services/report.service';
import { DailyStatisticDTO, ReportResponse } from '../../../model/report';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
@Component({
  selector: 'app-personal-report',
  standalone: true,
  imports: [
    NgIcon, 
    BaseChartDirective, 
    CommonModule, 
    ReactiveFormsModule
  ],
  templateUrl: './personal-report.html',
  styleUrl: './personal-report.css',
})
export class PersonalReport {
  private fb = inject(FormBuilder);
  private reportService = inject(ReportService);

  reportData = signal<ReportResponse | undefined>(undefined);
  isLoading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  reportForm = this.fb.group({
    from: ['', [Validators.required]],
    to: ['', [Validators.required]]
  });

  barChartData = computed<ChartData<'bar'>>(() => this.mapChartData('count', '#00cc92', 'Rides'));
  lineChartData = computed<ChartData<'line'>>(() => this.mapChartData('distance', '#00a2ff', 'Kilometers', true));
  moneyChartData = computed<ChartData<'bar'>>(() => this.mapChartData('money', '#6366f1', 'Money'));

  public chartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } }
  };

  generateReport(): void {
    if (this.reportForm.invalid) {
      this.errorMessage.set("Please select both dates.");
      return;
    }

    const { from, to } = this.reportForm.value;
    this.isLoading.set(true);
    this.errorMessage.set(null);
    
    // Creating date data :)
    const fromStr = this.reportService.formatLocalDateTime(new Date(from!));
    const toDate = new Date(to!);
    toDate.setHours(23, 59, 59);
    const toStr = this.reportService.formatLocalDateTime(toDate);

    this.reportService.getPersonalReport(fromStr!, toStr!).subscribe({
      next: (res) => {
        this.reportData.set(res);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.message || "Error fetching personal data.");
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