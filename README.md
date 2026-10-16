# Local Microservices Platform

Tek GitHub deposunda iki bağımsız Spring Boot mikroservisi, Kubernetes deployment'ları, GitOps, CI/CD ve release yönetimi.

## Mimari

```
┌─────────────────────────────────────────────────────────────┐
│                   GitHub Actions CI/CD                       │
│  build → test → image → GHCR push → GitOps update          │
└──────────────────────────┬──────────────────────────────────┘
                           │
               ┌───────────▼───────────┐
               │    Argo CD (GitOps)   │
               │   Helm chart izleme   │
               └───────────┬───────────┘
                           │ sync
         ┌─────────────────▼──────────────────┐
         │      kind (local Kubernetes)        │
         │                                     │
         │  ┌──────────────┐ ┌──────────────┐ │
         │  │  Service A   │ │  Service B   │ │
         │  │ Order Svc    │ │ Notif. Svc   │ │
         │  │ :8080        │ │ :8081        │ │
         │  └──────┬───────┘ └──────▲───────┘ │
         │         │   Kafka event  │          │
         │         └────────────────┘          │
         │                                     │
         │  PostgreSQL  │  Redis  │  Vault     │
         └─────────────────────────────────────┘
```

## Araç gereksinimleri

| Araç | Sürüm |
|------|-------|
| Java (Corretto) | 25.0.3+ |
| Gradle | 9.5.1 (Wrapper ile) |
| Docker | 29+ |
| Docker Compose | v5+ |
| kubectl | v1.34+ |
| kind | v0.33+ |
| Helm | v4.3+ |
| gh (GitHub CLI) | v2.102+ |

## Hızlı başlangıç

### 1. Ön koşul kontrolü
```bash
./scripts/check-prerequisites.sh
```

### 2. Derleme ve test
```bash
./gradlew build
```

### 3. Docker Compose ile local altyapı
```bash
cp infra/docker/.env.example infra/docker/.env
# .env dosyasını düzenle
docker compose -f infra/docker/compose.yaml up -d
```

### 4. kind cluster oluştur
```bash
./scripts/create-cluster.sh
```

### 5. Helm ile deploy et
```bash
./scripts/deploy-local.sh
```

### 6. Smoke test
```bash
./scripts/smoke-test.sh
```

## Servisler

| Servis | Port | Açıklama |
|--------|------|----------|
| Order Service (A) | 8080 | Sipariş REST API |
| Notification Service (B) | 8081 | Kafka consumer |
| PostgreSQL | 5432 | Sipariş DB |
| PostgreSQL | 5433 | Bildirim DB |
| Redis | 6379 | Order cache |
| Kafka | 9092 | Mesajlaşma |
| Kafka UI | 8090 | Yönetim arayüzü |
| Vault | 8200 | Secret yönetimi |
| Argo CD | 8080 (port-forward) | GitOps |

## API örnekleri

### Sipariş oluştur
```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"00000000-0000-0000-0000-000000000001","totalAmount":99.99}'
```

### Sipariş listele
```bash
curl http://localhost:8080/api/v1/orders
```

### Health check
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
```

## Temizleme

```bash
./scripts/stop-local.sh
kind delete cluster --name microservices-local
```

## Daha fazla bilgi

- [Mimari](docs/architecture.md)
- [Local kurulum](docs/local-setup.md)
- [CI/CD](docs/ci-cd.md)
- [GitOps](docs/gitops.md)
- [Vault](docs/vault.md)
- [Release süreci](docs/release-process.md)
