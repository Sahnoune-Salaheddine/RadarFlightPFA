# Script PowerShell pour vérifier et corriger les migrations
# Usage: .\VERIFIER_ET_CORRIGER.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  VERIFICATION ET CORRECTION" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Chemin PostgreSQL
$postgresPath = "C:\Program Files\PostgreSQL\16\bin"
if (-not (Test-Path $postgresPath)) {
    $postgresPath = "C:\Program Files\PostgreSQL\15\bin"
}

$psqlExe = Join-Path $postgresPath "psql.exe"

if (-not (Test-Path $psqlExe)) {
    Write-Host "ERREUR: PostgreSQL non trouve" -ForegroundColor Red
    Write-Host "Ajoutez PostgreSQL au PATH ou modifiez le chemin dans le script" -ForegroundColor Yellow
    exit 1
}

# Demander le mot de passe
$password = Read-Host "Entrez le mot de passe PostgreSQL (postgres)" -AsSecureString
$passwordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($password)
)
$env:PGPASSWORD = $passwordPlain

Write-Host ""
Write-Host "Verification des colonnes..." -ForegroundColor Yellow

# Vérifier les colonnes
$checkQuery = "SELECT column_name FROM information_schema.columns WHERE table_name = 'flights' AND column_name IN ('cruise_altitude', 'cruise_speed', 'flight_type', 'alternate_airport_id', 'estimated_time_enroute', 'pilot_id') ORDER BY column_name;"

$result = & $psqlExe -U postgres -d flightradar -t -c $checkQuery

$requiredColumns = @('cruise_altitude', 'cruise_speed', 'flight_type', 'alternate_airport_id', 'estimated_time_enroute', 'pilot_id')
$foundColumns = $result | Where-Object { $_.Trim() -ne '' } | ForEach-Object { $_.Trim() }

$missingColumns = $requiredColumns | Where-Object { $foundColumns -notcontains $_ }

if ($missingColumns.Count -gt 0) {
    Write-Host "ERREUR: Colonnes manquantes: $($missingColumns -join ', ')" -ForegroundColor Red
    Write-Host ""
    Write-Host "Execution du script de migration..." -ForegroundColor Yellow
    
    $scriptPath = Join-Path $PSScriptRoot "backend\database\add_flight_fields.sql"
    if (Test-Path $scriptPath) {
        & $psqlExe -U postgres -d flightradar -f $scriptPath
        Write-Host "OK: Script execute" -ForegroundColor Green
    } else {
        Write-Host "ERREUR: Script non trouve: $scriptPath" -ForegroundColor Red
    }
} else {
    Write-Host "OK: Toutes les colonnes existent" -ForegroundColor Green
}

Write-Host ""
Write-Host "Verification de la table activity_logs..." -ForegroundColor Yellow

# Vérifier activity_logs
$checkTableQuery = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'activity_logs');"
$tableExists = & $psqlExe -U postgres -d flightradar -t -c $checkTableQuery

if ($tableExists.Trim() -eq 't') {
    Write-Host "OK: Table activity_logs existe" -ForegroundColor Green
} else {
    Write-Host "ERREUR: Table activity_logs n'existe pas" -ForegroundColor Red
    Write-Host "Execution du script de creation..." -ForegroundColor Yellow
    
    $scriptPath = Join-Path $PSScriptRoot "backend\database\add_activity_logs_table.sql"
    if (Test-Path $scriptPath) {
        & $psqlExe -U postgres -d flightradar -f $scriptPath
        Write-Host "OK: Script execute" -ForegroundColor Green
    } else {
        Write-Host "ERREUR: Script non trouve: $scriptPath" -ForegroundColor Red
    }
}

# Nettoyer
Remove-Item Env:\PGPASSWORD

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  VERIFICATION TERMINEE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Prochaines étapes:" -ForegroundColor Yellow
Write-Host "1. Redémarrer le backend Spring Boot" -ForegroundColor White
Write-Host "2. Tester la création d'un vol" -ForegroundColor White
Write-Host ""

