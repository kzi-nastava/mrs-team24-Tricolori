package com.tricolori.backend.repository;

import com.tricolori.backend.entity.Panic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PanicRepository extends JpaRepository<Panic, Long> {
}
