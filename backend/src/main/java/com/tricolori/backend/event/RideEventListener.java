package com.tricolori.backend.event;

import com.tricolori.backend.dto.ride.RideUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class RideEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRideAssigned(RideAssignedEvent event) {
        messagingTemplate.convertAndSendToUser(
                event.getDriverEmail(),
                "/queue/ride-assigned",
                event.getRideId()
        );
        log.info("WebSocket: Driver {{}} notified with Ride ID: {{}}",
                event.getDriverEmail(), event.getRideId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRideStatusUpdate(RideStatusUpdateEvent event) {
        for (String email : event.getPassengerEmails()) {
            messagingTemplate.convertAndSendToUser(
                    email,
                    "/queue/ride-updates",
                    new RideUpdateMessage(event.getStatus(), event.getRideId(), event.getMessage())
            );
            log.info("WebSocket: Passenger {} notified with status: {}", email, event.getStatus());
        }
    }
}