package com.tricolori.backend.core.domain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.tricolori.backend.core.domain.models.Route;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    
}
