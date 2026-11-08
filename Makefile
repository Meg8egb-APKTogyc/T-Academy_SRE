.PHONY: deploy apply helm-repo telegram-secret

helm-repo:
	helm repo add vm https://victoriametrics.github.io/helm-charts/ --force-update
	helm repo update vm

apply:
	kubectl apply -k k8s/

deploy: helm-repo
	helm upgrade --install vm-operator vm/victoria-metrics-operator --namespace monitoring --create-namespace
	kubectl apply -k k8s/
	helm upgrade --install vm-single vm/victoria-metrics-single --namespace monitoring -f vm-single-values.yaml
	helm upgrade --install vmagent vm/victoria-metrics-agent --namespace monitoring -f vmagent-values.yaml

telegram-secret:
	@test -n "$(TELEGRAM_BOT_TOKEN)" || (echo "ERROR: задайте TELEGRAM_BOT_TOKEN"; exit 1)
	@test -n "$(TELEGRAM_CHAT_ID)" || (echo "ERROR: задайте TELEGRAM_CHAT_ID"; exit 1)
	kubectl create secret generic telegram-bot -n monitoring \
		--from-literal=bot-token='$(TELEGRAM_BOT_TOKEN)' \
		--from-literal=chat-id='$(TELEGRAM_CHAT_ID)' \
		--dry-run=client -o yaml | kubectl apply -f -