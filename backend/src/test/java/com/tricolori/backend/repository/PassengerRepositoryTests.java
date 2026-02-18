package com.tricolori.backend.repository;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.tricolori.backend.entity.Passenger;
import com.tricolori.backend.util.TestObjectFactory;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class PassengerRepositoryTests {
    private final PassengerRepository passengerRepository;

    @Autowired
    public PassengerRepositoryTests(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    @Test
    public void FindAllByEmailIn_ShouldFindSinglePassenger() {
        // Arrange
        Passenger passenger = TestObjectFactory.createTestPassenger();
        String email = passenger.getEmail();
        
        passengerRepository.save(passenger);
        
        // Act
        List<Passenger> foundPassengers = passengerRepository.findAllByEmailIn(Collections.singletonList(email));
        
        // Assert
        assertEquals(1, foundPassengers.size(), "Should find exactly one passenger");
        assertEquals(email, foundPassengers.get(0).getEmail());
    }

    @Test
    public void FindAllByEmailIn_ShouldFindMultiplePassengers() {
        // Arrange
        String email1 = "passenger1@example.com";
        String email2 = "passenger2@example.com";
        String email3 = "passenger3@example.com";

        Passenger passenger1 = TestObjectFactory.createTestPassenger();
        passenger1.setEmail(email1);
        
        Passenger passenger2 = TestObjectFactory.createTestPassenger();
        passenger2.setEmail(email2);
        
        Passenger passenger3 = TestObjectFactory.createTestPassenger();
        passenger3.setEmail(email3);
        
        passengerRepository.save(passenger1);
        passengerRepository.save(passenger2);
        passengerRepository.save(passenger3);
        
        List<String> emails = Arrays.asList(email1, email2);
        
        // Act
        List<Passenger> foundPassengers = passengerRepository.findAllByEmailIn(emails);
        
        // Assert
        assertEquals(2, foundPassengers.size(), "Should find exactly two passengers");
        assertTrue(foundPassengers.stream().anyMatch(p -> p.getEmail().equals(email1)));
        assertTrue(foundPassengers.stream().anyMatch(p -> p.getEmail().equals(email2)));
    }

    @Test
    public void FindAllByEmailIn_ShouldReturnEmpty_WhenEmailsDoNotExist() {
        // Arrange
        String existingEmail = "existing@example.com";
        String nonexistingEmail1 = "nonexisting1@example.com";
        String nonexistingEmail2 = "nonexisting2@example.com";

        Passenger passenger = TestObjectFactory.createTestPassenger();
        passenger.setEmail(existingEmail);
        
        passengerRepository.save(passenger);
        
        List<String> nonExistingEmails = Arrays.asList(nonexistingEmail1, nonexistingEmail2);
        
        // Act
        List<Passenger> foundPassengers = passengerRepository.findAllByEmailIn(nonExistingEmails);
        
        // Assert
        assertTrue(foundPassengers.isEmpty(), "Should not find any passengers for non-existing emails");
    }

    @Test
    public void FindAllByEmailIn_ShouldReturnEmpty_WhenNoPassengersExist() {
        // Arrange
        List<String> emails = Arrays.asList("email1@example.com", "email2@example.com");
        
        // Act
        List<Passenger> foundPassengers = passengerRepository.findAllByEmailIn(emails);
        
        // Assert
        assertTrue(foundPassengers.isEmpty(), "Should not find any passengers when repository is empty");
    }

    @Test
    public void FindAllByEmailIn_ShouldReturnEmpty_WhenEmailListIsEmpty() {
        // Arrange
        Passenger passenger = TestObjectFactory.createTestPassenger();
        passengerRepository.save(passenger);
        
        List<String> emptyEmailList = Collections.emptyList();
        
        // Act
        List<Passenger> foundPassengers = passengerRepository.findAllByEmailIn(emptyEmailList);
        
        // Assert
        assertTrue(foundPassengers.isEmpty(), "Should not find any passengers when email list is empty");
    }

    @Test
    public void FindAllByEmailIn_ShouldFindAllPassengers_WhenAllEmailsMatch() {
        // Arrange
        String email1 = "passenger1@example.com";
        String email2 = "passenger2@example.com";
        String email3 = "passenger3@example.com";

        Passenger passenger1 = TestObjectFactory.createTestPassenger();
        passenger1.setEmail(email1);
        
        Passenger passenger2 = TestObjectFactory.createTestPassenger();
        passenger2.setEmail(email2);
        
        Passenger passenger3 = TestObjectFactory.createTestPassenger();
        passenger3.setEmail(email3);
        
        passengerRepository.save(passenger1);
        passengerRepository.save(passenger2);
        passengerRepository.save(passenger3);
        
        List<String> allEmails = Arrays.asList(email1, email2, email3);
        
        // Act
        List<Passenger> foundPassengers = passengerRepository.findAllByEmailIn(allEmails);
        
        // Assert
        assertEquals(3, foundPassengers.size(), "Should find all three passengers");
    }

    @Test
    public void FindAllByEmailIn_ShouldFindOnlyMatchingPassengers_WhenSomeEmailsDoNotExist() {
        // Arrange
        String existingEmail1 = "existing1@example.com";
        String existingEmail2 = "existing2@example.com";
        String nonexistingEmail = "nonexisting@example.com";

        Passenger passenger1 = TestObjectFactory.createTestPassenger();
        passenger1.setEmail(existingEmail1);
        
        Passenger passenger2 = TestObjectFactory.createTestPassenger();
        passenger2.setEmail(existingEmail2);
        
        passengerRepository.save(passenger1);
        passengerRepository.save(passenger2);
        
        List<String> mixedEmails = Arrays.asList(
            existingEmail1,
            nonexistingEmail,
            existingEmail2
        );
        
        // Act
        List<Passenger> foundPassengers = passengerRepository.findAllByEmailIn(mixedEmails);
        
        // Assert
        assertEquals(2, foundPassengers.size(), "Should find only existing passengers");
        assertTrue(foundPassengers.stream().anyMatch(p -> p.getEmail().equals(existingEmail1)));
        assertTrue(foundPassengers.stream().anyMatch(p -> p.getEmail().equals(existingEmail2)));
    }

    @Test
    public void FindAllByEmailIn_ShouldNotReturnDuplicates_WhenEmailAppearsMultipleTimes() {
        // Arrange
        String duplicateEmail = "duplicate@example.com"; 
        Passenger passenger = TestObjectFactory.createTestPassenger();
        passenger.setEmail(duplicateEmail);
        
        passengerRepository.save(passenger);
        
        List<String> duplicateEmails = Arrays.asList(
            duplicateEmail, duplicateEmail, duplicateEmail
        );
        
        // Act
        List<Passenger> foundPassengers = passengerRepository.findAllByEmailIn(duplicateEmails);
        
        // Assert
        assertEquals(1, foundPassengers.size(), "Should return only one passenger despite duplicate emails in list");
        assertEquals(duplicateEmail, foundPassengers.get(0).getEmail());
    }

    @Test
    public void FindAllByEmailIn_ShouldBeCaseSensitive() {
        // Arrange
        String lowercaseEmail = "lowercase@example.com";
        String uppercaseEmail = "LOWERCASE@EXAMPLE.COM"; 
        Passenger passenger = TestObjectFactory.createTestPassenger();
        passenger.setEmail(lowercaseEmail);
        
        passengerRepository.save(passenger);
        
        List<String> uppercaseEmails = Collections.singletonList(uppercaseEmail);
        
        // Act
        List<Passenger> foundPassengers = passengerRepository.findAllByEmailIn(uppercaseEmails);
        
        // Assert
        assertTrue(foundPassengers.isEmpty(), "Should not find passenger - email search should be case sensitive");
    }
}