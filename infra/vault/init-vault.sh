#!/usr/bin/env bash
# infra/vault/init-vault.sh — Local Vault'u başlatır, KV engine ve politikaları yükler,
# yetki testlerini gerçekleştirir ve Kubernetes secret'larını senkronize eder.
set -euo pipefail

VAULT_ADDR="http://127.0.0.1:8200"
ROOT_TOKEN="${VAULT_DEV_ROOT_TOKEN_ID:-dev-root-token-do-not-use-in-prod}"

echo "=== HashiCorp Vault Yapılandırması ==="
export VAULT_ADDR="$VAULT_ADDR"
export VAULT_TOKEN="$ROOT_TOKEN"

echo "1. KV-v2 Secret Engine kontrolü..."
docker exec -e VAULT_ADDR=http://127.0.0.1:8200 -e VAULT_TOKEN="$ROOT_TOKEN" vault vault secrets enable -path=secret kv-v2 2>/dev/null || echo "   KV-v2 engine zaten etkin."

echo "2. Politikalar yükleniyor..."
docker cp infra/vault/policy-service-a.hcl vault:/tmp/policy-service-a.hcl
docker cp infra/vault/policy-service-b.hcl vault:/tmp/policy-service-b.hcl

docker exec -e VAULT_ADDR=http://127.0.0.1:8200 -e VAULT_TOKEN="$ROOT_TOKEN" vault vault policy write service-a /tmp/policy-service-a.hcl
docker exec -e VAULT_ADDR=http://127.0.0.1:8200 -e VAULT_TOKEN="$ROOT_TOKEN" vault vault policy write service-b /tmp/policy-service-b.hcl

echo "3. Örnek secret'lar Vault'a yazılıyor..."
docker exec -e VAULT_ADDR=http://127.0.0.1:8200 -e VAULT_TOKEN="$ROOT_TOKEN" vault vault kv put secret/service-a/config \
    db-password="changeme_order_dev" \
    redis-password="changeme_redis_dev"

docker exec -e VAULT_ADDR=http://127.0.0.1:8200 -e VAULT_TOKEN="$ROOT_TOKEN" vault vault kv put secret/service-b/config \
    db-password="changeme_notification_dev"

echo "4. Kısıtlı yetkili servis token'ları üretiliyor..."
TOKEN_A=$(docker exec -e VAULT_ADDR=http://127.0.0.1:8200 -e VAULT_TOKEN="$ROOT_TOKEN" vault vault token create -policy=service-a -format=json | grep -o '"client_token": "[^"]*' | cut -d'"' -f4)
TOKEN_B=$(docker exec -e VAULT_ADDR=http://127.0.0.1:8200 -e VAULT_TOKEN="$ROOT_TOKEN" vault vault token create -policy=service-b -format=json | grep -o '"client_token": "[^"]*' | cut -d'"' -f4)

echo "5. Yetkilendirme (Least-Privilege) testleri gerçekleştiriliyor:"

# Service A kendi secret'ını okuyabilmeli
echo -n "  - Service A kendi secret'ını okuyor: "
docker exec -e VAULT_ADDR=http://127.0.0.1:8200 -e VAULT_TOKEN="$TOKEN_A" vault vault kv get secret/service-a/config >/dev/null
echo "BAŞARILI (OK)"

# Service A, Service B'nin secret'ını OKUYAMAMALI (403 Permission Denied)
echo -n "  - Service A, Service B secret'ına erişmeyi deniyor: "
if docker exec -e VAULT_ADDR=http://127.0.0.1:8200 -e VAULT_TOKEN="$TOKEN_A" vault vault kv get secret/service-b/config >/dev/null 2>&1; then
    echo "HATA: Yetkisiz erişim engellenemedi!"
    exit 1
else
    echo "ENGELLENDİ (Permission Denied - BEKLENEN DAVRANIŞ - OK)"
fi

# Service B kendi secret'ını okuyabilmeli
echo -n "  - Service B kendi secret'ını okuyor: "
docker exec -e VAULT_ADDR=http://127.0.0.1:8200 -e VAULT_TOKEN="$TOKEN_B" vault vault kv get secret/service-b/config >/dev/null
echo "BAŞARILI (OK)"

# Service B, Service A'nın secret'ını OKUYAMAMALI
echo -n "  - Service B, Service A secret'ına erişmeyi deniyor: "
if docker exec -e VAULT_ADDR=http://127.0.0.1:8200 -e VAULT_TOKEN="$TOKEN_B" vault vault kv get secret/service-a/config >/dev/null 2>&1; then
    echo "HATA: Yetkisiz erişim engellenemedi!"
    exit 1
else
    echo "ENGELLENDİ (Permission Denied - BEKLENEN DAVRANIŞ - OK)"
fi

echo ""
echo "6. Kubernetes Secret'ları microservices namespace'ine senkronize ediliyor..."
kubectl create secret generic service-a-secrets \
    --namespace microservices \
    --from-literal=db-password="changeme_order_dev" \
    --from-literal=redis-password="changeme_redis_dev" \
    --dry-run=client -o yaml | kubectl apply -f -

kubectl create secret generic service-b-secrets \
    --namespace microservices \
    --from-literal=db-password="changeme_notification_dev" \
    --dry-run=client -o yaml | kubectl apply -f -

echo "✓ HashiCorp Vault ve Kubernetes Secret yapılandırması başarıyla doğrulandı."
