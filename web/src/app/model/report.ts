export interface PersonalReportResponse {
    totalCount: number;
    totalDistance: number;
    totalMoney: number;    
    averageCount: number;
    averageDistance: number;
    averageMoney: number;
    dailyStatistics: DailyStatisticDTO[];
}

export interface DailyStatisticDTO {
    date: string;
    count: number;
    distance: number;
    money: number;
}
