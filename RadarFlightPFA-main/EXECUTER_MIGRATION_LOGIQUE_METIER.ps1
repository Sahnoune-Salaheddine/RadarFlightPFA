# =====================================================
# Script PowerShell - Migration Logique Métier
# =====================================================
# Ce script applique la logique métier complète :
# - 4 aéroports
# - 8 avions (2 par aéroport)
# - 8 pilotes (1 par avion, 2 par aéroport)
# - 4 radars (1 par aéroport)
# =====================================================

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  MIGRATION LOGIQUE METIER" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$DB_NAME = "flightradar"
$DB_USER = "postgres"
$DB_HOST = "localhost"
$DB_PORT = "5432"
$SCRIPT_FILE = "backend\database\MIGRATION_LOGIQUE_METIER.sql"

# Verifier que le fichier existe
if (-not (Test-Path $SCRIPT_FILE)) {
    Write-Host "ERREUR: Fichier introuvable: $SCRIPT_FILE" -ForegroundColor Red
    Write-Host "   Verifiez que vous etes dans le bon repertoire." -ForegroundColor Yellow
    exit 1
}

Write-Host "Fichier trouve: $SCRIPT_FILE" -ForegroundColor Green
Write-Host ""

# Demander confirmation
Write-Host "ATTENTION: Cette migration va:" -ForegroundColor Yellow
Write-Host "  - Ajouter des colonnes aux tables existantes" -ForegroundColor White
Write-Host "  - Creer/mettre a jour 4 aeroports" -ForegroundColor White
Write-Host "  - Creer/mettre a jour 8 avions (2 par aeroport)" -ForegroundColor White
Write-Host "  - Creer/mettre a jour 8 pilotes (1 par avion)" -ForegroundColor White
Write-Host "  - Creer/mettre a jour 4 radars (1 par aeroport)" -ForegroundColor White
Write-Host ""
$confirmation = Read-Host "Voulez-vous continuer? (O/N)"

if ($confirmation -ne "O" -and $confirmation -ne "o" -and $confirmation -ne "Y" -and $confirmation -ne "y") {
    Write-Host "Migration annulee par l'utilisateur." -ForegroundColor Red
    exit 0
}

Write-Host ""
Write-Host "Execution de la migration..." -ForegroundColor Yellow
Write-Host ""

# Executer la migration
try {
    $securePassword = Read-Host "Mot de passe PostgreSQL pour l'utilisateur '$DB_USER' (masque)" -AsSecureString
    $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    $plainPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
    
    $env:PGPASSWORD = $plainPassword
    
    $result = psql -U $DB_USER -d $DB_NAME -h $DB_HOST -p $DB_PORT -f $SCRIPT_FILE 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "Migration terminee avec succes !" -ForegroundColor Green
        Write-Host ""
        Write-Host "PROCHAINES ETAPES:" -ForegroundColor Yellow
        Write-Host "   1. Redemarrer le backend Spring Boot" -ForegroundColor White
        Write-Host "   2. Verifier que toutes les donnees sont correctes" -ForegroundColor White
        Write-Host "   3. Tester l'application" -ForegroundColor White
        Write-Host ""
    } else {
        Write-Host ""
        Write-Host "ERREUR lors de l'execution de la migration" -ForegroundColor Red
        Write-Host "   Code de sortie: $LASTEXITCODE" -ForegroundColor Red
        Write-Host ""
        Write-Host "Sortie de psql:" -ForegroundColor Yellow
        Write-Host $result
        Write-Host ""
        exit 1
    }
    
} catch {
    Write-Host ""
    Write-Host "ERREUR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Verifiez que psql est installe et dans le PATH." -ForegroundColor Yellow
    exit 1
} finally {
    # Nettoyer le mot de passe de l'environnement
    Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue
}

Write-Host ""

