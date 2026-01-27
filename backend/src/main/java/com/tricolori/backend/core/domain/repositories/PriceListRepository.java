package com.tricolori.backend.core.domain.repositories;

import com.tricolori.backend.core.domain.models.PriceList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PriceListRepository extends JpaRepository<PriceList, Long> {

    // get latest price list
    @Query("""
        SELECT p FROM PriceList p
        ORDER BY p.createdAt DESC
        LIMIT 1
    """)
    Optional<PriceList> findLatest();
}
