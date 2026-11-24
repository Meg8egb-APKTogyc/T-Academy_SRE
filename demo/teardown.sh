#!/usr/bin/env bash
# Удаляет namespace демо. Monitoring и PostgreSQL task-tracker не трогает.
# С флагом --envoy удаляет и Envoy Gateway.
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENVOY_GATEWAY_VERSION="${ENVOY_GATEWAY_VERSION:-v1.10.0}"

kubectl delete namespace deploy-demo --ignore-not-found --wait=false || true

if [[ "${1:-}" == "--envoy" ]]; then
  kubectl delete -f \
    "https://github.com/envoyproxy/gateway/releases/download/${ENVOY_GATEWAY_VERSION}/install.yaml" \
    --ignore-not-found >/dev/null 2>&1 || true
fi

echo "Демо удалено. Мониторинг и БД task-tracker сохранены."
