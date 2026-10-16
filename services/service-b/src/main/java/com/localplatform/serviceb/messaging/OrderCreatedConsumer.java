package com.localplatform.serviceb.messaging;

import com.localplatform.serviceb.event.OrderCreatedEvent;
import com.localplatform.serviceb.service.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedConsumer.class);

    private final NotificationService notificationService;

    public OrderCreatedConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
        topics = "${app.kafka.topics.order-created:order.created}",
        groupId = "${spring.kafka.consumer.group-id:notification-service}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onOrderCreated(ConsumerRecord<String, OrderCreatedEvent> record, Acknowledgment ack) {
        OrderCreatedEvent event = record.value();
        log.info("Received OrderCreatedEvent eventId={} orderId={} partition={} offset={}",
            event.eventId(), event.orderId(), record.partition(), record.offset());
        try {
            notificationService.processOrderCreated(event);
            if (ack != null) {
                ack.acknowledge();
            }
        } catch (Exception ex) {
            log.error("Failed to process OrderCreatedEvent eventId={}", event.eventId(), ex);
            // Manuel ack çağırmıyoruz, Kafka yeniden deneyecek
            throw ex;
        }
    }
}
