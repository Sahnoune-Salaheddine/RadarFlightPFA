# Script pour FORCER l'execution de toutes les migrations
# Utile si certaines migrations ont echoue partiellement

$DB_NAME = "flightradar"
$DB_USER = "postgres"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  EXECUTION FORCEE DES MIGRATIONS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Chemin des scripts
$script1 = Join-Path $PSScriptRoot "backend\database\add_flight_fields.sql"
$script2 = Join-Path $PSScriptRoot "backend\database\add_activity_logs_table.sql"
$script3 = Join-Path $PSScriptRoot "backend\database\fix_flight_number_length.sql"

# 1. Corriger flight_number
Write-Host "1. Correction de flight_number..." -ForegroundColor Yellow
$sql1 = "ALTER TABLE flights ALTER COLUMN flight_number TYPE VARCHAR(20);"
try {
    & psql.exe -U $DB_USER -d $DB_NAME -c $sql1 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   OK: flight_number corrige" -ForegroundColor Green
    } else {
        Write-Host "   Note: Peut-etre deja corrige" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   Erreur: $_" -ForegroundColor Red
}

Write-Host ""

# 2. Executer add_flight_fields.sql
Write-Host "2. Execution de add_flight_fields.sql..." -ForegroundColor Yellow
if (Test-Path $script1) {
    try {
        & psql.exe -U $DB_USER -d $DB_NAME -f $script1 2>&1 | Out-Null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "   OK: Colonnes ajoutees" -ForegroundColor Green
        } else {
            Write-Host "   Note: Certaines colonnes peuvent deja exister" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "   Erreur: $_" -ForegroundColor Red
    }
} else {
    Write-Host "   ERREUR: Fichier non trouve: $script1" -ForegroundColor Red
}

Write-Host ""

# 3. Executer add_activity_logs_table.sql
Write-Host "3. Execution de add_activity_logs_table.sql..." -ForegroundColor Yellow
if (Test-Path $script2) {
    try {
        & psql.exe -U $DB_USER -d $DB_NAME -f $script2 2>&1 | Out-Null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "   OK: Table activity_logs creee" -ForegroundColor Green
        } else {
            Write-Host "   Note: La table peut deja exister" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "   Erreur: $_" -ForegroundColor Red
    }
} else {
    Write-Host "   ERREUR: Fichier non trouve: $script2" -ForegroundColor Red
}

Write-Host ""

# 4. Verification finale
Write-Host "4. Verification finale..." -ForegroundColor Yellow
$verifySQL = @"
SELECT 
    CASE WHEN EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'flights' AND column_name = 'cruise_altitude'
    ) THEN 'OK' ELSE 'MANQUANT' END AS cruise_altitude,
    CASE WHEN EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'flights' AND column_name = 'cruise_speed'
    ) THEN 'OK' ELSE 'MANQUANT' END AS cruise_speed,
    CASE WHEN EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'flights' AND column_name = 'flight_type'
    ) THEN 'OK' ELSE 'MANQUANT' END AS flight_type,
    CASE WHEN EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'flights' AND column_name = 'pilot_id'
    ) THEN 'OK' ELSE 'MANQUANT' END AS pilot_id;
"@

try {
    $result = & psql.exe -U $DB_USER -d $DB_NAME -t -A -F "|" -c $verifySQL 2>&1
    Write-Host "   Resultat: $result" -ForegroundColor $(if ($result -match "OK") { "Green" } else { "Yellow" })
} catch {
    Write-Host "   Erreur lors de la verification: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  MIGRATIONS FORCEES TERMINEES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Pour voir le diagnostic detaille:" -ForegroundColor Yellow
Write-Host "  psql -U postgres -d flightradar -f DIAGNOSTIC_DETAILLE.sql" -ForegroundColor Gray
Write-Host ""

