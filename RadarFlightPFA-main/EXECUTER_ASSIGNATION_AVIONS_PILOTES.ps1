# =====================================================
# Script PowerShell - Assigner Avions aux Pilotes
# =====================================================
# Ce script verifie et assigne automatiquement un avion
# a chaque pilote selon la logique metier
# =====================================================

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  ASSIGNATION AVIONS AUX PILOTES" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$DB_NAME = "flightradar"
$DB_USER = "postgres"
$DB_HOST = "localhost"
$DB_PORT = "5432"
$SCRIPT_FILE = "backend\database\VERIFIER_ET_ASSIGNER_AVIONS_PILOTES.sql"

# Verifier que le fichier existe
if (-not (Test-Path $SCRIPT_FILE)) {
    Write-Host "ERREUR: Fichier introuvable: $SCRIPT_FILE" -ForegroundColor Red
    exit 1
}

Write-Host "Fichier trouve: $SCRIPT_FILE" -ForegroundColor Green
Write-Host ""

# Demander confirmation
Write-Host "Ce script va:" -ForegroundColor Yellow
Write-Host "  - Verifier l'etat actuel des assignations" -ForegroundColor White
Write-Host "  - Assigner automatiquement un avion a chaque pilote" -ForegroundColor White
Write-Host "  - Respecter la logique metier (1 pilote = 1 avion)" -ForegroundColor White
Write-Host ""
$confirmation = Read-Host "Voulez-vous continuer? (O/N)"

if ($confirmation -ne "O" -and $confirmation -ne "o" -and $confirmation -ne "Y" -and $confirmation -ne "y") {
    Write-Host "Operation annulee." -ForegroundColor Red
    exit 0
}

Write-Host ""
Write-Host "Execution du script..." -ForegroundColor Yellow
Write-Host ""

# Executer le script
try {
    $securePassword = Read-Host "Mot de passe PostgreSQL pour l'utilisateur '$DB_USER' (masque)" -AsSecureString
    $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    $plainPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
    
    $env:PGPASSWORD = $plainPassword
    
    $result = psql -U $DB_USER -d $DB_NAME -h $DB_HOST -p $DB_PORT -f $SCRIPT_FILE 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "Assignation terminee avec succes !" -ForegroundColor Green
        Write-Host ""
        Write-Host "PROCHAINES ETAPES:" -ForegroundColor Yellow
        Write-Host "   1. Redemarrer le backend Spring Boot" -ForegroundColor White
        Write-Host "   2. Rafraichir le frontend (F5)" -ForegroundColor White
        Write-Host "   3. Se connecter en tant que pilote" -ForegroundColor White
        Write-Host ""
    } else {
        Write-Host ""
        Write-Host "ERREUR lors de l'execution" -ForegroundColor Red
        Write-Host $result
        exit 1
    }
    
} catch {
    Write-Host ""
    Write-Host "ERREUR: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
} finally {
    Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue
}

Write-Host ""

