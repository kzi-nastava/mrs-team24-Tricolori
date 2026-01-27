import { Location } from "./location";

export interface Stop {
  address: string;
  location: Location
}

export interface Route {
    pickup: Stop;
    destination: Stop;
    stops?: Stop[];
}

export interface FavoriteRoute extends Route {
  title: string;
}
