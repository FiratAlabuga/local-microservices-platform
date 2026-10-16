#!/usr/bin/env bash
# scripts/deploy-local.sh — Servisleri yerel olarak derler, container image üretir,
# kind cluster'ına yükler ve Helm ile deploy eder.
set -euo pipefail

CLUSTER_NAME="microservices-local"

echo "=== 1. Spring Boot Uygulamaları Derleniyor ==="
./gradlew bootJar --no-daemon

echo ""
echo "=== 2. Docker İmajları Oluşturuluyor ==="
docker build -t service-a:local services/service-a
docker build -t service-b:local services/service-b

echo ""
echo "=== 3. İmajlar kind Cluster'ına Yükleniyor ==="
kind load docker-image service-a:local --name "$CLUSTER_NAME"
kind load docker-image service-b:local --name "$CLUSTER_NAME"

echo ""
echo "=== 4. Vault & Kubernetes Secret Hazırlığı ==="
./infra/vault/init-vault.sh

echo ""
echo "=== 5. Helm ile Mikroservisler Deploy Ediliyor ==="
helm upgrade --install service-a infra/helm/service-a \
    -f infra/helm/service-a/values.yaml \
    -f infra/helm/service-a/values-local.yaml \
    --namespace microservices

helm upgrade --install service-b infra/helm/service-b \
    -f infra/helm/service-b/values.yaml \
    -f infra/helm/service-b/values-local.yaml \
    --namespace microservices

echo ""
echo "=== 6. Pod Durumu Kontrol Ediliyor ==="
kubectl get pods -n microservices
echo "✓ Local deployment tamamlandı."
