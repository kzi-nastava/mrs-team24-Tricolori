package com.tricolori.backend.service;

import com.tricolori.backend.entity.Passenger;
import com.tricolori.backend.entity.Ride;
import com.tricolori.backend.repository.ReviewRepository;
import com.tricolori.backend.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderSchedulerService {

    private final RideRepository rideRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationService notificationService;

    /**
     * Send rating reminders every 30 minutes
     * - 1 hour after ride completion: First reminder
     * - 24 hours before deadline (48h after completion): Final reminder
     */
    @Scheduled(cron = "0 */30 * * * *") // Every 30 minutes
    public void sendRatingReminders() {
        log.info("Running rating reminder job...");

        LocalDateTime now = LocalDateTime.now();

        // First reminder - 1 hour after completion (71 hours remaining)
        sendRatingRemindersForWindow(now.minusHours(1), 71);

        // Final reminder - 48 hours after completion (24 hours remaining)
        sendRatingRemindersForWindow(now.minusDays(2), 24);

        log.info("Rating reminder job completed");
    }

    /**
     * Send ride reminders every 5 minutes
     * Passengers: 15 minutes before, then every 5 minutes until ride starts
     * Driver: 5 minutes before scheduled time
     */
    @Scheduled(cron = "0 */5 * * * *") // Every 5 minutes
    public void sendScheduledRideReminders() {
        log.info("Running scheduled ride reminder job...");

        LocalDateTime now = LocalDateTime.now();

        // Passenger reminders: 15, 10, 5 minutes before
        sendPassengerRemindersForWindow(now.plusMinutes(15), 15);
        sendPassengerRemindersForWindow(now.plusMinutes(10), 10);
        sendPassengerRemindersForWindow(now.plusMinutes(5), 5);

        // Driver reminder: 5 minutes before only
        sendDriverRemindersForWindow(now.plusMinutes(5), 5);

        log.info("Scheduled ride reminder job completed");
    }

    // ==================== HELPER METHODS ====================

    private void sendRatingRemindersForWindow(LocalDateTime targetTime, int hoursRemaining) {
        // Find completed rides within ±15 minute window
        LocalDateTime startWindow = targetTime.minusMinutes(15);
        LocalDateTime endWindow = targetTime.plusMinutes(15);

        List<Ride> completedRides = rideRepository.findCompletedRidesBetween(startWindow, endWindow);

        for (Ride ride : completedRides) {
            if (ride.getDriver() == null) {
                continue;
            }

            String driverName = ride.getDriver().getFirstName() + " " + ride.getDriver().getLastName();

            // Notify all passengers who haven't rated yet
            for (Passenger passenger : ride.getPassengers()) {
                boolean alreadyRated = reviewRepository.existsByRideIdAndReviewerId(
                        ride.getId(),
                        passenger.getId()
                );

                if (!alreadyRated) {
                    try {
                        notificationService.sendRatingReminderNotification(passenger.getEmail(), ride.getId(),
                                driverName, hoursRemaining);
                        log.debug("Sent rating reminder to {} for ride {} ({} hours remaining)",
                                passenger.getEmail(), ride.getId(), hoursRemaining);
                    } catch (Exception e) {
                        log.error("Failed to send rating reminder to {}", passenger.getEmail(), e);
                    }
                }
            }
        }
    }

    private void sendPassengerRemindersForWindow(LocalDateTime targetTime, int minutesUntilPickup) {
        // Find scheduled rides within ±2 minute window
        LocalDateTime startWindow = targetTime.minusMinutes(2);
        LocalDateTime endWindow = targetTime.plusMinutes(2);

        List<Ride> upcomingRides = rideRepository.findScheduledRidesBetween(startWindow, endWindow);

        for (Ride ride : upcomingRides) {
            String from = ride.getRoute().getPickupStop().getAddress();
            String to = ride.getRoute().getDestinationStop().getAddress();

            // Notify all passengers (with email)
            for (Passenger passenger : ride.getPassengers()) {
                try {
                    notificationService.sendRideReminderNotification(passenger.getEmail(), ride.getId(),
                            passenger.getFirstName(), minutesUntilPickup, from, to);
                    log.debug("Sent ride reminder to {} for ride {} ({} min)",
                            passenger.getEmail(), ride.getId(), minutesUntilPickup);
                } catch (Exception e) {
                    log.error("Failed to send ride reminder to {}", passenger.getEmail(), e);
                }
            }
        }
    }

    private void sendDriverRemindersForWindow(LocalDateTime targetTime, int minutesUntilPickup) {
        // Find scheduled rides within ±2 minute window
        LocalDateTime startWindow = targetTime.minusMinutes(2);
        LocalDateTime endWindow = targetTime.plusMinutes(2);

        List<Ride> upcomingRides = rideRepository.findScheduledRidesBetween(startWindow, endWindow);

        for (Ride ride : upcomingRides) {
            if (ride.getDriver() == null) {
                continue;
            }

            String from = ride.getRoute().getPickupStop().getAddress();
            String passengerName = ride.getPassengers().getFirst().getFirstName() +
                    " " + ride.getPassengers().getFirst().getLastName();

            try {
                notificationService.sendUpcomingRideReminderNotification(ride.getDriver().getEmail(), ride.getId(),
                        minutesUntilPickup, from, passengerName);
                log.debug("Sent driver reminder for ride {} ({} min)", ride.getId(), minutesUntilPickup);
            } catch (Exception e) {
                log.error("Failed to send driver reminder for ride {}", ride.getId(), e);
            }
        }
    }
}