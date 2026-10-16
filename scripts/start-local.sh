#!/usr/bin/env bash
# scripts/start-local.sh — Docker Compose ile local altyapıyı başlatır.
# İdempotent: zaten çalışan containerlar yeniden oluşturulmaz.
set -euo pipefail

COMPOSE_FILE="infra/docker/compose.yaml"
ENV_FILE="infra/docker/.env"

if [ ! -f "$ENV_FILE" ]; then
    echo "⚠ $ENV_FILE bulunamadı. Örnek dosyadan kopyalanıyor..."
    cp "infra/docker/.env.example" "$ENV_FILE"
    echo "  → $ENV_FILE oluşturuldu. Gerekirse düzenleyin."
fi

echo "=== Local altyapı başlatılıyor ==="
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d

echo ""
echo "Servis durumu:"
docker compose -f "$COMPOSE_FILE" ps

echo ""
echo "Sağlık kontrolü bekleniyor (max 60s)..."
for i in $(seq 1 12); do
    if docker compose -f "$COMPOSE_FILE" ps | grep -qv "unhealthy"; then
        echo "✓ Altyapı hazır."
        break
    fi
    echo "  Bekleniyor ($((i*5))s)..."
    sleep 5
done
