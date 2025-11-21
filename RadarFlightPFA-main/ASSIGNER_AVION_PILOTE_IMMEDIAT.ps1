# =====================================================
# Script PowerShell pour Assigner un Avion au Pilote
# =====================================================
# Ce script exécute le script SQL pour assigner un avion
# =====================================================

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  ASSIGNATION D'AVION AU PILOTE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration de la base de données
$DB_HOST = "localhost"
$DB_PORT = "5432"
$DB_USER = "postgres"
$DB_NAME = "flightradar"
$SQL_FILE = "backend\database\ASSIGNER_AVION_PILOTE_IMMEDIAT.sql"

# Vérifier que le fichier SQL existe
if (-not (Test-Path $SQL_FILE)) {
    Write-Host "❌ Erreur: Le fichier SQL n'existe pas: $SQL_FILE" -ForegroundColor Red
    Write-Host ""
    Write-Host "Vérifiez que vous êtes dans le répertoire racine du projet." -ForegroundColor Yellow
    exit 1
}

Write-Host "1️⃣  Vérification de la connexion à la base de données..." -ForegroundColor Yellow

# Vérifier la connexion
$testConnection = & psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT 1;" 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Erreur de connexion à la base de données" -ForegroundColor Red
    Write-Host "   Vérifiez que PostgreSQL est démarré et que les identifiants sont corrects" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Détails:" -ForegroundColor Gray
    Write-Host "   $testConnection" -ForegroundColor Gray
    exit 1
}

Write-Host "   ✅ Connexion réussie" -ForegroundColor Green
Write-Host ""

Write-Host "2️⃣  Exécution du script SQL..." -ForegroundColor Yellow

# Exécuter le script SQL
$result = & psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f $SQL_FILE 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Script exécuté avec succès" -ForegroundColor Green
    Write-Host ""
    Write-Host "3️⃣  Résultats:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "$result" -ForegroundColor Gray
} else {
    Write-Host "   ❌ Erreur lors de l'exécution du script" -ForegroundColor Red
    Write-Host ""
    Write-Host "Détails de l'erreur:" -ForegroundColor Yellow
    Write-Host "$result" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  ✅ ASSIGNATION TERMINÉE" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "💡 Redémarrez le backend pour que les changements soient pris en compte" -ForegroundColor Yellow
Write-Host ""

