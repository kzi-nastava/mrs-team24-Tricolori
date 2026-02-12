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

import com.tricolori.backend.dto.report.DailyStatisticDTO;
import com.tricolori.backend.dto.report.PersonalReportRequest;
import com.tricolori.backend.dto.report.PersonalReportResponse;
import com.tricolori.backend.entity.Person;
import com.tricolori.backend.entity.Ride;
import com.tricolori.backend.enums.PersonRole;
import com.tricolori.backend.exception.InvalidReportDateException;
import com.tricolori.backend.repository.RideRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {
    private final RideRepository rideRepository;

    public PersonalReportResponse getPersonalResponse(Person person, PersonalReportRequest request) {
        LocalDateTime start = request.getFrom();
        LocalDateTime end = request.getTo();

        if (start.isAfter(end)) {
            throw new InvalidReportDateException("'From date' is after 'to date'.");
        }
        
        Long userId = person.getId();
        List<Ride> rides = person.getRole().equals(PersonRole.ROLE_PASSENGER) ?
            rideRepository.findAllFinishedRidesByPassenger(userId, start, end) :
            rideRepository.findAllFinishedRidesByDriver(userId, start, end);
        

        PersonalReportResponse response = new PersonalReportResponse();
        populatePersonalReport(response, rides, start, end);

        return response;
    }

    private void populatePersonalReport(
        PersonalReportResponse response, 
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
