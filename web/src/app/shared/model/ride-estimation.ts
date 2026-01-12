export type EstimationState = 'INITIAL' | 'FORM' | 'RESULT';

export interface EstimateResults {
  pickup: string;
  destination: string;
  distance: number;
  duration: number;
}

export interface RideOption {
  type: string;
  icon: string;
  eta: string;
  price: string;
  seats: string;
}

export interface Vehicle {
  id: number;
  lat: number;
  lng: number;
  status: 'available' | 'occupied';
}
