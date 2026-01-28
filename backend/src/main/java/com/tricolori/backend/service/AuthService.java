package com.tricolori.backend.service;

import com.tricolori.backend.dto.auth.*;
import com.tricolori.backend.entity.*;
import com.tricolori.backend.repository.*;
import com.tricolori.backend.dto.profile.PersonDto;
import com.tricolori.backend.mapper.PersonMapper;
import com.tricolori.backend.mapper.VehicleMapper;
import com.tricolori.backend.security.JwtUtil;
import com.tricolori.backend.enums.AccountStatus;
import com.tricolori.backend.enums.RegistrationTokenVerificationStatus;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final ActivationTokenRepository activationTokenRepository;
    private final RegistrationTokenRepository registrationTokenRepository;
    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CloudinaryService cloudinaryService;
    private final EmailService emailService;
    private final VehicleService vehicleService;
    private final JwtUtil jwtUtil;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final PersonMapper personMapper;
    private final VehicleMapper vehicleMapper;

    public LoginResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        Person person = (Person) authentication.getPrincipal();
        final String token = jwtUtil.generateToken(person.getEmail());
        PersonDto personDto = personMapper.toDto(person);

        return new LoginResponse(token, personDto);
    }

    @Transactional
    public void driverPasswordSetup(DriverPasswordSetupRequest request) {
        RegistrationToken token = registrationTokenRepository.findByToken(request.token())
        .orElseThrow(() -> {
            // TODO: add custom exception...
            throw new IllegalArgumentException("Token doesn't exist");
        });

        if (token.isExpired()) {
            throw new RuntimeException("Token expired");
        }

        // Load person...
        Driver driver = driverRepository.findById(token.getPerson().getId())
        .orElseThrow(() -> {
            // TODO: add custom exception...
            throw new IllegalArgumentException("User doesn't exist");
        });

        // and update status and password:
        driver.setPassword(passwordEncoder.encode(request.password()));
        driver.setAccountStatus(AccountStatus.ACTIVE);
        driverRepository.save(driver);

        // Delete request:
        registrationTokenRepository.delete(token);
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

        if (pfp != null) {
            String pfpUrl = cloudinaryService.uploadProfilePicture(pfp, savedPassenger.getId());
            savedPassenger.setPfpUrl(pfpUrl);
        }

        // Generate activation token
        ActivationToken token = ActivationToken.createForPerson(savedPassenger);
        activationTokenRepository.save(token);

        // Send activation mail
        emailService.sendActivationEmail(
            savedPassenger.getEmail(),
            savedPassenger.getFirstName(),
            token.getToken()
        );
    }

    @Transactional
    public RegistrationTokenVerificationStatus verifyToken(String tokenValue) {
        Optional<RegistrationToken> tokenOpt = registrationTokenRepository.findByToken(tokenValue);
        if (tokenOpt.isEmpty()) {
            return RegistrationTokenVerificationStatus.INVALID;
        }
        RegistrationToken token = tokenOpt.get();

        // Load person...
        Optional<Driver> driverOpt = driverRepository.findById(token.getPerson().getId());
        if (driverOpt.isEmpty()) {
            return RegistrationTokenVerificationStatus.INVALID;
        }
        Driver driver = driverOpt.get();

        if (driver.getAccountStatus() == AccountStatus.ACTIVE) {
            registrationTokenRepository.delete(token);
            return RegistrationTokenVerificationStatus.ALREADY_ACTIVE;
        }

        if (token.isExpired()) {
            registrationTokenRepository.delete(token);
            // Generate registration token:
            RegistrationToken newToken = RegistrationToken.createForPerson(driver);
            registrationTokenRepository.save(newToken);

            // Send driver registration mail:
            emailService.sendDriverRegistrationEmail(
                driver.getEmail(),
                driver.getFirstName(),
                newToken.getToken()
            );   

            return RegistrationTokenVerificationStatus.EXPIRED_NEW_SENT;
        }

        return RegistrationTokenVerificationStatus.VALID;
    }

    @Transactional
    public void registerDriver(AdminDriverRegistrationRequest request, MultipartFile pfpFile) {
        if (personRepository.existsByEmail(request.email())) {
            // TODO: add custom exception...
            throw new IllegalArgumentException("Email already registered");
        }

        // Save vehicle specifications:
        RegisterVehicleSpecification specsRequest = vehicleMapper.fromAdminDriverRegistration(request);
        VehicleSpecification savedSpecs = vehicleService.registerVehicleSpecification(specsRequest);

        // Save vehicle:
        Vehicle vehicle = vehicleService.registerVehicle(savedSpecs, request.registrationPlate());

        // Save new driver:
        Driver driver = new Driver();
        driver.setEmail(request.email());
        driver.setFirstName(request.firstName());
        driver.setLastName(request.lastName());
        driver.setHomeAddress(request.address());
        driver.setPhoneNum(request.phone());
        driver.setAccountStatus(AccountStatus.WAITING_FOR_ACTIVATION);
        driver.setPassword(passwordEncoder.encode("Temp1-" + UUID.randomUUID().toString()));
        driver.setVehicle(vehicle);

        Driver savedDriver = driverRepository.save(driver);

        // Generate and save pfp:
        if (pfpFile != null) {
            String pfpUrl = cloudinaryService.uploadProfilePicture(pfpFile, savedDriver.getId());
            savedDriver.setPfpUrl(pfpUrl);
        }

        // Generate registration token:
        RegistrationToken token = RegistrationToken.createForPerson(savedDriver);
        registrationTokenRepository.save(token);

        // Send driver registration mail:
        emailService.sendDriverRegistrationEmail(
            savedDriver.getEmail(),
            savedDriver.getFirstName(),
            token.getToken()
        );        
    }

    @Transactional
    public void activateAccount(String tokenString) {

        ActivationToken token = activationTokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new IllegalArgumentException("Invalid activation token"));

        if (!token.isValid()) {
            throw new IllegalArgumentException("Token expired or already used");
        }

        Person person = token.getPerson();
        person.setAccountStatus(AccountStatus.ACTIVE);

        token.setUsed(true);
        token.setActivatedAt(java.time.LocalDateTime.now());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new RuntimeException("Token not found"));

        if (!resetToken.isValid()) {
            throw new RuntimeException("Token is no longer valid (expired or used)");
        }

        Person person = resetToken.getPerson();
        person.setPassword(passwordEncoder.encode(request.password()));

        resetToken.setUsed(true);

        personRepository.save(person);
        passwordResetTokenRepository.save(resetToken);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {

        Optional<Person> personOpt = personRepository.findByEmail(request.email());

        if (personOpt.isPresent()) {
            Person person = personOpt.get();

            PasswordResetToken resetToken = PasswordResetToken.createForPerson(person);
            passwordResetTokenRepository.save(resetToken);

            emailService.sendPasswordResetEmail(
                    person.getEmail(),
                    person.getFirstName(),
                    resetToken.getToken()
            );
        }

        log.info("Password reset processed for email: {}", request.email());
    }

    // Get the authenticated user's ID from the security context
    public Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }

        // Get the Person object from authentication principal
        // Your AuthTokenFilter sets this when validating the JWT
        Object principal = authentication.getPrincipal();

        if (principal instanceof Person) {
            return ((Person) principal).getId();
        }

        throw new RuntimeException("Invalid authentication principal type");
    }

    //Get the authenticated Person object
    public Person getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Person) {
            return (Person) principal;
        }

        throw new RuntimeException("Invalid authentication principal type");
    }

    // Get authenticated User's Email
    public String getAuthenticatedUserEmail() {
        return getAuthenticatedUser().getEmail();
    }

}
