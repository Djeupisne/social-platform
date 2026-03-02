# ============================================================
# DÉPLOIEMENT KEYCLOAK 25 - Social Platform Togo
# ============================================================

$BASE = "C:\wamp64\www\social-platform"
$K8S = "$BASE\k8s"
$NAMESPACE = "social-platform"

Write-Host "🚀 Déploiement Keycloak 25..." -ForegroundColor Cyan

# ─── 1. Créer la base de données Keycloak dans PostgreSQL ────────────────────
Write-Host "📦 Création de la base keycloak_db..." -ForegroundColor Yellow
kubectl exec -it -n $NAMESPACE deployment/postgres -- psql -U social_user -c "CREATE DATABASE keycloak_db;" 2>$null
kubectl exec -it -n $NAMESPACE deployment/postgres -- psql -U social_user -c "GRANT ALL PRIVILEGES ON DATABASE keycloak_db TO social_user;" 2>$null
Write-Host "✅ Base keycloak_db prête" -ForegroundColor Green

# ─── 2. Créer le ConfigMap avec le realm ─────────────────────────────────────
Write-Host "⚙️ Création du ConfigMap realm..." -ForegroundColor Yellow

$REALM_JSON = Get-Content "$K8S\realm-social-togo.json" -Raw

kubectl create configmap keycloak-realm-config `
    --from-file=realm-social-togo.json="$K8S\realm-social-togo.json" `
    -n $NAMESPACE `
    --dry-run=client -o yaml | kubectl apply -f -

Write-Host "✅ ConfigMap realm créé" -ForegroundColor Green

# ─── 3. Télécharger l'image Keycloak ─────────────────────────────────────────
Write-Host "📥 Téléchargement image Keycloak 25..." -ForegroundColor Yellow
docker pull quay.io/keycloak/keycloak:25.0
Write-Host "✅ Image téléchargée" -ForegroundColor Green

# ─── 4. Déployer Keycloak ─────────────────────────────────────────────────────
Write-Host "🚀 Déploiement Keycloak..." -ForegroundColor Yellow
kubectl apply -f "$K8S\keycloak.yaml"
Write-Host "✅ Keycloak déployé" -ForegroundColor Green

# ─── 5. Attendre que Keycloak démarre ────────────────────────────────────────
Write-Host "⏳ Attente démarrage Keycloak (2-3 minutes)..." -ForegroundColor Yellow
Write-Host "   Surveiller avec: kubectl get pods -n social-platform -w" -ForegroundColor Gray

# ─── 6. Mettre à jour les variables des services ─────────────────────────────
Write-Host "🔧 Mise à jour variables Keycloak..." -ForegroundColor Yellow

kubectl set env deployment/auth-service -n $NAMESPACE `
    KEYCLOAK_URL="http://keycloak:8080" `
    KEYCLOAK_REALM="social-togo" `
    KEYCLOAK_CLIENT_ID="social-platform" `
    KEYCLOAK_CLIENT_SECRET="social-platform-secret-2024" `
    KEYCLOAK_ADMIN="admin" `
    KEYCLOAK_ADMIN_PASSWORD="Admin@Keycloak2024!"

Write-Host "✅ Variables mises à jour" -ForegroundColor Green

# ─── 7. Afficher les infos de connexion ──────────────────────────────────────
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  KEYCLOAK DÉPLOYÉ !" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 Accès Keycloak Admin Console:" -ForegroundColor Yellow
Write-Host "   kubectl port-forward svc/keycloak 8080:8080 -n social-platform" -ForegroundColor White
Write-Host "   Puis: http://localhost:8080" -ForegroundColor White
Write-Host "   Login: admin / Admin@Keycloak2024!" -ForegroundColor White
Write-Host ""
Write-Host "📋 Realm configuré: social-togo" -ForegroundColor Yellow
Write-Host "📋 Rôles: AGENT, CHEF_MENAGE, SUPER_ADMIN" -ForegroundColor Yellow
Write-Host "📋 Client: social-platform" -ForegroundColor Yellow
Write-Host "📋 Utilisateur admin: admin@social-togo.tg / Admin@Togo2024!" -ForegroundColor Yellow