package com.tricolori.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tricolori.backend.entity.ChangeDataRequest;
@Repository
public interface ChangeDataRequestRepository extends JpaRepository<ChangeDataRequest, Long> {
    @Query("SELECT r FROM ChangeDataRequest r WHERE r.reviewedAt IS NULL")
    List<ChangeDataRequest> findAllPending();
}
