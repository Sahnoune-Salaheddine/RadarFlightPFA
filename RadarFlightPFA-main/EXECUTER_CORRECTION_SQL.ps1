# Script PowerShell pour exécuter la correction SQL
Write-Host "🔧 Correction de la liaison Pilote ⇄ Avion" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier que PostgreSQL est accessible
Write-Host "1️⃣  Vérification de PostgreSQL..." -ForegroundColor Yellow
try {
    $pgTest = & psql --version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✅ PostgreSQL accessible" -ForegroundColor Green
    } else {
        Write-Host "   ⚠️  psql non trouvé dans le PATH" -ForegroundColor Yellow
        Write-Host "   💡 Essayez d'utiliser pgAdmin ou exécutez le script manuellement" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   ⚠️  psql non trouvé" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "2️⃣  Instructions pour exécuter le script SQL :" -ForegroundColor Yellow
Write-Host ""
Write-Host "   Option A : Via psql (ligne de commande)" -ForegroundColor Cyan
Write-Host "   psql -U postgres -d flightradar -f CORRIGER_PILOTE_AVION_RAPIDE.sql" -ForegroundColor Gray
Write-Host ""
Write-Host "   Option B : Via pgAdmin" -ForegroundColor Cyan
Write-Host "   1. Ouvrir pgAdmin" -ForegroundColor Gray
Write-Host "   2. Se connecter à PostgreSQL" -ForegroundColor Gray
Write-Host "   3. Sélectionner la base 'flightradar'" -ForegroundColor Gray
Write-Host "   4. Ouvrir Query Tool" -ForegroundColor Gray
Write-Host "   5. Copier-coller le contenu de CORRIGER_PILOTE_AVION_RAPIDE.sql" -ForegroundColor Gray
Write-Host "   6. Exécuter (F5)" -ForegroundColor Gray
Write-Host ""
Write-Host "   Option C : Via PowerShell (si psql est dans le PATH)" -ForegroundColor Cyan
Write-Host "   psql -U postgres -d flightradar -f CORRIGER_PILOTE_AVION_RAPIDE.sql" -ForegroundColor Gray
Write-Host ""

# Essayer d'exécuter automatiquement si psql est disponible
$sqlFile = "CORRIGER_PILOTE_AVION_RAPIDE.sql"
if (Test-Path $sqlFile) {
    Write-Host "3️⃣  Tentative d'exécution automatique..." -ForegroundColor Yellow
    
    # Demander les informations de connexion
    $dbUser = Read-Host "   Nom d'utilisateur PostgreSQL (défaut: postgres)"
    if ([string]::IsNullOrWhiteSpace($dbUser)) {
        $dbUser = "postgres"
    }
    
    $dbName = Read-Host "   Nom de la base de données (défaut: flightradar)"
    if ([string]::IsNullOrWhiteSpace($dbName)) {
        $dbName = "flightradar"
    }
    
    Write-Host ""
    Write-Host "   Exécution de la commande..." -ForegroundColor Gray
    Write-Host "   psql -U $dbUser -d $dbName -f $sqlFile" -ForegroundColor Gray
    Write-Host ""
    
    try {
        & psql -U $dbUser -d $dbName -f $sqlFile
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "   ✅ Script exécuté avec succès!" -ForegroundColor Green
        } else {
            Write-Host ""
            Write-Host "   ❌ Erreur lors de l'exécution" -ForegroundColor Red
            Write-Host "   💡 Vérifiez les identifiants et que PostgreSQL est démarré" -ForegroundColor Yellow
        }
    } catch {
        Write-Host ""
        Write-Host "   ⚠️  Impossible d'exécuter automatiquement" -ForegroundColor Yellow
        Write-Host "   💡 Utilisez pgAdmin ou exécutez manuellement" -ForegroundColor Yellow
    }
} else {
    Write-Host "   ⚠️  Fichier SQL non trouvé: $sqlFile" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "✅ Instructions affichées!" -ForegroundColor Green
Write-Host ""
Write-Host "💡 Après avoir exécuté le script SQL :" -ForegroundColor Cyan
Write-Host "   1. Redémarrer le backend (si nécessaire)" -ForegroundColor Gray
Write-Host "   2. Rafraîchir le frontend" -ForegroundColor Gray
Write-Host "   3. Se reconnecter avec pilote_cmn1 / pilote123" -ForegroundColor Gray

