import { Route } from "./route";
import { VehicleType } from "./vehicle.model";

export interface RideOptions {
    vehicleType: VehicleType;
    petFriendly: boolean;
    babyFriendly: boolean;
    schedule?: Date;
}

export interface RideRequest {
    route: Route;
    preferences: RideOptions;
    trackers: string[];
}
