package com.localplatform.servicea.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localplatform.servicea.domain.OrderStatus;
import com.localplatform.servicea.dto.CreateOrderRequest;
import com.localplatform.servicea.dto.OrderResponse;
import com.localplatform.servicea.service.OrderNotFoundException;
import com.localplatform.servicea.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    private OrderResponse sampleResponse() {
        return new OrderResponse(
            UUID.randomUUID(),
            UUID.randomUUID(),
            OrderStatus.PENDING,
            new BigDecimal("99.99"),
            Instant.now(),
            Instant.now()
        );
    }

    @Test
    void listOrders_returnsOk() throws Exception {
        when(orderService.findAll()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/v1/orders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getOrder_whenFound_returnsOk() throws Exception {
        OrderResponse resp = sampleResponse();
        when(orderService.findById(resp.id())).thenReturn(resp);

        mockMvc.perform(get("/api/v1/orders/{id}", resp.id()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(resp.id().toString()));
    }

    @Test
    void getOrder_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.findById(id)).thenThrow(new OrderNotFoundException(id));

        mockMvc.perform(get("/api/v1/orders/{id}", id))
            .andExpect(status().isNotFound());
    }

    @Test
    void createOrder_withValidBody_returns201() throws Exception {
        OrderResponse resp = sampleResponse();
        when(orderService.createOrder(any())).thenReturn(resp);

        CreateOrderRequest req = new CreateOrderRequest(UUID.randomUUID(), new BigDecimal("99.99"));

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
