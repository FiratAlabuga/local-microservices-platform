package com.localplatform.servicea.service;

import com.localplatform.servicea.domain.Order;
import com.localplatform.servicea.dto.CreateOrderRequest;
import com.localplatform.servicea.dto.OrderResponse;
import com.localplatform.servicea.event.OrderCreatedEvent;
import com.localplatform.servicea.messaging.OrderEventPublisher;
import com.localplatform.servicea.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final String CACHE_NAME = "orders";

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Cacheable(value = CACHE_NAME, key = "#id")
    public OrderResponse findById(UUID id) {
        return orderRepository.findById(id)
            .map(OrderResponse::from)
            .orElseThrow(() -> new OrderNotFoundException(id));
    }

    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
            .map(OrderResponse::from)
            .toList();
    }

    @Transactional
    @CacheEvict(value = CACHE_NAME, key = "#result.id()")
    public OrderResponse createOrder(CreateOrderRequest request) {
        var order = new Order();
        order.setCustomerId(request.customerId());
        order.setTotalAmount(request.totalAmount());
        Order saved = orderRepository.save(order);

        OrderCreatedEvent event = OrderCreatedEvent.of(
            saved.getId(), saved.getCustomerId(), saved.getTotalAmount());
        eventPublisher.publishOrderCreated(event);

        log.info("Order created id={} customerId={}", saved.getId(), saved.getCustomerId());
        return OrderResponse.from(saved);
    }
}
