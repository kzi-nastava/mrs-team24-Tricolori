export interface Address {
  address: string;
  city: string;
  longitude: number;
  latitude: number;
}

export interface Route {
    pickup: Address;
    destination: Address;
    stops?: Address[];
}

export interface FavoriteRoute extends Route {
  title: string;
}
