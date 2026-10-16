package com.localplatform.serviceb.service;

import com.localplatform.serviceb.domain.Notification;
import com.localplatform.serviceb.event.OrderCreatedEvent;
import com.localplatform.serviceb.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private OrderCreatedEvent sampleEvent() {
        return new OrderCreatedEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            new BigDecimal("99.99"),
            Instant.now()
        );
    }

    @Test
    void processOrderCreated_newEvent_savesNotification() {
        OrderCreatedEvent event = sampleEvent();
        when(notificationRepository.existsByEventId(event.eventId())).thenReturn(false);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.processOrderCreated(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getEventId()).isEqualTo(event.eventId());
        assertThat(captor.getValue().getOrderId()).isEqualTo(event.orderId());
    }

    @Test
    void processOrderCreated_duplicateEvent_skipsWithoutSaving() {
        OrderCreatedEvent event = sampleEvent();
        when(notificationRepository.existsByEventId(event.eventId())).thenReturn(true);

        notificationService.processOrderCreated(event);

        verify(notificationRepository, never()).save(any());
    }
}
