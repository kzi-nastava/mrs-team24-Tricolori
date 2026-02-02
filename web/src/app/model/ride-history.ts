export interface PassengerRideHistoryResponse {
  id: number;
  driverName?: string;
  pickupAddress: string;
  destinationAddress: string;
  status: string;
  totalPrice: number;
  distance?: number;
  duration?: number;
  createdAt: string;
  endDate?: string | null;
  driverRating?: number | null;
  vehicleRating?: number | null;
}

export interface RideHistoryResponse {
  id: number;
  passengerName?: string;
  pickupAddress: string;
  destinationAddress: string;
  status: string;
  price: number;
  distance?: number;
  duration?: number;
  startDate: string;
  endDate: string | null;
  driverRating?: number | null;
  vehicleRating?: number | null;
}

export interface RideDetailResponse {
  id: number;
  passengerName: string;
  passengerPhone: string;
  driverName: string;
  vehicleModel: string;
  vehicleLicensePlate: string;
  pickupAddress: string;
  pickupLatitude: number;
  pickupLongitude: number;
  dropoffAddress: string;
  dropoffLatitude: number;
  dropoffLongitude: number;
  status: string;
  totalPrice: number;
  distance: number;
  duration: number;
  createdAt: string;
  acceptedAt: string;
  startedAt: string;
  completedAt: string;
  driverRating: number | null;
  vehicleRating: number | null;
  ratingComment: string | null;
}

export interface RideRatingRequest {
  driverRating: number;
  vehicleRating: number;
  comment: string;
}