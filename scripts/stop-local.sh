#!/usr/bin/env bash
# scripts/stop-local.sh — Yerel geliştirme ortamını güvenli biçimde durdurur.
set -euo pipefail

echo "=== Local Altyapı Durduruluyor ==="
docker compose -f infra/docker/compose.yaml --env-file infra/docker/.env down

if [ "${1:-}" = "--all" ]; then
    echo "kind Kubernetes Cluster siliniyor..."
    kind delete cluster --name microservices-local || true
    echo "Tüm kaynaklar temizlendi."
else
    echo "Docker container'ları durduruldu. (Cluster'ı da silmek için: ./scripts/stop-local.sh --all)"
fi
