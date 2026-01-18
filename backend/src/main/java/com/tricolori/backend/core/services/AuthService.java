package com.tricolori.backend.core.services;

import com.tricolori.backend.core.domain.models.Passenger;
import com.tricolori.backend.core.domain.repositories.PassengerRepository;
import com.tricolori.backend.core.domain.repositories.PersonRepository;
import com.tricolori.backend.infrastructure.presentation.dtos.RegisterPassengerRequest;
import com.tricolori.backend.shared.enums.AccountStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PersonRepository personRepository;
    private final PassengerRepository passengerRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public void registerPassenger(RegisterPassengerRequest request, MultipartFile pfp) {

        if (personRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        Passenger passenger = new Passenger();
        passenger.setEmail(request.email());
        passenger.setPassword(passwordEncoder.encode(request.password()));
        passenger.setFirstName(request.firstName());
        passenger.setLastName(request.lastName());
        passenger.setHomeAddress(request.homeAddress());
        passenger.setPhoneNum(request.phoneNum());
        passenger.setAccountStatus(AccountStatus.WAITING_FOR_ACTIVATION);

        Passenger savedPassenger = passengerRepository.save(passenger);

        String pfpUrl = cloudinaryService.uploadProfilePicture(pfp, savedPassenger.getId());
        savedPassenger.setPfpUrl(pfpUrl);
    }

}
