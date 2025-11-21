# Script PowerShell simple pour exécuter les migrations SQL
# Usage: .\EXECUTER_MIGRATIONS_SIMPLE.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  EXECUTION DES MIGRATIONS SQL" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Chemin du projet
$projectPath = $PSScriptRoot
$dbPath = Join-Path $projectPath "backend\database"

# Chemin PostgreSQL
$postgresPath = "C:\Program Files\PostgreSQL\16\bin"
if (-not (Test-Path $postgresPath)) {
    $postgresPath = "C:\Program Files\PostgreSQL\15\bin"
    if (-not (Test-Path $postgresPath)) {
        Write-Host "ERREUR: PostgreSQL non trouve" -ForegroundColor Red
        Write-Host "Modifiez le chemin dans le script ou ajoutez PostgreSQL au PATH" -ForegroundColor Yellow
        exit 1
    }
}

$psqlExe = Join-Path $postgresPath "psql.exe"

if (-not (Test-Path $psqlExe)) {
    Write-Host "ERREUR: psql.exe non trouve" -ForegroundColor Red
    exit 1
}

Write-Host "PostgreSQL trouve: $psqlExe" -ForegroundColor Green
Write-Host ""

# Demander le mot de passe
$password = Read-Host "Entrez le mot de passe PostgreSQL (postgres)" -AsSecureString
$passwordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($password)
)
$env:PGPASSWORD = $passwordPlain

# Scripts à exécuter
$script1 = Join-Path $dbPath "add_flight_fields.sql"
$script2 = Join-Path $dbPath "add_activity_logs_table.sql"

Write-Host "Execution du script 1: Ajout des colonnes a la table flights" -ForegroundColor Yellow
if (Test-Path $script1) {
    & $psqlExe -U postgres -d flightradar -f $script1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "OK: Script 1 execute avec succes" -ForegroundColor Green
    } else {
        Write-Host "ERREUR lors de l'execution du script 1" -ForegroundColor Red
    }
} else {
    Write-Host "ERREUR: Script non trouve: $script1" -ForegroundColor Red
}

Write-Host ""
Write-Host "Execution du script 2: Creation de la table activity_logs" -ForegroundColor Yellow
if (Test-Path $script2) {
    & $psqlExe -U postgres -d flightradar -f $script2
    if ($LASTEXITCODE -eq 0) {
        Write-Host "OK: Script 2 execute avec succes" -ForegroundColor Green
    } else {
        Write-Host "ERREUR lors de l'execution du script 2" -ForegroundColor Red
    }
} else {
    Write-Host "ERREUR: Script non trouve: $script2" -ForegroundColor Red
}

# Nettoyer
Remove-Item Env:\PGPASSWORD

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  MIGRATIONS TERMINEES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Prochaines etapes:" -ForegroundColor Yellow
Write-Host "1. Redemarrer le backend Spring Boot" -ForegroundColor White
Write-Host "2. Tester la creation d'un vol depuis l'interface Admin" -ForegroundColor White
Write-Host ""

