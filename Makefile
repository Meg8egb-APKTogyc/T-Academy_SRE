.PHONY: deploy apply helm-repo

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
