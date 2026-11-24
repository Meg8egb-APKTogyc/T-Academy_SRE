#!/usr/bin/env bash
# Сбрасывает приложение между стратегиями, сохраняя namespace, Gateway,
# доступ к БД и мониторинг.
set -euo pipefail
NS="${NS:-deploy-demo}"

echo "Удаляю приложения и маршруты в namespace $NS ..."
kubectl delete httproute --all -n "$NS" --ignore-not-found
kubectl delete deployment task-tracker task-tracker-v1 task-tracker-v2 -n "$NS" --ignore-not-found
kubectl delete service task-tracker task-tracker-v1 task-tracker-v2 -n "$NS" --ignore-not-found
echo "Готово. Gateway 'eg', БД и мониторинг на месте."
