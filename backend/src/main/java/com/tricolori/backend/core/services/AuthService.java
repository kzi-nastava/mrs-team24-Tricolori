package com.tricolori.backend.core.services;

import com.tricolori.backend.core.domain.models.ActivationToken;
import com.tricolori.backend.core.domain.models.Passenger;
import com.tricolori.backend.core.domain.models.Person;
import com.tricolori.backend.core.domain.repositories.ActivationTokenRepository;
import com.tricolori.backend.core.domain.repositories.PassengerRepository;
import com.tricolori.backend.core.domain.repositories.PersonRepository;
import com.tricolori.backend.infrastructure.presentation.dtos.LoginRequest;
import com.tricolori.backend.infrastructure.presentation.dtos.LoginResponse;
import com.tricolori.backend.infrastructure.presentation.dtos.PersonDto;
import com.tricolori.backend.infrastructure.presentation.dtos.RegisterPassengerRequest;
import com.tricolori.backend.infrastructure.presentation.mappers.PersonMapper;
import com.tricolori.backend.infrastructure.security.JwtUtil;
import com.tricolori.backend.shared.enums.AccountStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final PersonRepository personRepository;
    private final PassengerRepository passengerRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
    private final EmailService emailService;
    private final ActivationTokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PersonMapper personMapper;

    public LoginResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        log.info("iddler");

        Person person = (Person) authentication.getPrincipal();
        final String token = jwtUtil.generateToken(person.getEmail());
        PersonDto personDto = personMapper.toDto(person);

        return new LoginResponse(token, personDto);
    }

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

        // Generate activation token
        ActivationToken token = ActivationToken.createForPerson(savedPassenger);
        tokenRepository.save(token);

        // Send activation mail
        emailService.sendActivationEmail(
                savedPassenger.getEmail(),
                savedPassenger.getFirstName(),
                token.getToken()
        );
    }

    @Transactional
    public void activateAccount(String tokenString) {

        ActivationToken token = tokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new IllegalArgumentException("Invalid activation token"));

        if (!token.isValid()) {
            throw new IllegalArgumentException("Token expired or already used");
        }

        Person person = token.getPerson();
        person.setAccountStatus(AccountStatus.ACTIVE);

        token.setUsed(true);
        token.setActivatedAt(java.time.LocalDateTime.now());
    }

}
