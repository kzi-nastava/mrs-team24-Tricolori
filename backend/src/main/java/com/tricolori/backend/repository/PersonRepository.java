package com.tricolori.backend.repository;

import com.tricolori.backend.entity.Person;
import com.tricolori.backend.enums.PersonRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    Optional<Person> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRole(PersonRole role);
}
