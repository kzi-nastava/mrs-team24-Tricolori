package com.tricolori.backend.infrastructure.presentation.mappers;

import com.tricolori.backend.core.domain.models.Ride;
import com.tricolori.backend.core.domain.models.Stop;
import com.tricolori.backend.infrastructure.presentation.dtos.Ride.RideHistoryResponse;
import com.tricolori.backend.infrastructure.presentation.dtos.Ride.RideDetailResponse;
import com.tricolori.backend.infrastructure.presentation.dtos.Ride.PassengerRideHistoryResponse;
import com.tricolori.backend.infrastructure.presentation.dtos.Ride.PassengerRideDetailResponse;
import org.mapstruct.*;

import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RideMapper {

    // ==================== DRIVER MAPPINGS ====================

    /**
     * Map Ride to Driver History Response (List view)
     */
    @Mapping(target = "pickupAddress", expression = "java(getPickupAddress(ride))")
    @Mapping(target = "destinationAddress", expression = "java(getDropoffAddress(ride))")
    @Mapping(source = "createdAt", target = "startDate")
    @Mapping(source = "endTime", target = "endDate")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    RideHistoryResponse toDriverHistoryResponse(Ride ride);

    /**
     * Map Ride to Driver Detail Response (Detail view)
     */
    @Mapping(target = "passengerName", expression = "java(getPassengerFullName(ride))")
    @Mapping(target = "passengerPhone", expression = "java(getPassengerPhone(ride))")
    @Mapping(target = "driverName", expression = "java(getDriverFullName(ride))")
    @Mapping(target = "vehicleModel", expression = "java(getVehicleModel(ride))")
    @Mapping(target = "vehicleLicensePlate", expression = "java(getVehicleLicensePlate(ride))")
    @Mapping(target = "pickupAddress", expression = "java(getPickupAddress(ride))")
    @Mapping(target = "pickupLatitude", expression = "java(getPickupLatitude(ride))")
    @Mapping(target = "pickupLongitude", expression = "java(getPickupLongitude(ride))")
    @Mapping(target = "dropoffAddress", expression = "java(getDropoffAddress(ride))")
    @Mapping(target = "dropoffLatitude", expression = "java(getDropoffLatitude(ride))")
    @Mapping(target = "dropoffLongitude", expression = "java(getDropoffLongitude(ride))")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    @Mapping(source = "price", target = "totalPrice")
    @Mapping(target = "distance", expression = "java(getDistance(ride))")
    @Mapping(target = "duration", expression = "java(getDurationInMinutes(ride))")
    @Mapping(target = "driverRating", expression = "java(getAverageDriverRating(ride))")
    @Mapping(target = "vehicleRating", expression = "java(getAverageVehicleRating(ride))")
    @Mapping(target = "ratingComment", expression = "java(getFirstRatingComment(ride))")
    RideDetailResponse toDriverDetailResponse(Ride ride);

    // ==================== PASSENGER MAPPINGS ====================

    /**
     * Map Ride to Passenger History Response (List view)
     */
    @Mapping(target = "pickupAddress", expression = "java(getPickupAddress(ride))")
    @Mapping(target = "destinationAddress", expression = "java(getDropoffAddress(ride))")
    @Mapping(source = "price", target = "totalPrice")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    @Mapping(target = "driverRating", expression = "java(getAverageDriverRating(ride))")
    @Mapping(target = "vehicleRating", expression = "java(getAverageVehicleRating(ride))")
    PassengerRideHistoryResponse toPassengerHistoryResponse(Ride ride);

    /**
     * Map Ride to Passenger Detail Response (Detail view)
     */
    @Mapping(target = "driverName", expression = "java(getDriverFullName(ride))")
    @Mapping(target = "driverPhone", expression = "java(getDriverPhone(ride))")
    @Mapping(target = "vehicleModel", expression = "java(getVehicleModel(ride))")
    @Mapping(target = "vehicleLicensePlate", expression = "java(getVehicleLicensePlate(ride))")
    @Mapping(target = "vehicleColor", expression = "java(getVehicleColor(ride))")
    @Mapping(target = "pickupAddress", expression = "java(getPickupAddress(ride))")
    @Mapping(target = "pickupLatitude", expression = "java(getPickupLatitude(ride))")
    @Mapping(target = "pickupLongitude", expression = "java(getPickupLongitude(ride))")
    @Mapping(target = "dropoffAddress", expression = "java(getDropoffAddress(ride))")
    @Mapping(target = "dropoffLatitude", expression = "java(getDropoffLatitude(ride))")
    @Mapping(target = "dropoffLongitude", expression = "java(getDropoffLongitude(ride))")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    @Mapping(source = "price", target = "totalPrice")
    @Mapping(target = "distance", expression = "java(getDistance(ride))")
    @Mapping(target = "duration", expression = "java(getDurationInMinutes(ride))")
    @Mapping(target = "driverRating", expression = "java(getAverageDriverRating(ride))")
    @Mapping(target = "vehicleRating", expression = "java(getAverageVehicleRating(ride))")
    @Mapping(target = "ratingComment", expression = "java(getFirstRatingComment(ride))")
    PassengerRideDetailResponse toPassengerDetailResponse(Ride ride);

    // ==================== HELPER METHODS ====================

    /**
     * Convert status enum to string
     */
    @Named("statusToString")
    default String statusToString(Enum<?> status) {
        return status != null ? status.toString() : "UNKNOWN";
    }

    /**
     * Get pickup address from route stops
     */
    default String getPickupAddress(Ride ride) {
        if (ride.getRoute() == null || ride.getRoute().getRouteStops() == null) {
            return "Unknown";
        }
        return ride.getRoute().getRouteStops().stream()
                .min(Comparator.comparing(RouteStop::getStopIndex))
                .map(stop -> stop.getAddress() != null ? stop.getAddress() :
                        (stop.getCity() != null ? stop.getCity() : "Unknown"))
                .orElse("Unknown");
    }

    /**
     * Get dropoff address from route stops
     */
    default String getDropoffAddress(Ride ride) {
        if (ride.getRoute() == null || ride.getRoute().getRouteStops() == null) {
            return "Unknown";
        }
        return ride.getRoute().getRouteStops().stream()
                .max(Comparator.comparing(RouteStop::getStopIndex))
                .map(stop -> stop.getAddress() != null ? stop.getAddress() :
                        (stop.getCity() != null ? stop.getCity() : "Unknown"))
                .orElse("Unknown");
    }

    /**
     * Get pickup latitude
     */
    default Double getPickupLatitude(Ride ride) {
        if (ride.getRoute() == null || ride.getRoute().getRouteStops() == null) {
            return null;
        }
        return ride.getRoute().getRouteStops().stream()
                .min(Comparator.comparing(RouteStop::getStopIndex))
                .map(RouteStop::getLatitude)
                .orElse(null);
    }

    /**
     * Get pickup longitude
     */
    default Double getPickupLongitude(Ride ride) {
        if (ride.getRoute() == null || ride.getRoute().getRouteStops() == null) {
            return null;
        }
        return ride.getRoute().getRouteStops().stream()
                .min(Comparator.comparing(RouteStop::getStopIndex))
                .map(RouteStop::getLongitude)
                .orElse(null);
    }

    /**
     * Get dropoff latitude
     */
    default Double getDropoffLatitude(Ride ride) {
        if (ride.getRoute() == null || ride.getRoute().getRouteStops() == null) {
            return null;
        }
        return ride.getRoute().getRouteStops().stream()
                .max(Comparator.comparing(RouteStop::getStopIndex))
                .map(RouteStop::getLatitude)
                .orElse(null);
    }

    /**
     * Get dropoff longitude
     */
    default Double getDropoffLongitude(Ride ride) {
        if (ride.getRoute() == null || ride.getRoute().getRouteStops() == null) {
            return null;
        }
        return ride.getRoute().getRouteStops().stream()
                .max(Comparator.comparing(RouteStop::getStopIndex))
                .map(RouteStop::getLongitude)
                .orElse(null);
    }

    /**
     * Get passenger full name
     */
    default String getPassengerFullName(Ride ride) {
        if (ride.getMainPassenger() == null) {
            return "Unknown";
        }
        return ride.getMainPassenger().getFirstName() + " " + ride.getMainPassenger().getLastName();
    }

    /**
     * Get passenger phone
     */
    default String getPassengerPhone(Ride ride) {
        if (ride.getMainPassenger() == null) {
            return null;
        }
        return ride.getMainPassenger().getPhoneNumber();
    }

    /**
     * Get driver full name
     */
    default String getDriverFullName(Ride ride) {
        if (ride.getDriver() == null) {
            return "Unknown";
        }
        return ride.getDriver().getFirstName() + " " + ride.getDriver().getLastName();
    }

    /**
     * Get driver phone
     */
    default String getDriverPhone(Ride ride) {
        if (ride.getDriver() == null) {
            return null;
        }
        return ride.getDriver().getPhoneNumber();
    }

    /**
     * Get vehicle model
     */
    default String getVehicleModel(Ride ride) {
        if (ride.getVehicle() == null) {
            return "Unknown";
        }
        return ride.getVehicle().getModel();
    }

    /**
     * Get vehicle license plate
     */
    default String getVehicleLicensePlate(Ride ride) {
        if (ride.getVehicle() == null) {
            return null;
        }
        return ride.getVehicle().getLicensePlate();
    }

    /**
     * Get vehicle color
     */
    default String getVehicleColor(Ride ride) {
        if (ride.getVehicle() == null) {
            return null;
        }
        return ride.getVehicle().getColor();
    }

    /**
     * Get distance from route
     */
    default Double getDistance(Ride ride) {
        if (ride.getRoute() == null) {
            return null;
        }
        return ride.getRoute().getDistanceKm();
    }

    /**
     * Get duration in minutes from route (convert from seconds)
     */
    default Integer getDurationInMinutes(Ride ride) {
        if (ride.getRoute() == null || ride.getRoute().getEstimatedTimeSeconds() == null) {
            return null;
        }
        return (int) Math.round(ride.getRoute().getEstimatedTimeSeconds() / 60.0);
    }

    /**
     * Get average driver rating from reviews
     */
    default Integer getAverageDriverRating(Ride ride) {
        if (ride.getReviews() == null || ride.getReviews().isEmpty()) {
            return null;
        }
        double avg = ride.getReviews().stream()
                .filter(r -> r.getDriverRating() != null)
                .mapToInt(r -> r.getDriverRating())
                .average()
                .orElse(0.0);
        return avg > 0 ? (int) Math.round(avg) : null;
    }

    /**
     * Get average vehicle rating from reviews
     */
    default Integer getAverageVehicleRating(Ride ride) {
        if (ride.getReviews() == null || ride.getReviews().isEmpty()) {
            return null;
        }
        double avg = ride.getReviews().stream()
                .filter(r -> r.getVehicleRating() != null)
                .mapToInt(r -> r.getVehicleRating())
                .average()
                .orElse(0.0);
        return avg > 0 ? (int) Math.round(avg) : null;
    }

    /**
     * Get first rating comment from reviews
     */
    default String getFirstRatingComment(Ride ride) {
        if (ride.getReviews() == null || ride.getReviews().isEmpty()) {
            return null;
        }
        return ride.getReviews().stream()
                .map(r -> r.getComment())
                .filter(c -> c != null && !c.isEmpty())
                .findFirst()
                .orElse(null);
    }
}