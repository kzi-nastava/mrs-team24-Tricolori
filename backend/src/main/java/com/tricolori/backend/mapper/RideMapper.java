package com.tricolori.backend.mapper;
import com.tricolori.backend.dto.history.AdminRideHistoryResponse;
import com.tricolori.backend.dto.ride.*;
import com.tricolori.backend.dto.vehicle.VehicleLocationResponse;
import com.tricolori.backend.entity.Driver;
import com.tricolori.backend.entity.Passenger;
import com.tricolori.backend.entity.Review;
import com.tricolori.backend.entity.Ride;
import org.mapstruct.*;

import java.time.LocalDateTime;

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
    @Mapping(
            target = "distance",
            expression = "java(getRouteDistanceKm(ride))"
    )
    RideHistoryResponse toDriverHistoryResponse(Ride ride);

    /**
     * Map Ride to Driver Detail Response (Detail view)
     */
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "startTime", target = "startedAt")
    @Mapping(source = "endTime", target = "completedAt")
    @Mapping(target = "passengerName", expression = "java(getPassengerFullName(ride))")
    @Mapping(target = "passengerPhone", expression = "java(getPassengerPhone(ride))")
    @Mapping(target = "pickupAddress", expression = "java(getPickupAddress(ride))")
    @Mapping(target = "pickupLatitude", expression = "java(getPickupLatitude(ride))")
    @Mapping(target = "pickupLongitude", expression = "java(getPickupLongitude(ride))")
    @Mapping(target = "dropoffAddress", expression = "java(getDropoffAddress(ride))")
    @Mapping(target = "dropoffLatitude", expression = "java(getDropoffLatitude(ride))")
    @Mapping(target = "dropoffLongitude", expression = "java(getDropoffLongitude(ride))")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    @Mapping(source = "price", target = "totalPrice")
    @Mapping(target = "distance", expression = "java(getDistance(ride))")
    @Mapping(target = "duration", expression = "java(getDuration(ride))")
    @Mapping(target = "driverRating", expression = "java(getAverageDriverRating(ride))")
    @Mapping(target = "vehicleRating", expression = "java(getAverageVehicleRating(ride))")
    @Mapping(target = "ratingComment", expression = "java(getFirstRatingComment(ride))")
    RideDetailResponse toDriverDetailResponse(Ride ride);


    // ==================== PASSENGER MAPPINGS ====================

    /**
     * Map Ride to Passenger History Response (List view)
     */
    @Mapping(target = "pickupAddress", expression = "java(getPickupAddress(ride))")
    @Mapping(target = "destinationAddress", expression = "java(getDestinationAddress(ride))")
    PassengerRideHistoryResponse toPassengerHistoryResponse(Ride ride);

    /**
     * Map Ride to Admin History Response (List view)
     */
    @Mapping(target = "pickupAddress", expression = "java(getPickupAddress(ride))")
    @Mapping(target = "destinationAddress", expression = "java(getDestinationAddress(ride))")
    AdminRideHistoryResponse toAdminHistoryResponse(Ride ride);

    /**
     * Map Ride to Passenger Detail Response (Detail view)
     */
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "startTime", target = "startedAt")
    @Mapping(source = "endTime", target = "completedAt")
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
    @Mapping(target = "duration", expression = "java(getDuration(ride))")
    @Mapping(target = "driverRating", expression = "java(getAverageDriverRating(ride))")
    @Mapping(target = "vehicleRating", expression = "java(getAverageVehicleRating(ride))")
    @Mapping(target = "ratingComment", expression = "java(getFirstRatingComment(ride))")
    PassengerRideDetailResponse toPassengerDetailResponse(Ride ride);

    /**
     * Map Ride to RideTrackingResponse for real-time tracking
     */
    @Mapping(source = "id", target = "rideId")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    @Mapping(target = "currentLocation", expression = "java(getVehicleLocation(ride))")
    @Mapping(target = "estimatedTimeMinutes", expression = "java(getEstimatedMinutes(ride))")
    @Mapping(target = "estimatedArrival", expression = "java(getEstimatedArrival(ride))")
    @Mapping(target = "driver", ignore = true)  // Set manually in service
    @Mapping(target = "passengers", ignore = true)  // Set manually in service
    @Mapping(target = "route", ignore = true)  // Set manually in service
    RideTrackingResponse toTrackingResponse(Ride ride);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "passengerFirstName", source = "mainPassenger.firstName")
    @Mapping(target = "passengerLastName", source = "mainPassenger.lastName")
    @Mapping(target = "passengerEmail", source = "mainPassenger.email")
    @Mapping(target = "passengerPhoneNum", source = "mainPassenger.phoneNum")
    @Mapping(target = "driverFirstName", source = "driver.firstName")
    @Mapping(target = "driverLastName", source = "driver.lastName")
    @Mapping(target = "driverEmail", source = "driver.email")
    @Mapping(target = "driverPhoneNum", source = "driver.phoneNum")
    @Mapping(target = "vehiclePlateNum", source = "driver.vehicle.plateNum")
    @Mapping(target = "vehicleModel", source = "driver.vehicle.model")
    @Mapping(target = "routeGeometry", source = "route.routeGeometry")
    @Mapping(target = "distanceKm", source = "route.distanceKm")
    @Mapping(target = "estimatedTimeSeconds", source = "route.estimatedTimeSeconds")
    @Mapping(target = "pickupAddress", source = "route.pickupStop.address")
    @Mapping(target = "destinationAddress", source = "route.destinationStop.address")
    RideAssignmentResponse toAssignmentResponse(Ride ride);

    // ================= helpers =================

    default String getDestinationAddress(Ride ride) {
        if (ride.getRoute() == null || ride.getRoute().getStops().isEmpty()) {
            return null;
        }
        return ride.getRoute().getDestinationStop().getAddress();
    }

    default String getPickupAddress(Ride ride) {
        if (ride.getRoute() == null || ride.getRoute().getStops().isEmpty()) {
            return null;
        }
        return ride.getRoute().getPickupStop().getAddress();
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

    default Integer getDuration(Ride ride) {
        if (ride.getRoute() == null || ride.getRoute().getEstimatedTimeSeconds() == null) {
            return null;
        }
        return (int) Math.round(ride.getRoute().getEstimatedTimeSeconds());
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

    default VehicleLocationResponse getVehicleLocation(Ride ride) {
        if (ride.getDriver() == null ||
                ride.getDriver().getVehicle() == null ||
                ride.getDriver().getVehicle().getLocation() == null) {
            return null;
        }

        var vehicle = ride.getDriver().getVehicle();
        var location = vehicle.getLocation();

        return new VehicleLocationResponse(
                vehicle.getId(), vehicle.getModel(), vehicle.getPlateNum(), location.getLatitude(),
                location.getLongitude(), vehicle.isAvailable()
        );
    }

    default Integer getEstimatedMinutes(Ride ride) {
        if (ride.getRoute() == null || ride.getRoute().getEstimatedTimeSeconds() == null) {
            return null;
        }
        return (int) Math.round(ride.getRoute().getEstimatedTimeSeconds() / 60.0);
    }

    default LocalDateTime getEstimatedArrival(Ride ride) {
        if (ride.getStartTime() == null) {
            return null;
        }

        Integer minutes = getEstimatedMinutes(ride);
        return minutes != null ? ride.getStartTime().plusMinutes(minutes) : null;
    }

    @Named("statusToString")
    default String statusToString(Enum<?> status) {
        return status != null ? status.name() : null;
    }

    @Named("routeDistanceKm")
    default Double getRouteDistanceKm(Ride ride) {
        if (ride == null || ride.getRoute() == null) {
            return null;
        }
        return ride.getRoute().getDistanceKm();
    }

}