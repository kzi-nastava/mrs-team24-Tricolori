package com.tricolori.backend.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tricolori.backend.dto.block.ActivePersonStatus;
import com.tricolori.backend.dto.block.BlockRequest;
import com.tricolori.backend.entity.Block;
import com.tricolori.backend.entity.Person;
import com.tricolori.backend.enums.AccountStatus;
import com.tricolori.backend.enums.PersonRole;
import com.tricolori.backend.exception.BlockAdminException;
import com.tricolori.backend.exception.PersonNotFoundException;
import com.tricolori.backend.exception.UserAlreadyBlockedException;
import com.tricolori.backend.exception.UserNotBlockedException;
import com.tricolori.backend.mapper.PersonMapper;
import com.tricolori.backend.repository.PersonRepository;
import com.tricolori.backend.repository.PersonSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;
    private final PersonMapper mapper;

    public Page<ActivePersonStatus> getActivePersons(Long id, String firstName, String lastName, String email, Pageable pageable) {
        Specification<Person> spec = PersonSpecifications.withFilters(id, firstName, lastName, email);
        Page<Person> personPage = personRepository.findAll(spec, pageable);
        
        return mapper.toActivePersonStatusPage(personPage);
    }

    public void applyBlock(BlockRequest request) {
        String email = request.getUserEmail();
        Person person = personRepository.findByEmail(email)
        .orElseThrow(() -> new PersonNotFoundException(
            "Person with email " + email + " not found."
        ));

        // Check if person is admin...
        if (person.getRole().equals(PersonRole.ROLE_ADMIN)) 
            throw new BlockAdminException("Admin can't be blocked - admin email provided.");

        if (person.getBlock() != null) {
            throw new UserAlreadyBlockedException("Person with email " + email + " is already blocked.");
        }

        Block block = new Block();
        block.setBlockReason(request.getBlockReason());
        block.setBlockedAt(LocalDateTime.now());
        person.setBlock(block);
        person.setAccountStatus(AccountStatus.SUSPENDED);

        personRepository.save(person);
    }

    public void removeBlock(String email) {
        Person person = personRepository.findByEmail(email)
        .orElseThrow(() -> new PersonNotFoundException(
            "Person with email " + email + " not found."
        ));

        if (person.getBlock() == null) {
            throw new UserNotBlockedException("Can't unblock user that is not blocked.");
        }

        person.setBlock(null);
        person.setAccountStatus(AccountStatus.ACTIVE);

        personRepository.save(person);
    }
}
