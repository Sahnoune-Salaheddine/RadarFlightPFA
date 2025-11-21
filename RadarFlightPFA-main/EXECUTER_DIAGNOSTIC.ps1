# Script PowerShell pour executer le diagnostic complet
$DB_NAME = "flightradar"
$DB_USER = "postgres"
$SQL_FILE = "DIAGNOSTIC_COMPLET_ERREUR.sql"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  DIAGNOSTIC COMPLET - ERREUR VOL" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if (-not (Test-Path $SQL_FILE)) {
    Write-Host "ERREUR: Fichier SQL non trouve: $SQL_FILE" -ForegroundColor Red
    exit 1
}

$psqlPath = $null
if (Get-Command psql -ErrorAction SilentlyContinue) {
    $psqlPath = "psql"
} else {
    $possiblePaths = @(
        "C:\Program Files\PostgreSQL\16\bin\psql.exe",
        "C:\Program Files\PostgreSQL\15\bin\psql.exe",
        "C:\Program Files\PostgreSQL\14\bin\psql.exe"
    )
    foreach ($path in $possiblePaths) {
        if (Test-Path $path) {
            $psqlPath = $path
            break
        }
    }
}

if (-not $psqlPath) {
    Write-Host "ERREUR: psql non trouve" -ForegroundColor Red
    Write-Host "Utilisez pgAdmin pour executer: $SQL_FILE" -ForegroundColor Yellow
    exit 1
}

Write-Host "Execution du diagnostic..." -ForegroundColor Yellow
Write-Host ""

$result = & $psqlPath -U $DB_USER -d $DB_NAME -f $SQL_FILE 2>&1

Write-Host $result

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  DIAGNOSTIC TERMINE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Analysez les resultats ci-dessus pour identifier:" -ForegroundColor Yellow
Write-Host "  - Les colonnes manquantes" -ForegroundColor Gray
Write-Host "  - Les contraintes incorrectes" -ForegroundColor Gray
Write-Host "  - Les donnees de test manquantes" -ForegroundColor Gray
Write-Host ""
Write-Host "Pour corriger, executez ensuite:" -ForegroundColor Yellow
Write-Host "  .\CORRIGER_FLIGHTS_MAINTENANT.ps1" -ForegroundColor Cyan

