package com.tricolori.backend.infrastructure.presentation.mappers;
import com.tricolori.backend.core.domain.models.*;
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

    // ================= helpers =================

    default String getPickupAddress(Ride ride) {
        if (ride.getRoute() == null || ride.getRoute().getStops().isEmpty()) {
            return null;
        }
        return ride.getRoute().getStops().getFirst().getAddress();
    }

    default String getDropoffAddress(Ride ride) {
        if (ride.getRoute() == null || ride.getRoute().getStops().isEmpty()) {
            return null;
        }
        return ride.getRoute().getStops().getLast().getAddress();
    }

    default Double getPickupLatitude(Ride ride) {
        if (ride.getRoute() == null || ride.getRoute().getStops().isEmpty()) {
            return null;
        }
        return ride.getRoute().getStops().getFirst().getLocation().getLatitude();
    }

    default Double getPickupLongitude(Ride ride) {
        if (ride.getRoute() == null || ride.getRoute().getStops().isEmpty()) {
            return null;
        }
        return ride.getRoute().getStops().getFirst().getLocation().getLongitude();
    }

    default Double getDropoffLatitude(Ride ride) {
        if (ride.getRoute() == null || ride.getRoute().getStops().isEmpty()) {
            return null;
        }
        return ride.getRoute().getStops().getLast().getLocation().getLatitude();
    }

    default Double getDropoffLongitude(Ride ride) {
        if (ride.getRoute() == null || ride.getRoute().getStops().isEmpty()) {
            return null;
        }
        return ride.getRoute().getStops().getLast().getLocation().getLongitude();
    }

    default String getPassengerFullName(Ride ride) {
        Passenger p = ride.getMainPassenger();
        return p != null ? p.getFirstName() + " " + p.getLastName() : null;
    }

    default String getPassengerPhone(Ride ride) {
        Passenger p = ride.getMainPassenger();
        return p != null ? p.getPhoneNum() : null;
    }

    default String getDriverFullName(Ride ride) {
        Driver d = ride.getDriver();
        return d != null ? d.getFirstName() + " " + d.getLastName() : null;
    }

    default String getDriverPhone(Ride ride) {
        Driver d = ride.getDriver();
        return d != null ? d.getPhoneNum() : null;
    }

    default String getVehicleModel(Ride ride) {
        return ride.getVehicleSpecification() != null
                ? ride.getVehicleSpecification().getModel()
                : null;
    }

    default String getVehicleLicensePlate(Ride ride) {
        return ride.getDriver() != null && ride.getDriver().getVehicle() != null
                ? ride.getDriver().getVehicle().getPlateNum()
                : null;
    }

    default Double getDistance(Ride ride) {
        return ride.getRoute() != null ? ride.getRoute().getDistanceKm() : null;
    }

    default Integer getDurationInMinutes(Ride ride) {
        if (ride.getRoute() == null || ride.getRoute().getEstimatedTimeSeconds() == null) {
            return null;
        }
        return (int) Math.round(ride.getRoute().getEstimatedTimeSeconds() / 60.0);
    }

    default Integer getAverageDriverRating(Ride ride) {
        return ride.getReviews().stream()
                .filter(r -> r.getDriverRating() != null)
                .mapToInt(Review::getDriverRating)
                .boxed()
                .findFirst()
                .orElse(null);
    }

    default Integer getAverageVehicleRating(Ride ride) {
        return ride.getReviews().stream()
                .filter(r -> r.getVehicleRating() != null)
                .mapToInt(Review::getVehicleRating)
                .boxed()
                .findFirst()
                .orElse(null);
    }

    default String getFirstRatingComment(Ride ride) {
        return ride.getReviews().stream()
                .map(Review::getComment)
                .filter(c -> c != null && !c.isBlank())
                .findFirst()
                .orElse(null);
    }

    @Named("statusToString")
    default String statusToString(Enum<?> status) {
        return status != null ? status.name() : null;
    }

}