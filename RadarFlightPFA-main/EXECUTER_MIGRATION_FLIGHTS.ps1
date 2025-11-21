# Script PowerShell pour executer la migration SQL des colonnes flights
$DB_NAME = "flightradar"
$DB_USER = "postgres"
$SQL_FILE = "backend\database\add_flight_fields.sql"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  MIGRATION SQL - TABLE FLIGHTS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if (-not (Test-Path $SQL_FILE)) {
    Write-Host "ERREUR: Fichier SQL non trouve: $SQL_FILE" -ForegroundColor Red
    exit 1
}

Write-Host "Fichier SQL trouve: $SQL_FILE" -ForegroundColor Green
Write-Host ""

$psqlPath = $null
if (Get-Command psql -ErrorAction SilentlyContinue) {
    $psqlPath = "psql"
    Write-Host "psql trouve dans le PATH" -ForegroundColor Green
} else {
    $possiblePaths = @(
        "C:\Program Files\PostgreSQL\16\bin\psql.exe",
        "C:\Program Files\PostgreSQL\15\bin\psql.exe",
        "C:\Program Files\PostgreSQL\14\bin\psql.exe"
    )
    
    foreach ($path in $possiblePaths) {
        if (Test-Path $path) {
            $psqlPath = $path
            Write-Host "psql trouve: $path" -ForegroundColor Green
            break
        }
    }
    
    if (-not $psqlPath) {
        Write-Host "ERREUR: psql non trouve" -ForegroundColor Red
        Write-Host "Utilisez pgAdmin pour executer le script manuellement" -ForegroundColor Yellow
        exit 1
    }
}

Write-Host ""
Write-Host "Informations de connexion:" -ForegroundColor Cyan
Write-Host "   Base de donnees: $DB_NAME" -ForegroundColor Gray
Write-Host "   Utilisateur: $DB_USER" -ForegroundColor Gray
Write-Host ""

$confirm = Read-Host "Voulez-vous executer la migration ? (O/N)"
if ($confirm -ne "O" -and $confirm -ne "o" -and $confirm -ne "Y" -and $confirm -ne "y") {
    Write-Host "Migration annulee" -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "Execution de la migration..." -ForegroundColor Yellow
Write-Host ""

try {
    $result = & $psqlPath -U $DB_USER -d $DB_NAME -f $SQL_FILE 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "Migration executee avec succes !" -ForegroundColor Green
        Write-Host ""
        Write-Host "Colonnes ajoutees:" -ForegroundColor Cyan
        Write-Host "   - estimated_arrival" -ForegroundColor Gray
        Write-Host "   - cruise_altitude" -ForegroundColor Gray
        Write-Host "   - cruise_speed" -ForegroundColor Gray
        Write-Host "   - flight_type" -ForegroundColor Gray
        Write-Host "   - alternate_airport_id" -ForegroundColor Gray
        Write-Host "   - estimated_time_enroute" -ForegroundColor Gray
        Write-Host "   - pilot_id" -ForegroundColor Gray
        Write-Host ""
        Write-Host "Prochaines etapes:" -ForegroundColor Yellow
        Write-Host "   1. Redemarrer le backend Spring Boot" -ForegroundColor Gray
        Write-Host "   2. Rafraichir le frontend" -ForegroundColor Gray
        Write-Host "   3. Essayer de creer un vol a nouveau" -ForegroundColor Gray
    } else {
        Write-Host ""
        Write-Host "Erreur lors de l'execution de la migration" -ForegroundColor Red
        Write-Host ""
        Write-Host "Details:" -ForegroundColor Yellow
        Write-Host $result -ForegroundColor Red
        Write-Host ""
        Write-Host "Verifiez:" -ForegroundColor Yellow
        Write-Host "   - Que PostgreSQL est demarre" -ForegroundColor Gray
        Write-Host "   - Que la base de donnees '$DB_NAME' existe" -ForegroundColor Gray
        Write-Host "   - Que l'utilisateur '$DB_USER' a les permissions" -ForegroundColor Gray
        Write-Host "   - Le mot de passe PostgreSQL (generalement 'postgres')" -ForegroundColor Gray
        exit 1
    }
} catch {
    Write-Host ""
    Write-Host "Erreur lors de l'execution: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Script termine !" -ForegroundColor Green
