#!/usr/bin/env bash
# Поднимает только инфраструктуру демо: Envoy Gateway, namespace, Gateway,
# доступ к существующей БД task-tracker и Grafana-дашборд.
# Мониторинг (VictoriaMetrics/Grafana) должен быть уже развёрнут (make deploy).
set -euo pipefail

ENVOY_GATEWAY_VERSION="${ENVOY_GATEWAY_VERSION:-v1.10.0}"
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
NS="${NS:-deploy-demo}"

log() { printf '\n\033[1;32m==> %s\033[0m\n' "$*"; }
die() { printf '\033[1;31mОШИБКА: %s\033[0m\n' "$*" >&2; exit 1; }

for bin in kubectl minikube; do
  command -v "$bin" >/dev/null 2>&1 || die "не найден $bin"
done

if ! minikube status >/dev/null 2>&1; then
  log "Запускаем minikube"
  minikube start --driver=docker --memory=2560mb
fi

log "Проверяем Envoy Gateway ${ENVOY_GATEWAY_VERSION}"
if ! kubectl -n envoy-gateway-system get deployment envoy-gateway >/dev/null 2>&1; then
  kubectl apply --server-side -f \
    "https://github.com/envoyproxy/gateway/releases/download/${ENVOY_GATEWAY_VERSION}/install.yaml"
fi
kubectl wait --timeout=5m -n envoy-gateway-system deployment/envoy-gateway \
  --for=condition=Available

log "Применяем namespace, БД-доступ и Gateway"
kubectl apply -f "${HERE}/00-namespace.yaml"
kubectl apply -f "${HERE}/02-db.yaml"
kubectl apply -f "${HERE}/01-gateway.yaml"
kubectl wait --timeout=3m -n "$NS" gateway/eg --for=condition=Accepted

log "Проверяем доступность исходной БД"
if kubectl -n task-tracker get svc db >/dev/null 2>&1; then
  echo "PostgreSQL task-tracker доступна по db.task-tracker.svc.cluster.local:5432"
else
  echo "ВНИМАНИЕ: сервис db в namespace task-tracker не найден."
  echo "Разверните основной стек: make deploy (или хотя бы k8s/03-postgres.yaml)."
fi

if kubectl -n monitoring get svc grafana >/dev/null 2>&1; then
  log "Провиженим Grafana-дашборд"
  kubectl create configmap grafana-dashboard-deploy-strategies \
    --namespace monitoring \
    --from-file=deploy-strategies.json="${HERE}/monitoring/dashboard.json" \
    --dry-run=client -o yaml \
    | kubectl label --local=true --dry-run=client -o yaml -f - grafana_dashboard="1" \
    | kubectl apply -f -
else
  echo "Grafana не найдена — дашборд пропущен."
fi

cat <<EOF

Инфраструктура готова. Дальше:
  ./build-images.sh                  # образы 1.1.0 и 1.2.0
  ./traffic.sh                       # трафик через Envoy (порт 8888)
  kubectl apply -f app-v1.yaml -f app-v2.yaml
  kubectl apply -f strategies/canary/httproute-90-10.yaml
EOF
