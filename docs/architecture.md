# Mimari Tasarım

## 1. Genel Mimari

Platform iki bağımsız Java mikroservisi ve onları destekleyen veri/mesajlaşma katmanından oluşur:

```
                  ┌──────────────────────────────┐
                  │        Order Service         │
                  │   Java 25 · Spring Boot 4    │
                  └────────┬────────────┬────────┘
                           │            │
            (Sync / Cache) │            │ (Async Event)
                           ▼            ▼
             ┌────────────────┐   ┌───────────────┐
             │ Redis 7 Alpine │   │ Kafka (KRaft) │
             │  Order Cache   │   │ order.created │
             └────────────────┘   └───────┬───────┘
                                          │
                                          ▼
                  ┌──────────────────────────────┐
                  │     Notification Service     │
                  │   Java 25 · Spring Boot 4    │
                  └──────────────┬───────────────┘
                                 │
                                 ▼
                     ┌───────────────────────┐
                     │ PostgreSQL 16 Alpine  │
                     │  Notifications & Idem │
                     └───────────────────────┘
```

## 2. Servis Sorumlulukları

### Service A: Order Service
- Port: 8080
- REST API: `/api/v1/orders`
- Kalıcı depolama: PostgreSQL (`orders` tablosu, Flyway migration)
- Cache: Redis (`orders` key, configurable TTL)
- Event Publisher: `order.created` Kafka topic'ine `OrderCreatedEvent` üretimi
- Gözlemlenebilirlik: Structured Logging (MDC, JSON format), Spring Boot Actuator liveness/readiness probes

### Service B: Notification Service
- Port: 8081
- Kafka Consumer: `order.created` topic'ini manual ack modunda dinleme
- Kalıcı depolama: PostgreSQL (`notifications` tablosu, Flyway migration)
- Idempotency: `event_id` bazında duplicate check (`existsByEventId`). Yinelenen mesajlar güvenle atlanır.
