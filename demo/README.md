# task-tracker: стратегии развёртывания

Демо на Envoy Gateway (Gateway API): Rolling, Blue/Green, Canary, A/B, Shadow.
Приложение использует существующую PostgreSQL `task-tracker`, поэтому состояние
не зависит от пода.

Быстрый старт:

```bash
cd /home/meg8egb/IdeaProjects/Tink_SRE/demo
./setup.sh
./build-images.sh
./reset.sh
kubectl apply -f app-v1.yaml -f app-v2.yaml
kubectl apply -f strategies/blue-green/httproute-blue.yaml
./traffic.sh 10
```

Логи прогонов лежат в `logs/`.
