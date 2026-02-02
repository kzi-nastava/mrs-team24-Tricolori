package com.tricolori.backend.service;

import com.tricolori.backend.dto.profile.ChangeDriverStatusRequest;
import com.tricolori.backend.entity.Driver;
import com.tricolori.backend.entity.DriverDailyLog;
import com.tricolori.backend.enums.RideStatus;
import com.tricolori.backend.exception.PersonNotFoundException;
import com.tricolori.backend.repository.DriverDailyLogRepository;
import com.tricolori.backend.repository.DriverRepository;
import com.tricolori.backend.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverDailyLogService {

    private final DriverDailyLogRepository dailyLogRepository;
    private final DriverRepository driverRepository;
    private final RideRepository rideRepository;

    @Transactional
    public void changeStatus(ChangeDriverStatusRequest request, Long driverId) {

        DriverDailyLog dailyLog = dailyLogRepository.findByDriverIdAndDate(driverId, LocalDate.now())
                .orElseGet(() -> createLog(driverId));

        if (dailyLog.isActive() == request.active()) {
            return;
        }

        if (!request.active()) {
            boolean hasActiveRide = rideRepository.existsByDriverIdAndStatus(driverId, RideStatus.ONGOING);
            if (hasActiveRide) {
                throw new IllegalStateException("You cannot go inactive while ride is ongoing.");
            }
        }

        if (request.active()) {
            dailyLog.setActiveOn();
        } else {
            dailyLog.setActiveOff();
        }

        log.info("Driver with ID {{}} changed activity status to {{}}.", driverId, request.active());
        dailyLogRepository.save(dailyLog);
    }

    @Transactional
    protected DriverDailyLog createLog(Long driverId) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new PersonNotFoundException("Driver not found."));

        DriverDailyLog log = new DriverDailyLog();
        log.setDriver(driver);

        return dailyLogRepository.save(log);
    }

}
