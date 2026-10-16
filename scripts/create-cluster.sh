#!/usr/bin/env bash
# scripts/create-cluster.sh — kind local Kubernetes cluster oluşturur ve namespace'leri hazırlar.
# İdempotent: cluster zaten varsa hata vermez.
set -euo pipefail

CLUSTER_NAME="microservices-local"
CONFIG_FILE="infra/kind/cluster-config.yaml"

echo "=== kind Kubernetes Cluster Hazırlığı ==="

if kind get clusters 2>/dev/null | grep -q "^${CLUSTER_NAME}$"; then
    echo "✓ Cluster '${CLUSTER_NAME}' zaten mevcut."
else
    echo "Cluster '${CLUSTER_NAME}' oluşturuluyor..."
    kind create cluster --config "$CONFIG_FILE" --name "$CLUSTER_NAME"
fi

echo "Kubernetes context kontrolü..."
kubectl cluster-info --context "kind-${CLUSTER_NAME}"

echo ""
echo "Namespace'ler hazırlanıyor..."
for ns in microservices monitoring vault argocd; do
    if kubectl get namespace "$ns" >/dev/null 2>&1; then
        echo "  ✓ namespace '$ns' zaten mevcut."
    else
        kubectl create namespace "$ns"
        echo "  + namespace '$ns' oluşturuldu."
    fi
done

echo ""
echo "✓ kind Kubernetes cluster başarıyla hazırlandı."
