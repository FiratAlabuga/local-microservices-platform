# Yerel Geliştirme Ortamı Kurulumu

## Ön Koşullar
- macOS / Linux
- Docker Desktop veya Colima (Docker 29+)
- Java 25 (Amazon Corretto)
- kubectl, kind, helm, gh (kurulum için: `brew install kind helm gh`)

## Adım Adım Kurulum

### 1. Ön Koşul Doğrulaması
```bash
./scripts/check-prerequisites.sh
```

### 2. Docker Compose Altyapısını Başlatma
```bash
./scripts/start-local.sh
```
Bu komut PostgreSQL, Redis, KRaft Kafka, Kafka-UI ve HashiCorp Vault konteynerlerini ayağa kaldırır.

### 3. Local Kubernetes (kind) Cluster'ı Oluşturma
```bash
./scripts/create-cluster.sh
```

### 4. Vault Secret Yapılandırması
```bash
./infra/vault/init-vault.sh
```

### 5. Mikroservisleri Derleme ve Deploy Etme
```bash
./scripts/deploy-local.sh
```

### 6. Uçtan Uca Doğrulama
```bash
./scripts/smoke-test.sh
```
