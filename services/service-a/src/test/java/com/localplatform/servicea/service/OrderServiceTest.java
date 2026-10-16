package com.localplatform.servicea.service;

import com.localplatform.servicea.domain.Order;
import com.localplatform.servicea.domain.OrderStatus;
import com.localplatform.servicea.dto.CreateOrderRequest;
import com.localplatform.servicea.dto.OrderResponse;
import com.localplatform.servicea.messaging.OrderEventPublisher;
import com.localplatform.servicea.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = new Order();
        sampleOrder.setId(UUID.randomUUID());
        sampleOrder.setCustomerId(UUID.randomUUID());
        sampleOrder.setStatus(OrderStatus.PENDING);
        sampleOrder.setTotalAmount(new BigDecimal("99.99"));
        sampleOrder.setCreatedAt(Instant.now());
        sampleOrder.setUpdatedAt(Instant.now());
    }

    @Test
    void findById_whenExists_returnsResponse() {
        when(orderRepository.findById(sampleOrder.getId()))
            .thenReturn(Optional.of(sampleOrder));

        OrderResponse response = orderService.findById(sampleOrder.getId());

        assertThat(response.id()).isEqualTo(sampleOrder.getId());
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.totalAmount()).isEqualByComparingTo("99.99");
    }

    @Test
    void findById_whenNotExists_throwsOrderNotFoundException() {
        UUID randomId = UUID.randomUUID();
        when(orderRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(randomId))
            .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void findAll_returnsMappedList() {
        when(orderRepository.findAll()).thenReturn(List.of(sampleOrder));

        List<OrderResponse> all = orderService.findAll();

        assertThat(all).hasSize(1);
        assertThat(all.getFirst().id()).isEqualTo(sampleOrder.getId());
    }

    @Test
    void createOrder_savesAndPublishesEvent() {
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        CreateOrderRequest req = new CreateOrderRequest(
            sampleOrder.getCustomerId(), new BigDecimal("99.99"));

        OrderResponse response = orderService.createOrder(req);

        assertThat(response.id()).isEqualTo(sampleOrder.getId());
        verify(eventPublisher, times(1)).publishOrderCreated(any());
    }
}
