# Script PowerShell URGENT pour corriger la table flights
$DB_NAME = "flightradar"
$DB_USER = "postgres"
$SQL_FILE = "backend\database\CORRIGER_FLIGHTS_FORCE.sql"

Write-Host ""
Write-Host "========================================" -ForegroundColor Red
Write-Host "  CORRECTION URGENTE - TABLE FLIGHTS" -ForegroundColor Red
Write-Host "========================================" -ForegroundColor Red
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
    Write-Host "ERREUR: psql non trouve. Utilisez pgAdmin pour executer:" -ForegroundColor Red
    Write-Host "  $SQL_FILE" -ForegroundColor Yellow
    exit 1
}

Write-Host "Execution de la correction..." -ForegroundColor Yellow
Write-Host ""

$result = & $psqlPath -U $DB_USER -d $DB_NAME -f $SQL_FILE 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  CORRECTION TERMINEE !" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Redemarrez maintenant le backend Spring Boot" -ForegroundColor Yellow
    Write-Host "Puis testez la creation d'un vol" -ForegroundColor Yellow
} else {
    Write-Host ""
    Write-Host "ERREUR lors de l'execution" -ForegroundColor Red
    Write-Host $result -ForegroundColor Red
    Write-Host ""
    Write-Host "Alternative: Executer manuellement via pgAdmin" -ForegroundColor Yellow
}

