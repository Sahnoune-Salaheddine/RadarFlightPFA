# =====================================================
# Script PowerShell pour Se Connecter à la Base de Données
# =====================================================

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  CONNEXION À LA BASE DE DONNÉES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration de la base de données
$DB_HOST = "localhost"
$DB_PORT = "5432"
$DB_USER = "postgres"
$DB_NAME = "flightradar"

Write-Host "Paramètres de connexion:" -ForegroundColor Yellow
Write-Host "  Host: $DB_HOST" -ForegroundColor Gray
Write-Host "  Port: $DB_PORT" -ForegroundColor Gray
Write-Host "  Database: $DB_NAME" -ForegroundColor Gray
Write-Host "  User: $DB_USER" -ForegroundColor Gray
Write-Host ""

# Vérifier que psql est disponible
$psqlPath = Get-Command psql -ErrorAction SilentlyContinue

if (-not $psqlPath) {
    Write-Host "❌ Erreur: psql n'est pas trouvé dans le PATH" -ForegroundColor Red
    Write-Host ""
    Write-Host "Solutions:" -ForegroundColor Yellow
    Write-Host "  1. Vérifier que PostgreSQL est installé" -ForegroundColor Gray
    Write-Host "  2. Ajouter PostgreSQL au PATH Windows" -ForegroundColor Gray
    Write-Host "  3. Utiliser pgAdmin (interface graphique)" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Chemin typique: C:\Program Files\PostgreSQL\14\bin\psql.exe" -ForegroundColor Gray
    exit 1
}

Write-Host "✅ psql trouvé: $($psqlPath.Source)" -ForegroundColor Green
Write-Host ""

# Test de connexion rapide
Write-Host "Test de connexion..." -ForegroundColor Yellow
$testResult = & psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT version();" 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Connexion réussie!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Connexion interactive..." -ForegroundColor Yellow
    Write-Host "Tapez \q pour quitter" -ForegroundColor Gray
    Write-Host ""
    
    # Connexion interactive
    & psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME
} else {
    Write-Host "❌ Erreur de connexion" -ForegroundColor Red
    Write-Host ""
    Write-Host "Détails:" -ForegroundColor Yellow
    Write-Host "$testResult" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Vérifications:" -ForegroundColor Yellow
    Write-Host "  1. PostgreSQL est-il démarré?" -ForegroundColor Gray
    Write-Host "  2. Le mot de passe est-il correct? (postgres)" -ForegroundColor Gray
    Write-Host "  3. La base de données 'flightradar' existe-t-elle?" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Pour créer la base de données:" -ForegroundColor Yellow
    Write-Host "  psql -U postgres -c 'CREATE DATABASE flightradar;'" -ForegroundColor Gray
    exit 1
}

