package com.tricolori.backend.repository;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.tricolori.backend.entity.Person;
import com.tricolori.backend.enums.AccountStatus;

public class PersonSpecifications {
    private PersonSpecifications() {}

    public static Specification<Person> withFilters(Long id, String firstName, String lastName, String email) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (id != null) {
                predicates.add(cb.equal(root.get("id"), id));
            }
            if (firstName != null && !firstName.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
            }
            if (lastName != null && !lastName.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
            }
            if (email != null && !email.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }
            
            predicates.add(cb.equal(root.get("accountStatus"), AccountStatus.ACTIVE));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
