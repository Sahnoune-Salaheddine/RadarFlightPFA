# =====================================================
# Script PowerShell pour Assigner un Avion au Pilote
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
$SQL_FILE = "ASSIGNER_AVION_RAPIDE.sql"

# Vérifier que le fichier SQL existe
if (-not (Test-Path $SQL_FILE)) {
    Write-Host "❌ Erreur: Le fichier SQL n'existe pas: $SQL_FILE" -ForegroundColor Red
    exit 1
}

Write-Host "1️⃣  Exécution du script SQL..." -ForegroundColor Yellow

# Exécuter le script SQL
$result = & psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f $SQL_FILE 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Script exécuté avec succès" -ForegroundColor Green
    Write-Host ""
    Write-Host "$result" -ForegroundColor Gray
} else {
    Write-Host "   ❌ Erreur lors de l'exécution du script" -ForegroundColor Red
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

