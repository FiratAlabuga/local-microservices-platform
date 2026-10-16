#!/usr/bin/env bash
# scripts/check-prerequisites.sh — Gerekli araçların kurulu ve uyumlu sürümde olduğunu kontrol eder.
# İdempotent: birden fazla çalıştırılabilir.
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

ERRORS=0

check() {
    local name="$1"
    local cmd="$2"
    local min_version="$3"

    if output=$(eval "$cmd" 2>&1); then
        echo -e "${GREEN}✓${NC} $name — $output"
    else
        echo -e "${RED}✗${NC} $name — kurulu değil veya erişilemiyor"
        echo "  Öneri: $min_version"
        ERRORS=$((ERRORS + 1))
    fi
}

echo "=== Local Microservices Platform — Ön koşul kontrolü ==="
echo ""

check "Java 25+"      "java -version 2>&1 | head -1"                "https://aws.amazon.com/corretto/"
check "Gradle Wrapper" "./gradlew --version 2>&1 | grep '^Gradle'"  "repo içinde ./gradlew mevcut olmalı"
check "Docker"        "docker version --format 'Client {{.Client.Version}}' 2>/dev/null" "https://docs.docker.com/get-docker/"
check "Docker Compose" "docker compose version 2>/dev/null"          "Docker ile birlikte gelir"
check "kubectl"       "kubectl version --client --short 2>/dev/null" "https://kubernetes.io/docs/tasks/tools/"
check "kind"          "kind version"                                  "brew install kind"
check "Helm"          "helm version --short"                         "brew install helm"
check "gh (GitHub CLI)" "gh --version | head -1"                    "brew install gh"
check "git"           "git --version"                                "https://git-scm.com/"

echo ""
if [ "$ERRORS" -eq 0 ]; then
    echo -e "${GREEN}Tüm ön koşullar karşılandı.${NC}"
else
    echo -e "${RED}$ERRORS ön koşul eksik veya hatalı. Lütfen yukarıdaki önerileri takip edin.${NC}"
    exit 1
fi
