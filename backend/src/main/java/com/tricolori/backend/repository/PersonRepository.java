package com.tricolori.backend.repository;

import com.tricolori.backend.entity.Person;
import com.tricolori.backend.enums.PersonRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long>, JpaSpecificationExecutor<Person> {

    Optional<Person> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRole(PersonRole role);

    List<Person> findByRole(PersonRole role);

    @Query("SELECT p.email FROM Person p WHERE p.role = com.tricolori.backend.enums.PersonRole.ROLE_ADMIN")
    List<String> findAllAdminsEmails();
}
