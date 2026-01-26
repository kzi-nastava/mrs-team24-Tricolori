package com.tricolori.backend.infrastructure.presentation.mappers;

import com.tricolori.backend.core.domain.models.Ride;
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

}