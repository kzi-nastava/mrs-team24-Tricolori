package com.tricolori.backend.repository;

import com.tricolori.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // check if passenger already reviewed a ride
    boolean existsByRideIdAndReviewerId(Long rideId, Long reviewerId);

    // find review by ride and reviewer
    Optional<Review> findByRideIdAndReviewerId(Long rideId, Long reviewerId);

    // all reviews for a ride
    List<Review> findAllByRideId(Long rideId);

    // average driver rating for a ride
    @Query("""
        SELECT AVG(r.driverRating)
        FROM Review r
        WHERE r.ride.id = :rideId
    """)
    Double getAverageDriverRating(@Param("rideId") Long rideId);

    // average vehicle rating for a ride
    @Query("""
        SELECT AVG(r.vehicleRating)
        FROM Review r
        WHERE r.ride.id = :rideId
    """)
    Double getAverageVehicleRating(@Param("rideId") Long rideId);
}
