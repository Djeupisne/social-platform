# ============================================================
# DEPLOIEMENT KUBERNETES - Social Platform
# Prérequis: kubectl connecté au cluster production
# ============================================================

Write-Host "🚀 Déploiement Kubernetes - Social Platform Production" -ForegroundColor Cyan

# Namespace
kubectl apply -f namespace/namespace.yaml
Write-Host "✅ Namespace créé" -ForegroundColor Green

# Secrets et ConfigMaps
kubectl apply -f secrets/secrets.yaml
kubectl apply -f configmaps/app-config.yaml
Write-Host "✅ Secrets et ConfigMaps appliqués" -ForegroundColor Green

# Infrastructure
kubectl apply -f infrastructure/postgres.yaml
kubectl apply -f infrastructure/redis.yaml
kubectl apply -f infrastructure/rabbitmq.yaml
Write-Host "⏳ Infrastructure en cours de démarrage..." -ForegroundColor Yellow

# Attendre que PostgreSQL soit prêt
kubectl wait --for=condition=ready pod -l app=postgres -n social-platform --timeout=120s
Write-Host "✅ PostgreSQL prêt" -ForegroundColor Green

# Services (ordre important)
kubectl apply -f services/config-server.yaml
kubectl wait --for=condition=ready pod -l app=config-server -n social-platform --timeout=120s
Write-Host "✅ Config Server prêt" -ForegroundColor Green

kubectl apply -f services/discovery-service.yaml
kubectl wait --for=condition=ready pod -l app=discovery-service -n social-platform --timeout=120s
Write-Host "✅ Discovery Service prêt" -ForegroundColor Green

kubectl apply -f services/api-gateway.yaml
kubectl apply -f services/auth-service.yaml
kubectl apply -f services/main-service.yaml
kubectl apply -f services/scoring-service.yaml
kubectl apply -f services/admin-service.yaml
kubectl apply -f services/report-service.yaml
Write-Host "⏳ Microservices en cours de démarrage..." -ForegroundColor Yellow

# HPA
kubectl apply -f services/hpa.yaml

# Ingress
kubectl apply -f ingress/ingress.yaml

Write-Host ""
Write-Host "✅ Déploiement terminé !" -ForegroundColor Green
Write-Host ""
kubectl get pods -n social-platform
Write-Host ""
kubectl get services -n social-platform
