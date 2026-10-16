package com.localplatform.serviceb.service;

import com.localplatform.serviceb.domain.Notification;
import com.localplatform.serviceb.event.OrderCreatedEvent;
import com.localplatform.serviceb.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void processOrderCreated(OrderCreatedEvent event) {
        // Idempotency: aynı eventId daha önce işlendiyse atla
        if (notificationRepository.existsByEventId(event.eventId())) {
            log.info("Duplicate event skipped eventId={}", event.eventId());
            return;
        }

        Notification notification = new Notification();
        notification.setEventId(event.eventId());
        notification.setOrderId(event.orderId());
        notificationRepository.save(notification);

        log.info("Notification processed and saved for eventId={} orderId={}", event.eventId(), event.orderId());
    }
}
