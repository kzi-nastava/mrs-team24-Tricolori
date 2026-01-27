package com.tricolori.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tricolori.backend.entity.VehicleSpecification;

@Repository
public interface VehicleSpecificationRepository extends JpaRepository<VehicleSpecification, Long> {

}