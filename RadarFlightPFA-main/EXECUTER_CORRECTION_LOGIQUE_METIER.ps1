# =====================================================
# Script PowerShell - Correction Logique Métier
# =====================================================
# Ce script corrige les données pour respecter la logique métier
# =====================================================

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  CORRECTION LOGIQUE METIER" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$DB_NAME = "flightradar"
$DB_USER = "postgres"
$DB_HOST = "localhost"
$DB_PORT = "5432"
$SCRIPT_FILE = "backend\database\CORRECTION_LOGIQUE_METIER.sql"

# Verifier que le fichier existe
if (-not (Test-Path $SCRIPT_FILE)) {
    Write-Host "ERREUR: Fichier introuvable: $SCRIPT_FILE" -ForegroundColor Red
    exit 1
}

Write-Host "Fichier trouve: $SCRIPT_FILE" -ForegroundColor Green
Write-Host ""

# Demander confirmation
Write-Host "ATTENTION: Cette correction va:" -ForegroundColor Yellow
Write-Host "  - Supprimer les avions en trop (garder 2 par aeroport)" -ForegroundColor White
Write-Host "  - Assigner correctement les pilotes aux avions" -ForegroundColor White
Write-Host "  - Creer les avions manquants si necessaire" -ForegroundColor White
Write-Host ""
$confirmation = Read-Host "Voulez-vous continuer? (O/N)"

if ($confirmation -ne "O" -and $confirmation -ne "o" -and $confirmation -ne "Y" -and $confirmation -ne "y") {
    Write-Host "Correction annulee." -ForegroundColor Red
    exit 0
}

Write-Host ""
Write-Host "Execution de la correction..." -ForegroundColor Yellow
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
        Write-Host "Correction terminee avec succes !" -ForegroundColor Green
        Write-Host ""
        Write-Host "Verifiez les resultats ci-dessus." -ForegroundColor Yellow
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

