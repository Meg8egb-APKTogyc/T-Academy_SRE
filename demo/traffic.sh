#!/usr/bin/env bash
# Непрерывный трафик через Envoy Gateway на /api/info.
# Печатает версию и под, которые ответили, и итоговое распределение.
#
# Использование:
#   ./traffic.sh              # бесконечно
#   ./traffic.sh 100          # 100 запросов и выход
#   HEADER='x-user-group: beta' ./traffic.sh 20   # A/B
#   RATE=0.2 ./traffic.sh     # пауза между запросами, сек
set -euo pipefail

NS="${NS:-deploy-demo}"
EG_NS="${EG_NS:-envoy-gateway-system}"
LOCAL_PORT="${LOCAL_PORT:-8888}"
RATE="${RATE:-0.2}"
COUNT="${1:-0}"
HEADER="${HEADER:-}"
GW="${GW:-http://localhost:${LOCAL_PORT}}"

SVC="$(kubectl get svc -n "$EG_NS" \
  -l gateway.envoyproxy.io/owning-gateway-name=eg \
  -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || true)"
if [[ -z "$SVC" ]]; then
  echo "Не найден Envoy-прокси для Gateway 'eg'." >&2
  echo "Сначала выполните ./setup.sh и проверьте: kubectl get gateway -n $NS eg" >&2
  exit 1
fi

echo "Пробрасываю :$LOCAL_PORT -> $EG_NS/svc/$SVC:80"
kubectl port-forward -n "$EG_NS" "svc/$SVC" "${LOCAL_PORT}:80" >/tmp/traffic-pf.log 2>&1 &
PF_PID=$!
trap 'kill "$PF_PID" 2>/dev/null || true' EXIT INT TERM

for _ in $(seq 1 240); do
  curl -sf "${GW}/api/info" >/dev/null 2>&1 && break
  sleep 0.5
done

echo "Трафик на ${GW}/api/info (заголовок: ${HEADER:-нет}). Ctrl-C — стоп."
echo "-------------------------------------------------------------------"

declare -A counts=()
errors=0
total=0
i=0

print_summary() {
  local label="$1"
  echo "---- $label: всего=$total, ошибок=$errors ----"
  for v in "${!counts[@]}"; do
    pct=$(python3 -c "print(f'{100*${counts[$v]}/${total}:.1f}')")
    printf '  %-10s %5d  (%5s%%)\n' "$v" "${counts[$v]}" "$pct"
  done
}

while :; do
  if [[ -n "$HEADER" ]]; then
    body="$(curl -sS --max-time 5 -H "$HEADER" "${GW}/api/info" || true)"
  else
    body="$(curl -sS --max-time 5 "${GW}/api/info" || true)"
  fi
  i=$((i + 1)); total=$((total + 1))

  parsed="$(printf '%s' "$body" | python3 -c '
import json,sys
try:
    d=json.load(sys.stdin)
    print("%s|%s|%s" % (d.get("version","?"), d.get("instanceId","?"), d.get("hostname","?")))
except Exception:
    print("ERROR||")
' 2>/dev/null || echo 'ERROR||')"

  version="${parsed%%|*}"
  rest="${parsed#*|}"
  instance="${rest%%|*}"
  host="${rest#*|}"

  if [[ "$version" == "ERROR" ]]; then
    errors=$((errors + 1))
    printf '%s | ОШИБКА: %s\n' "$(date +%H:%M:%S)" "${body:0:120}"
  else
    counts[$version]=$(( ${counts[$version]:-0} + 1 ))
    printf '%s | version=%-8s instance=%-10s host=%s\n' \
      "$(date +%H:%M:%S)" "$version" "$instance" "$host"
  fi

  if (( i % 25 == 0 )); then
    print_summary "промежуточный итог"
  fi
  if [[ "$COUNT" -gt 0 && "$i" -ge "$COUNT" ]]; then break; fi
  sleep "$RATE"
done

print_summary "ИТОГО"
