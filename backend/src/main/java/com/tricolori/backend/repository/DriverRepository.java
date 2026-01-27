
package com.tricolori.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tricolori.backend.entity.Driver;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

}