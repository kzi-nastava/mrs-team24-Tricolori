package com.tricolori.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tricolori.backend.entity.ChangeDataRequest;
@Repository
public interface ChangeDataRequestRepository extends JpaRepository<ChangeDataRequest, Long> {
    
}
