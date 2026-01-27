package com.tricolori.backend.core.domain.repositories;

import com.tricolori.backend.core.domain.models.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    // finds route by its geometry (as encoded polyline)
    @Query("SELECT r FROM Route r WHERE r.routeGeometry = :geometry")
    Optional<Route> findByRouteGeometry(@Param("geometry") String geometry);
}
