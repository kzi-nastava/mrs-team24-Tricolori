export interface Route {
    from: string;
    stops?: string[];
    to: string;
}

export interface FavoriteRoute extends Route {
  title: string;
}
