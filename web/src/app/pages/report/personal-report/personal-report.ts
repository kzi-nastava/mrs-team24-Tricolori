import { Component } from '@angular/core';
import { NgIcon } from "@ng-icons/core";

import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';

@Component({
  selector: 'app-personal-report',
  imports: [
    NgIcon, 
    BaseChartDirective
  ],
  templateUrl: './personal-report.html',
  styleUrl: './personal-report.css',
})
export class PersonalReport {

  public barChartData: ChartData<'bar'> = {
    labels: ['01.01', '02.01', '03.01', '04.01', '05.01'], // Ovo puniš iz DTO.dailyData
    datasets: [
      { 
        data: [12, 15, 8, 18, 14], // d.rideCount
        label: 'Broj vožnji',
        backgroundColor: '#00cc92', // Tvoja --color-light-600
        borderRadius: 6, // Zaobljeni vrhovi stubića kao na slici
      }
    ]
  };

  // 2. Podaci za Kilometre (Line Chart)
  public lineChartData: ChartData<'line'> = {
    labels: ['01.01', '02.01', '03.01', '04.01', '05.01'],
    datasets: [
      {
        data: [140, 180, 90, 210, 160], // d.distance
        label: 'Kilometri',
        borderColor: '#00a2ff', // Tvoja --color-dark
        pointBackgroundColor: '#00a2ff',
        tension: 0.4, // Ovo pravi "smooth" krivu liniju sa slike
        fill: 'origin',
        backgroundColor: 'rgba(0, 162, 255, 0.1)', // Blaga senka ispod linije
      }
    ]
  };

  public chartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false } // Sakrivamo legendu jer imamo naslov u HTML-u
    },
    scales: {
      x: {
        grid: { display: false } // Sklanjamo vertikalne linije
      },
      y: {
        border: { display: false },
        grid: { color: '#f3f4f6' } // Svetlo sive horizontalne linije
      }
    }
  };
}
