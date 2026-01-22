package com.tricolori.backend.core.domain.repositories;

import com.tricolori.backend.core.domain.models.Panic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PanicRepository extends JpaRepository<Panic, Long> {
}
