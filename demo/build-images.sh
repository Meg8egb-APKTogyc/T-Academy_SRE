#!/usr/bin/env bash
# Собирает и загружает в minikube две версии backend.
# Версия запекается в образ через APP_VERSION и видна в /api/info и метриках.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
V1="${V1:-1.1.0}"
V2="${V2:-1.2.0}"

cd "$ROOT"
for version in "$V1" "$V2"; do
  echo "==> docker build task-tracker-backend:${version}"
  docker build --build-arg APP_VERSION="$version" -t "task-tracker-backend:${version}" .
  echo "==> minikube image load task-tracker-backend:${version}"
  minikube image load "task-tracker-backend:${version}"
done

echo
echo "Образы загружены: task-tracker-backend:${V1}, task-tracker-backend:${V2}"
