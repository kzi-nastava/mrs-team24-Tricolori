export interface Stop {
  address: string;
  longitude: number;
  latitude: number;
}

export interface Route {
    pickup: Stop;
    destination: Stop;
    stops?: Stop[];
}

export interface FavoriteRoute extends Route {
  title: string;
}
