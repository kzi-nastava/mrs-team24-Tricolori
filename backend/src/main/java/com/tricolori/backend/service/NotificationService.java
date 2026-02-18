package com.tricolori.backend.service;

import com.tricolori.backend.dto.notifications.NotificationDto;
import com.tricolori.backend.entity.Notification;
import com.tricolori.backend.entity.Ride;
import com.tricolori.backend.enums.NotificationType;
import com.tricolori.backend.mapper.NotificationMapper;
import com.tricolori.backend.repository.NotificationRepository;
import com.tricolori.backend.repository.PersonRepository;
import com.tricolori.backend.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;
    private final PersonRepository personRepository;
    private final RideRepository rideRepository;
    private final TrackingTokenService trackingTokenService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // ==================== CORE METHODS ====================

    @Transactional(readOnly = true)
    public List<NotificationDto> getAllNotifications(String email) {
        log.info("Fetching all notifications for user: {}", email);
        return notificationMapper.toDtoList(
                notificationRepository.findByEmailOrderByTimeDesc(email)
        );
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotifications(String email) {
        log.info("Fetching unread notifications for user: {}", email);
        return notificationMapper.toDtoList(
                notificationRepository.findByEmailAndOpenedOrderByTimeDesc(email, false)
        );
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        return notificationRepository.countByEmailAndOpened(email, false);
    }

    @Transactional
    public NotificationDto markAsRead(Long notificationId) {
        log.info("Marking notification as read: id={}", notificationId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        notification.setOpened(true);
        return notificationMapper.toDto(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsRead(String email) {
        log.info("Marking all notifications as read for user: {}", email);
        List<Notification> unreadNotifications =
                notificationRepository.findByEmailAndOpenedOrderByTimeDesc(email, false);

        unreadNotifications.forEach(n -> n.setOpened(true));
        notificationRepository.saveAll(unreadNotifications);

        log.info("Marked {} notifications as read", unreadNotifications.size());
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        log.info("Deleting notification: id={}", notificationId);
        notificationRepository.deleteById(notificationId);
    }

    @Transactional
    public void deleteAllNotifications(String email) {
        log.info("Deleting all notifications for user: {}", email);
        notificationRepository.deleteByEmail(email);
    }

    // ==================== HELPER METHOD ====================

    private void saveAndSend(Notification notification, String email) {
        notification = notificationRepository.save(notification);
        NotificationDto dto = notificationMapper.toDto(notification);
        messagingTemplate.convertAndSend("/topic/notifications/" + email, dto);
    }

    // ==================== PASSENGER NOTIFICATIONS ====================

    // RIDE_STARTING
    public void sendRideStartingNotification(String passengerEmail, Long rideId,
                                             String driverName, String vehicleInfo,
                                             String pickupLocation) {
        String content = String.format("Your driver %s will arrive at %s very soon. The vehicle model is %s.",
                driverName, pickupLocation, vehicleInfo);

        Notification notification = new Notification(passengerEmail, content, NotificationType.RIDE_STARTING, rideId);
        notification.setDriverName(driverName);
        notification.setActionUrl("/passenger/ride-tracking/" + rideId);
        saveAndSend(notification, passengerEmail);
    }

    // RIDE_CANCELLED
    public void sendRideCancelledNotification(String passengerEmail, Long rideId,
                                              String scheduledTime, String from, String to, String reason) {
        String content = String.format("Your ride scheduled for %s from %s to %s has been cancelled%s. Your payment has been refunded.",
                scheduledTime, from, to, reason != null && !reason.isEmpty() ? " due to " + reason : "");

        Notification notification = new Notification(passengerEmail, content, NotificationType.RIDE_CANCELLED, rideId);
        notification.setActionUrl("/passenger/history?openRide=" + rideId);
        saveAndSend(notification, passengerEmail);
    }

    // RIDE_REJECTED
    public void sendRideRejectedNotification(String passengerEmail, Long rideId) {
        String content = "Unfortunately, there are no available drivers at the moment. Please try again later.";

        Notification notification = new Notification(passengerEmail, content, NotificationType.RIDE_REJECTED, rideId);
        notification.setActionUrl("/passenger/history");
        saveAndSend(notification, passengerEmail);
    }

    // ADDED_TO_RIDE (with email)
    public void sendAddedToRideNotification(String passengerEmail, Long rideId,
                                            String organizerName, String passengerFirstName,
                                            String from, String to, String scheduledTime) {
        String content = String.format("%s added you to a shared ride from %s to %s scheduled for %s. Total cost will be paid by the organizer.",
                organizerName, from, to, scheduledTime);

        Notification notification = new Notification(passengerEmail, content, NotificationType.ADDED_TO_RIDE, rideId);
        notification.setPassengerName(organizerName);
        notification.setActionUrl("/passenger/ride-tracking/" + rideId);

        try {
            // Preprocessing: Check if user is registered and generate appropriate tracking link
            boolean isRegistered = personRepository.findByEmail(passengerEmail).isPresent();

            String trackingLink;
            if (isRegistered) {
                // Registered users: redirect through login
                trackingLink = frontendUrl + "/login?redirect=/passenger/ride-tracking/" + rideId;
            } else {
                // Unregistered users: direct tracking with token
                // Create tracking token with passenger details
                Ride ride = rideRepository.findById(rideId)
                        .orElseThrow(() -> new RuntimeException("Ride not found"));
                String token = trackingTokenService.createTrackingToken(passengerEmail, passengerFirstName, ride);
                trackingLink = frontendUrl + "/track-ride?token=" + token;
            }

            emailService.sendLinkedPassengerEmail(
                    passengerEmail,
                    passengerFirstName,
                    organizerName,
                    from,
                    to,
                    scheduledTime,
                    trackingLink,
                    isRegistered
            );
            log.info("Email sent to linked passenger: {} (registered: {})", passengerEmail, isRegistered);
        } catch (Exception e) {
            log.error("Failed to send email to linked passenger: {}", passengerEmail, e);
        }

        saveAndSend(notification, passengerEmail);
    }


    // RIDE_COMPLETED
    public void sendRideCompletedNotification(String passengerEmail, String passengerFirstName, Long rideId,
                                              String from, String to, double totalFare) {
        String content = String.format("Your ride from %s to %s has been completed. Total fare: %.2f RSD. Thank you for riding with us!",
                from, to, totalFare);

        Notification notification = new Notification(passengerEmail, content, NotificationType.RIDE_COMPLETED, rideId);
        notification.setActionUrl("/passenger/history?openRide=" + rideId);
        saveAndSend(notification, passengerEmail);

        try {
            emailService.sendRideCompletedEmail(passengerEmail, passengerFirstName, from, to, totalFare, rideId);
            log.info("Ride completed email sent to: {}", passengerEmail);
        } catch (Exception e) {
            log.error("Failed to send ride completed email to: {}", passengerEmail, e);
        }
    }

    // RATING_REMINDER
    public void sendRatingReminderNotification(String passengerEmail, Long rideId,
                                               String driverName, int hoursRemaining) {
        String content = String.format("How was your ride with %s? Your feedback helps us maintain quality service. You have %d hours remaining to submit your rating.",
                driverName, hoursRemaining);

        Notification notification = new Notification(passengerEmail, content, NotificationType.RATING_REMINDER, rideId);
        notification.setDriverName(driverName);
        notification.setActionUrl("/passenger/ride-rating/" + rideId);
        saveAndSend(notification, passengerEmail);
    }

    // RIDE_REMINDER (with email)
    public void sendRideReminderNotification(String passengerEmail, Long rideId,
                                             String passengerFirstName, int minutesUntilPickup,
                                             String from, String to) {
        String content = String.format("Reminder: Your scheduled ride starts in %d minutes. Be ready for pickup!",
                minutesUntilPickup);

        Notification notification = new Notification(passengerEmail, content, NotificationType.RIDE_REMINDER, rideId);
        notification.setActionUrl("/passenger/ride-tracking/" + rideId);

        // Send email reminder
        try {
            emailService.sendRideReminderEmail(passengerEmail, passengerFirstName, minutesUntilPickup,
                    from, to, rideId);
            log.info("Ride reminder email sent to: {}", passengerEmail);
        } catch (Exception e) {
            log.error("Failed to send ride reminder email to: {}", passengerEmail, e);
        }

        saveAndSend(notification, passengerEmail);
    }

    // ==================== DRIVER NOTIFICATIONS ====================

    // UPCOMING_RIDE_REMINDER
    public void sendUpcomingRideReminderNotification(String driverEmail, Long rideId,
                                                     int minutesUntilPickup, String pickupLocation,
                                                     String passengerName) {
        String content = String.format("You have a ride scheduled in %d minutes. Pickup location: %s. Passenger: %s. Make sure to arrive on time!",
                minutesUntilPickup, pickupLocation, passengerName);

        Notification notification = new Notification(driverEmail, content, NotificationType.UPCOMING_RIDE_REMINDER, rideId);
        notification.setPassengerName(passengerName);
        notification.setActionUrl("/driver/history?openRide=" + rideId);
        saveAndSend(notification, driverEmail);
    }

    // RATING_RECEIVED
    public void sendRatingReceivedNotification(String driverEmail, Long rideId, Integer driverStars, Integer vehicleStars, String passengerName, String comment)
    {

        List<String> parts = new ArrayList<>();
        if (driverStars != null)
            parts.add("you " + driverStars + " stars");
        if (vehicleStars != null)
            parts.add("the vehicle " + vehicleStars + " stars");

        String ratingPart = parts.isEmpty() ? "your ride" : String.join(" and ", parts);

        String content = passengerName + " rated " + ratingPart
                + (comment != null && !comment.isBlank() ? " with the comment: \"" + comment + "\"" : "") + ". Keep up the great work!";

        Notification notification = new Notification(driverEmail, content, NotificationType.RATING_RECEIVED, rideId);
        notification.setPassengerName(passengerName);
        notification.setActionUrl("/driver/history?openRide=" + rideId);
        saveAndSend(notification, driverEmail);
    }


    // RIDE_STARTED
    public void sendRideStartedNotification(String driverEmail, Long rideId) {
        String content = String.format("Your ride with id #%d has started. Drive safely and provide excellent service!",
                rideId);

        Notification notification = new Notification(driverEmail, content, NotificationType.RIDE_STARTED, rideId);
        notification.setActionUrl("/driver/ride-tracking/" + rideId);
        saveAndSend(notification, driverEmail);
    }

    // ==================== ADMIN NOTIFICATIONS ====================

    // RIDE_REPORT
    public void sendRideReportNotification(String adminEmail, Long rideId, String driverName,
                                           String reportType, String reportDetails) {
        String content = String.format("%s reported on ride #%d (by driver %s). User comment: %s",
                reportType, rideId, driverName, reportDetails != null && !reportDetails.isBlank() ? reportDetails : "No additional details provided.");

        Notification notification = new Notification(adminEmail, content, NotificationType.RIDE_REPORT, rideId);
        notification.setActionUrl("/admin/history?openRide=" + rideId);
        saveAndSend(notification, adminEmail);
    }

    // NEW_REGISTRATION
    public void sendNewRegistrationNotification(String adminEmail, String driverName,
                                                String vehicleModel) {
        String content = String.format("New driver %s has been registered. Vehicle: %s. Wish him a warm welcome!",
                driverName, vehicleModel);

        Notification notification = new Notification(adminEmail, content, NotificationType.NEW_REGISTRATION, null);
        notification.setDriverName(driverName);
        // TODO: redirect to admins page with all users
//        notification.setActionUrl("/admin/users");
        saveAndSend(notification, adminEmail);
    }

    // PROFILE_CHANGE_REQUEST
    public void sendProfileChangeRequestNotification(String adminEmail, String driverName,
                                                     Long driverId) {
        String content = String.format("Driver %s with id %d has requested profile changes. Please review and approve/reject.",
                driverName, driverId);

        Notification notification = new Notification(adminEmail, content,
                NotificationType.PROFILE_CHANGE_REQUEST, null);
        notification.setDriverName(driverName);
        notification.setActionUrl("/admin/change-requests");
        saveAndSend(notification, adminEmail);
    }

    public void sendPanicNotification(Long rideId, String senderEmail) {
        List<String> adminEmails = personRepository.findAllAdminsEmails();

        String content = String.format("!!! PANIC !!! Ride #%d. User %s has activated the panic button!",
                rideId, senderEmail);

        for (String adminEmail : adminEmails) {
            Notification notification = new Notification(adminEmail, content, NotificationType.RIDE_PANIC, rideId);
            notification.setActionUrl("/admin/history?openRide=" + rideId);

            saveAndSend(notification, adminEmail);
        }
    }

    // ==================== CHAT NOTIFICATIONS ====================

    // NEW_CHAT_MESSAGE
    public void sendNewChatMessageNotification(String email, String senderName, boolean isAdmin) {
        String content = String.format("New message from %s in support chat.",
                isAdmin ? "Admin " + senderName : senderName);

        Notification notification = new Notification(email, content,
                NotificationType.NEW_CHAT_MESSAGE, null);
        personRepository.findByEmail(email).ifPresent(person -> {
            switch (person.getRole().name()) {
                case "ROLE_ADMIN" -> notification.setActionUrl("/admin/support");
                case "ROLE_DRIVER" -> notification.setActionUrl("/driver/support");
                case "ROLE_PASSENGER" -> notification.setActionUrl("/passenger/support");
            }
        });
        saveAndSend(notification, email);
    }
}