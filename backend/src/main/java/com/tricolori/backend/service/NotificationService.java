package com.tricolori.backend.service;

import com.tricolori.backend.dto.notifications.NotificationDto;
import com.tricolori.backend.entity.Notification;
import com.tricolori.backend.enums.NotificationType;
import com.tricolori.backend.mapper.NotificationMapper;
import com.tricolori.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;

    // ==================== CORE METHODS ====================

    @Transactional
    public NotificationDto sendNotification(String email, String content, NotificationType type, Long rideId) {
        log.info("Sending notification to {}: type={}, rideId={}", email, type, rideId);

        Notification notification = new Notification(email, content, type, rideId);
        notification = notificationRepository.save(notification);

        NotificationDto dto = notificationMapper.toDto(notification);
        messagingTemplate.convertAndSend("/topic/notifications/" + email, dto);

        log.info("Notification sent successfully: id={}", notification.getId());
        return dto;
    }

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

    private NotificationDto saveAndSend(Notification notification, String email) {
        notification = notificationRepository.save(notification);
        NotificationDto dto = notificationMapper.toDto(notification);
        messagingTemplate.convertAndSend("/topic/notifications/" + email, dto);
        return dto;
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
        saveAndSend(notification, passengerEmail);
    }

    // RIDE_REJECTED
    public void sendRideRejectedNotification(String passengerEmail, Long rideId) {
        String content = "Unfortunately, there are no available drivers at the moment. Please try again later.";

        Notification notification = new Notification(passengerEmail, content, NotificationType.RIDE_REJECTED, rideId);
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
        notification.setActionUrl("/passenger/ride-details/" + rideId);

        try {
            emailService.sendLinkedPassengerEmail(passengerEmail, passengerFirstName, organizerName,
                    from, to, scheduledTime, rideId);
            log.info("Email sent to linked passenger: {}", passengerEmail);
        } catch (Exception e) {
            log.error("Failed to send email to linked passenger: {}", passengerEmail, e);
        }

        saveAndSend(notification, passengerEmail);
    }

    // RIDE_COMPLETED
    public void sendRideCompletedNotification(String passengerEmail, Long rideId,
                                              String from, String to, double totalFare) {
        String content = String.format("Your ride from %s to %s has been completed. Total fare: %.2f RSD. Thank you for riding with us!",
                from, to, totalFare);

        Notification notification = new Notification(passengerEmail, content, NotificationType.RIDE_COMPLETED, rideId);
        notification.setActionUrl("/passenger/history");
        saveAndSend(notification, passengerEmail);
    }

    // RATING_REMINDER
    public NotificationDto sendRatingReminderNotification(String passengerEmail, Long rideId,
                                                          String driverName, int hoursRemaining) {
        String content = String.format("How was your ride with %s? Your feedback helps us maintain quality service. You have %d hours remaining to submit your rating.",
                driverName, hoursRemaining);

        Notification notification = new Notification(passengerEmail, content, NotificationType.RATING_REMINDER, rideId);
        notification.setDriverName(driverName);
        notification.setActionUrl("/passenger/ride-rating/" + rideId);
        return saveAndSend(notification, passengerEmail);
    }

    // RIDE_REMINDER (with email)
    public NotificationDto sendRideReminderNotification(String passengerEmail, Long rideId,
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

        return saveAndSend(notification, passengerEmail);
    }

    // ==================== DRIVER NOTIFICATIONS ====================

    // NEW_RIDE_REQUEST
    public NotificationDto sendNewRideRequestNotification(String driverEmail, Long rideId,
                                                          String passengerName, String pickup,
                                                          String dropoff, double estimatedFare, double distance) {
        String content = String.format("%s has requested a ride from %s to %s. Distance: %.1f km. Estimated fare: %.2f RSD.",
                passengerName, pickup, dropoff, distance, estimatedFare);

        Notification notification = new Notification(driverEmail, content, NotificationType.NEW_RIDE_REQUEST, rideId);
        notification.setPassengerName(passengerName);
        notification.setActionUrl("/driver/ride-requests/" + rideId);
        return saveAndSend(notification, driverEmail);
    }

    // UPCOMING_RIDE_REMINDER
    public NotificationDto sendUpcomingRideReminderNotification(String driverEmail, Long rideId,
                                                                int minutesUntilPickup, String pickupLocation,
                                                                String passengerName) {
        String content = String.format("You have a ride scheduled in %d minutes. Pickup location: %s. Passenger: %s. Make sure to arrive on time!",
                minutesUntilPickup, pickupLocation, passengerName);

        Notification notification = new Notification(driverEmail, content, NotificationType.UPCOMING_RIDE_REMINDER, rideId);
        notification.setPassengerName(passengerName);
        notification.setActionUrl("/driver/upcoming-rides/" + rideId);
        return saveAndSend(notification, driverEmail);
    }

    // RATING_RECEIVED
    public NotificationDto sendRatingReceivedNotification(String driverEmail, Long rideId,
                                                          int stars, String passengerName, String comment) {
        String content = String.format("%s rated you %d stars%s Keep up the great work!",
                passengerName, stars,
                comment != null && !comment.isEmpty() ? " with the comment: \"" + comment + ".\"" : ".");

        Notification notification = new Notification(driverEmail, content, NotificationType.RATING_RECEIVED, rideId);
        notification.setPassengerName(passengerName);
        return saveAndSend(notification, driverEmail);
    }

    // RIDE_STARTED
    public void sendRideStartedNotification(String driverEmail, Long rideId) {
        String content = String.format("Your ride with id %d has started. Drive safely and provide excellent service!",
                rideId);

        Notification notification = new Notification(driverEmail, content, NotificationType.RIDE_STARTED, rideId);
        notification.setActionUrl("/driver/ride-tracking/" + rideId);
        saveAndSend(notification, driverEmail);
    }

    // ==================== ADMIN NOTIFICATIONS ====================

    // RIDE_REPORT
    public void sendRideReportNotification(String adminEmail, Long rideId,
                                           String reportType, String reportDetails) {
        String content = String.format("%s reported on ride #%d. User comment: %s",
                reportType, rideId, reportDetails);

        Notification notification = new Notification(adminEmail, content, NotificationType.RIDE_REPORT, rideId);
        notification.setActionUrl("/admin/ride-reports/" + rideId);
        saveAndSend(notification, adminEmail);
    }

    // NEW_REGISTRATION
    public NotificationDto sendNewRegistrationNotification(String adminEmail, String driverName,
                                                           String vehicleInfo) {
        String content = String.format("New driver %s submitted registration documents for review. Vehicle: %s. Background check pending.",
                driverName, vehicleInfo);

        Notification notification = new Notification(adminEmail, content, NotificationType.NEW_REGISTRATION, null);
        notification.setDriverName(driverName);
        notification.setActionUrl("/admin/pending-registrations");
        return saveAndSend(notification, adminEmail);
    }

    // PROFILE_CHANGE_REQUEST
    public NotificationDto sendProfileChangeRequestNotification(String adminEmail, String driverName,
                                                                Long driverId) {
        String content = String.format("Driver %s has requested profile changes. Please review and approve/reject.",
                driverName);

        Notification notification = new Notification(adminEmail, content,
                NotificationType.PROFILE_CHANGE_REQUEST, null);
        notification.setDriverName(driverName);
        notification.setActionUrl("/admin/driver-requests/" + driverId);
        return saveAndSend(notification, adminEmail);
    }

    // ==================== CHAT NOTIFICATIONS ====================

    // NEW_CHAT_MESSAGE
    public NotificationDto sendNewChatMessageNotification(String email, String senderName, boolean isAdmin) {
        String content = String.format("New message from %s in support chat.",
                isAdmin ? "Admin " + senderName : senderName);

        Notification notification = new Notification(email, content,
                NotificationType.NEW_CHAT_MESSAGE, null);
        notification.setActionUrl("/chat");
        return saveAndSend(notification, email);
    }
}