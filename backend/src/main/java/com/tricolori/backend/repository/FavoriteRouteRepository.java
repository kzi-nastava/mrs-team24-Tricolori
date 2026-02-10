package com.tricolori.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tricolori.backend.entity.FavoriteRoute;

@Repository
public interface FavoriteRouteRepository extends JpaRepository<FavoriteRoute, Long> {
    Optional<FavoriteRoute> findByPersonIdAndRouteId(Long personId, Long routeId);
    List<FavoriteRoute> findAllByPersonId(Long personId);
    boolean existsByPersonIdAndRouteId(Long personId, Long routeId);
}
