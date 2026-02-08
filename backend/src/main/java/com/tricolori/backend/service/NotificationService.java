package com.tricolori.backend.service;

import com.tricolori.backend.dto.notifications.NotificationDto;
import com.tricolori.backend.entity.Notification;
import com.tricolori.backend.enums.NotificationType;
import com.tricolori.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Send a notification to a user via WebSocket and save it to the database
    @Transactional
    public NotificationDto sendNotification(String email, String content, NotificationType type, Long rideId) {
        log.info("Sending notification to {}: type={}, rideId={}", email, type, rideId);

        Notification notification = new Notification(email, content, type, rideId);
        notification = notificationRepository.save(notification);

        NotificationDto NotificationDto = convertToDTO(notification);

        // Send via WebSocket to user's personal topic
        messagingTemplate.convertAndSend("/topic/notifications/" + email, NotificationDto);

        log.info("Notification sent successfully: id={}", notification.getId());
        return NotificationDto;
    }

    // Get all notifications for a user
    @Transactional(readOnly = true)
    public List<NotificationDto> getAllNotifications(String email) {
        log.info("Fetching all notifications for user: {}", email);
        List<Notification> notifications = notificationRepository.findByEmailOrderByTimeDesc(email);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get unread notifications for a user
    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotifications(String email) {
        log.info("Fetching unread notifications for user: {}", email);
        List<Notification> notifications = notificationRepository.findByEmailAndOpenedOrderByTimeDesc(email, false);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get count of unread notifications
    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        return notificationRepository.countByEmailAndOpened(email, false);
    }

    // Mark a notification as read
    @Transactional
    public NotificationDto markAsRead(Long notificationId) {
        log.info("Marking notification as read: id={}", notificationId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        notification.setOpened(true);
        notification = notificationRepository.save(notification);

        return convertToDTO(notification);
    }

    // Mark all notifications as read for a user
    @Transactional
    public void markAllAsRead(String email) {
        log.info("Marking all notifications as read for user: {}", email);
        List<Notification> unreadNotifications = notificationRepository.findByEmailAndOpenedOrderByTimeDesc(email, false);

        unreadNotifications.forEach(notification -> notification.setOpened(true));
        notificationRepository.saveAll(unreadNotifications);

        log.info("Marked {} notifications as read", unreadNotifications.size());
    }

    // Delete a notification
    @Transactional
    public void deleteNotification(Long notificationId) {
        log.info("Deleting notification: id={}", notificationId);
        notificationRepository.deleteById(notificationId);
    }

    // Delete all notifications for a user
    @Transactional
    public void deleteAllNotifications(String email) {
        log.info("Deleting all notifications for user: {}", email);
        notificationRepository.deleteByEmail(email);
    }

    // Helper method to convert Notification entity to DTO
    private NotificationDto convertToDTO(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getEmail(),
                notification.getTime(),
                notification.isOpened(),
                notification.getContent(),
                notification.getType(),
                notification.getRideId(),
                notification.getActionUrl(),
                notification.getDriverName(),
                notification.getPassengerName()
        );
    }

    // ==================== PASSENGER NOTIFICATION METHODS ====================

    // Notify passenger that ride is starting soon
    public NotificationDto sendRideStartingNotification(String passengerEmail, Long rideId,
                                                        String driverName, String vehicleInfo,
                                                        String pickupLocation, int minutesUntilArrival) {
        String content = String.format("Your driver %s will arrive at %s in approximately %d minutes. The vehicle is %s.",
                driverName, pickupLocation, minutesUntilArrival, vehicleInfo);
        String actionUrl = "/passenger/ride-tracking/" + rideId;

        Notification notification = new Notification(passengerEmail, content, NotificationType.RIDE_STARTING, rideId);
        notification.setDriverName(driverName);
        notification.setActionUrl(actionUrl);
        notification = notificationRepository.save(notification);

        NotificationDto dto = convertToDTO(notification);
        messagingTemplate.convertAndSend("/topic/notifications/" + passengerEmail, dto);
        return dto;
    }

    // Notify passenger that ride was cancelled
    public NotificationDto sendRideCancelledNotification(String passengerEmail, Long rideId,
                                                         String scheduledTime, String from, String to, String reason) {
        String content = String.format("Your ride scheduled for %s from %s to %s has been cancelled%s. Your payment has been refunded.",
                scheduledTime, from, to, reason != null && !reason.isEmpty() ? " due to " + reason : "");

        Notification notification = new Notification(passengerEmail, content, NotificationType.RIDE_CANCELLED, rideId);
        notification = notificationRepository.save(notification);

        NotificationDto dto = convertToDTO(notification);
        messagingTemplate.convertAndSend("/topic/notifications/" + passengerEmail, dto);
        return dto;
    }

    // Notify passenger they were added to a shared ride
    public NotificationDto sendAddedToRideNotification(String passengerEmail, Long rideId,
                                                       String organizerName, String from, String to,
                                                       String scheduledTime, int totalPassengers) {
        String content = String.format("%s added you to a shared ride from %s to %s scheduled for %s. Total cost will be split between %d passengers.",
                organizerName, from, to, scheduledTime, totalPassengers);
        String actionUrl = "/passenger/ride-details/" + rideId;

        Notification notification = new Notification(passengerEmail, content, NotificationType.ADDED_TO_RIDE, rideId);
        notification.setPassengerName(organizerName);
        notification.setActionUrl(actionUrl);
        notification = notificationRepository.save(notification);

        NotificationDto dto = convertToDTO(notification);
        messagingTemplate.convertAndSend("/topic/notifications/" + passengerEmail, dto);
        return dto;
    }

    // Notify passenger to rate their completed ride
    public NotificationDto sendRatingReminderNotification(String passengerEmail, Long rideId,
                                                          String driverName, int hoursRemaining) {
        String content = String.format("How was your ride with %s? Your feedback helps us maintain quality service. You have %d hours remaining to submit your rating.",
                driverName, hoursRemaining);
        String actionUrl = "/passenger/ride-rating/" + rideId;

        Notification notification = new Notification(passengerEmail, content, NotificationType.RATING_REMINDER, rideId);
        notification.setDriverName(driverName);
        notification.setActionUrl(actionUrl);
        notification = notificationRepository.save(notification);

        NotificationDto dto = convertToDTO(notification);
        messagingTemplate.convertAndSend("/topic/notifications/" + passengerEmail, dto);
        return dto;
    }

    // Notify passenger that ride was completed
    public NotificationDto sendRideCompletedNotification(String passengerEmail, Long rideId,
                                                         String from, String to, double totalFare) {
        String content = String.format("Your ride from %s to %s has been completed. Total fare: %.2f RSD. Thank you for riding with us!",
                from, to, totalFare);
        String actionUrl = "/passenger/history";

        Notification notification = new Notification(passengerEmail, content, NotificationType.RIDE_COMPLETED, rideId);
        notification.setActionUrl(actionUrl);
        notification = notificationRepository.save(notification);

        NotificationDto dto = convertToDTO(notification);
        messagingTemplate.convertAndSend("/topic/notifications/" + passengerEmail, dto);
        return dto;
    }

    // ==================== DRIVER NOTIFICATION METHODS ====================

    // Notify driver of new ride request
    public NotificationDto sendNewRideRequestNotification(String driverEmail, Long rideId,
                                                          String passengerName, String pickup,
                                                          String dropoff, double estimatedFare, double distance) {
        String content = String.format("%s has requested a ride from %s to %s. Distance: %.1f km. Estimated fare: %.2f RSD.",
                passengerName, pickup, dropoff, distance, estimatedFare);
        String actionUrl = "/driver/ride-requests/" + rideId;

        Notification notification = new Notification(driverEmail, content, NotificationType.NEW_RIDE_REQUEST, rideId);
        notification.setPassengerName(passengerName);
        notification.setActionUrl(actionUrl);
        notification = notificationRepository.save(notification);

        NotificationDto dto = convertToDTO(notification);
        messagingTemplate.convertAndSend("/topic/notifications/" + driverEmail, dto);
        return dto;
    }

    //Notify driver of upcoming scheduled ride
    public NotificationDto sendUpcomingRideReminderNotification(String driverEmail, Long rideId,
                                                                int minutesUntilPickup, String pickupLocation,
                                                                String passengerName) {
        String content = String.format("You have a ride scheduled in %d minutes. Pickup location: %s. Passenger: %s. Make sure to arrive on time!",
                minutesUntilPickup, pickupLocation, passengerName);
        String actionUrl = "/driver/upcoming-rides/" + rideId;

        Notification notification = new Notification(driverEmail, content, NotificationType.UPCOMING_RIDE_REMINDER, rideId);
        notification.setPassengerName(passengerName);
        notification.setActionUrl(actionUrl);
        notification = notificationRepository.save(notification);

        NotificationDto dto = convertToDTO(notification);
        messagingTemplate.convertAndSend("/topic/notifications/" + driverEmail, dto);
        return dto;
    }

    // Notify driver they received a rating
    public NotificationDto sendRatingReceivedNotification(String driverEmail, Long rideId,
                                                          int stars, String passengerName, String comment) {
        String content = String.format("%s rated you %d stars%s Keep up the great work!",
                passengerName, stars,
                comment != null && !comment.isEmpty() ? " with the comment: \"" + comment + ".\"" : ".");

        Notification notification = new Notification(driverEmail, content, NotificationType.RATING_RECEIVED, rideId);
        notification.setPassengerName(passengerName);
        notification = notificationRepository.save(notification);

        NotificationDto dto = convertToDTO(notification);
        messagingTemplate.convertAndSend("/topic/notifications/" + driverEmail, dto);
        return dto;
    }

    // Notify driver that ride has started
    public NotificationDto sendRideStartedNotification(String driverEmail, Long rideId) {
        String content = "Your ride has started. Drive safely and provide excellent service!";

        Notification notification = new Notification(driverEmail, content, NotificationType.RIDE_STARTED, rideId);
        notification = notificationRepository.save(notification);

        NotificationDto dto = convertToDTO(notification);
        messagingTemplate.convertAndSend("/topic/notifications/" + driverEmail, dto);
        return dto;
    }
    

    // ==================== ADMIN NOTIFICATION METHODS ====================

    // Notify admin of ride report/complaint
    public NotificationDto sendRideReportNotification(String adminEmail, Long rideId,
                                                      String reportType, String reportDetails) {
        String content = String.format("%s reported on ride #%d. %s",
                reportType, rideId, reportDetails);
        String actionUrl = "/admin/ride-reports/" + rideId;

        Notification notification = new Notification(adminEmail, content, NotificationType.RIDE_REPORT, rideId);
        notification.setActionUrl(actionUrl);
        notification = notificationRepository.save(notification);

        NotificationDto dto = convertToDTO(notification);
        messagingTemplate.convertAndSend("/topic/notifications/" + adminEmail, dto);
        return dto;
    }

    // Notify admin of new driver registration
    public NotificationDto sendNewRegistrationNotification(String adminEmail, String driverName,
                                                           String vehicleInfo) {
        String content = String.format("New driver %s submitted registration documents for review. Vehicle: %s. Background check pending.",
                driverName, vehicleInfo);
        String actionUrl = "/admin/pending-registrations";

        Notification notification = new Notification(adminEmail, content, NotificationType.NEW_REGISTRATION, null);
        notification.setDriverName(driverName);
        notification.setActionUrl(actionUrl);
        notification = notificationRepository.save(notification);

        NotificationDto dto = convertToDTO(notification);
        messagingTemplate.convertAndSend("/topic/notifications/" + adminEmail, dto);
        return dto;
    }
}