package com.tricolori.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tricolori.backend.dto.report.AdminReportRequest;
import com.tricolori.backend.dto.report.DailyStatisticDTO;
import com.tricolori.backend.dto.report.ReportRequest;
import com.tricolori.backend.dto.report.ReportResponse;
import com.tricolori.backend.entity.Person;
import com.tricolori.backend.entity.Ride;
import com.tricolori.backend.enums.PersonRole;
import com.tricolori.backend.enums.ReportScope;
import com.tricolori.backend.exception.BadReportIndividualEmailException;
import com.tricolori.backend.exception.InvalidReportDateException;
import com.tricolori.backend.exception.PersonNotFoundException;
import com.tricolori.backend.exception.ReportAdminEmailProvidedException;
import com.tricolori.backend.repository.PersonRepository;
import com.tricolori.backend.repository.RideRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {
    private final RideRepository rideRepository;
    private final PersonRepository personRepository;

    public ReportResponse getPersonalResponse(Person person, ReportRequest request) {
        LocalDateTime start = request.getFrom();
        LocalDateTime end = request.getTo();

        if (start.isAfter(end)) {
            throw new InvalidReportDateException("'From date' can't be after 'to date'.");
        }
        
        Long userId = person.getId();
        List<Ride> rides = person.getRole().equals(PersonRole.ROLE_PASSENGER) ?
            rideRepository.findAllFinishedRidesByPassenger(userId, start, end) :
            rideRepository.findAllFinishedRidesByDriver(userId, start, end);
        

        ReportResponse response = new ReportResponse();
        populatePersonalReport(response, rides, start, end);

        return response;
    }

    public ReportResponse getAdminResponse(Person person, AdminReportRequest request) {
        LocalDateTime start = request.getFrom();
        LocalDateTime end = request.getTo();

        if (start.isAfter(end)) {
            throw new InvalidReportDateException("'From date' can't be after 'to date'.");
        }

        var rides = getAdminReportRides(request);

        ReportResponse response = new ReportResponse();
        populatePersonalReport(response, rides, start, end);

        return response;
    }

    private List<Ride> getAdminReportRides(AdminReportRequest request) {
        var email = request.getIndividualEmail();
        var scope = request.getScope();
        var from = request.getFrom();
        var to = request.getTo();

        if (scope.equals(ReportScope.ALL)) {
            return rideRepository.findAllFinishedRidesInPeriod(from, to);
        } 

        // For individual user...
        if (email == null || email.isEmpty()) {
            throw new BadReportIndividualEmailException("Email of individual user can't be empty.");
        }

        var person = personRepository.findByEmail(email)
        .orElseThrow(() -> new PersonNotFoundException(
            String.format("Person with email %s doesn't exist.", email)
        ));

        PersonRole role = person.getRole();
        if (role.equals(PersonRole.ROLE_ADMIN))
            throw new ReportAdminEmailProvidedException(
                String.format("Email %s is email of an Admin.", email)
            );
    
        Long personId = person.getId();
        return person.getRole().equals(PersonRole.ROLE_PASSENGER) ?
            rideRepository.findAllFinishedRidesByPassenger(personId, from, to) :
            rideRepository.findAllFinishedRidesByDriver(personId, from, to);
    }

    private void populatePersonalReport(
        ReportResponse response, 
        List<Ride> rides, 
        LocalDateTime start, 
        LocalDateTime end
    ) {
        // Group rides by day...
        Map<LocalDate, List<Ride>> ridesByDate = rides.stream()
            .collect(Collectors.groupingBy(r -> r.getStartTime().toLocalDate()));

        List<DailyStatisticDTO> dailyStats = new ArrayList<>();

        double totalMoney = 0;
        double totalDistance = 0;

        long daysBetween = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate()) + 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.");

        for (int i = 0; i < daysBetween; i++) {
            LocalDate currentDate = start.toLocalDate().plusDays(i);
            List<Ride> dayRides = ridesByDate.getOrDefault(currentDate, Collections.emptyList());

            double dayDistance = dayRides.stream().mapToDouble(r -> r.getRoute().getDistanceKm()).sum();
            double dayMoney = dayRides.stream().mapToDouble(Ride::getPrice).sum();

            dailyStats.add(new DailyStatisticDTO(
                    currentDate.format(formatter),
                    (long) dayRides.size(),
                    round(dayDistance),
                    round(dayMoney)
            ));

            totalMoney += dayMoney;
            totalDistance += dayDistance;
        }

        // Cumulative Sum...
        response.setDailyStatistics(dailyStats);
        response.setTotalCount((long) rides.size());
        response.setTotalDistance(round(totalDistance));
        response.setTotalMoney(round(totalMoney));

        // Mean...
        response.setAverageCount(round((double) rides.size() / daysBetween));
        response.setAverageDistance(round(totalDistance / daysBetween));
        response.setAverageMoney(round(totalMoney / daysBetween));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
