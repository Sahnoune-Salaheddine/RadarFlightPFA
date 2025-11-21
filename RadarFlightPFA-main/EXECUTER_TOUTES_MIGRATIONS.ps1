# Script complet pour verifier et executer toutes les migrations SQL
# Verifie l'etat actuel et applique les migrations necessaires

$DB_NAME = "flightradar"
$DB_USER = "postgres"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  VERIFICATION ET MIGRATIONS SQL" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Fonction pour executer une commande SQL
function Execute-SQL {
    param(
        [string]$sqlCommand,
        [string]$description
    )
    
    Write-Host "$description..." -ForegroundColor Yellow
    try {
        $result = & psql.exe -U $DB_USER -d $DB_NAME -c $sqlCommand 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "   OK" -ForegroundColor Green
            return $true
        } else {
            Write-Host "   Erreur: $result" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "   Erreur: $_" -ForegroundColor Red
        return $false
    }
}

# Fonction pour executer un fichier SQL
function Execute-SQLFile {
    param(
        [string]$filePath,
        [string]$description
    )
    
    Write-Host "$description..." -ForegroundColor Yellow
    if (-not (Test-Path $filePath)) {
        Write-Host "   ERREUR: Fichier non trouve: $filePath" -ForegroundColor Red
        return $false
    }
    
    try {
        $result = & psql.exe -U $DB_USER -d $DB_NAME -f $filePath 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "   OK: Fichier execute avec succes" -ForegroundColor Green
            return $true
        } else {
            Write-Host "   Erreur lors de l'execution" -ForegroundColor Red
            Write-Host "   Details: $result" -ForegroundColor Yellow
            return $false
        }
    } catch {
        Write-Host "   Erreur: $_" -ForegroundColor Red
        return $false
    }
}

# ETAPE 1: Verifier l'etat actuel
Write-Host "ETAPE 1: Verification de l'etat actuel" -ForegroundColor Cyan
Write-Host ""

# 1.1 Verifier flight_number
Write-Host "1.1 Verification de flight_number..." -ForegroundColor Yellow
$checkFlightNumber = @"
SELECT character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'flights' 
  AND column_name = 'flight_number';
"@

try {
    $result = & psql.exe -U $DB_USER -d $DB_NAME -t -A -c $checkFlightNumber 2>&1
    $length = [int]($result.Trim())
    if ($length -lt 20) {
        Write-Host "   ATTENTION: flight_number est VARCHAR($length), doit etre VARCHAR(20)" -ForegroundColor Yellow
        $needFixFlightNumber = $true
    } else {
        Write-Host "   OK: flight_number est VARCHAR($length)" -ForegroundColor Green
        $needFixFlightNumber = $false
    }
} catch {
    Write-Host "   Erreur lors de la verification" -ForegroundColor Red
    $needFixFlightNumber = $true
}

Write-Host ""

# 1.2 Verifier les nouvelles colonnes
Write-Host "1.2 Verification des nouvelles colonnes..." -ForegroundColor Yellow
$checkColumns = @"
SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'flights' 
  AND column_name IN ('cruise_altitude', 'cruise_speed', 'flight_type', 
                      'alternate_airport_id', 'estimated_time_enroute', 'pilot_id');
"@

try {
    $result = & psql.exe -U $DB_USER -d $DB_NAME -t -A -c $checkColumns 2>&1
    $columns = ($result -split "`n" | Where-Object { $_.Trim() -ne "" }) | ForEach-Object { $_.Trim() }
    $expectedColumns = @('cruise_altitude', 'cruise_speed', 'flight_type', 
                         'alternate_airport_id', 'estimated_time_enroute', 'pilot_id')
    
    $missing = @()
    foreach ($col in $expectedColumns) {
        if ($columns -notcontains $col) {
            $missing += $col
        }
    }
    
    if ($missing.Count -eq 0) {
        Write-Host "   OK: Toutes les colonnes existent" -ForegroundColor Green
        $needAddColumns = $false
    } else {
        Write-Host "   Colonnes manquantes: $($missing -join ', ')" -ForegroundColor Yellow
        $needAddColumns = $true
    }
} catch {
    Write-Host "   Erreur lors de la verification" -ForegroundColor Red
    $needAddColumns = $true
}

Write-Host ""

# 1.3 Verifier la table activity_logs
Write-Host "1.3 Verification de la table activity_logs..." -ForegroundColor Yellow
$checkActivityLogs = @"
SELECT EXISTS (
    SELECT FROM information_schema.tables 
    WHERE table_name = 'activity_logs'
) AS exists;
"@

try {
    $result = & psql.exe -U $DB_USER -d $DB_NAME -t -A -c $checkActivityLogs 2>&1
    if ($result -match "t|true|1") {
        Write-Host "   OK: Table activity_logs existe" -ForegroundColor Green
        $needCreateActivityLogs = $false
    } else {
        Write-Host "   Table activity_logs n'existe pas" -ForegroundColor Yellow
        $needCreateActivityLogs = $true
    }
} catch {
    Write-Host "   Erreur lors de la verification" -ForegroundColor Red
    $needCreateActivityLogs = $true
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ETAPE 2: Executer les migrations necessaires
Write-Host "ETAPE 2: Execution des migrations" -ForegroundColor Cyan
Write-Host ""

$migrationsExecuted = 0

# 2.1 Corriger flight_number si necessaire
if ($needFixFlightNumber) {
    Write-Host "2.1 Correction de flight_number..." -ForegroundColor Yellow
    $sql = "ALTER TABLE flights ALTER COLUMN flight_number TYPE VARCHAR(20);"
    if (Execute-SQL -sqlCommand $sql -description "   Modification de flight_number en VARCHAR(20)") {
        $migrationsExecuted++
    }
    Write-Host ""
}

# 2.2 Ajouter les colonnes manquantes
if ($needAddColumns) {
    $scriptPath = Join-Path $PSScriptRoot "backend\database\add_flight_fields.sql"
    if (Execute-SQLFile -filePath $scriptPath -description "2.2 Ajout des colonnes a la table flights") {
        $migrationsExecuted++
    }
    Write-Host ""
}

# 2.3 Creer la table activity_logs
if ($needCreateActivityLogs) {
    $scriptPath = Join-Path $PSScriptRoot "backend\database\add_activity_logs_table.sql"
    if (Execute-SQLFile -filePath $scriptPath -description "2.3 Creation de la table activity_logs") {
        $migrationsExecuted++
    }
    Write-Host ""
}

# ETAPE 3: Verification finale
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "ETAPE 3: Verification finale" -ForegroundColor Cyan
Write-Host ""

# 3.1 Verifier flight_number
Write-Host "3.1 Verification finale de flight_number..." -ForegroundColor Yellow
try {
    $result = & psql.exe -U $DB_USER -d $DB_NAME -t -A -c $checkFlightNumber 2>&1
    $length = [int]($result.Trim())
    if ($length -ge 20) {
        Write-Host "   OK: flight_number est VARCHAR($length)" -ForegroundColor Green
    } else {
        Write-Host "   ERREUR: flight_number est encore VARCHAR($length)" -ForegroundColor Red
    }
} catch {
    Write-Host "   Erreur lors de la verification" -ForegroundColor Red
}

Write-Host ""

# 3.2 Verifier les colonnes
Write-Host "3.2 Verification finale des colonnes..." -ForegroundColor Yellow
try {
    $result = & psql.exe -U $DB_USER -d $DB_NAME -t -A -c $checkColumns 2>&1
    $columns = ($result -split "`n" | Where-Object { $_.Trim() -ne "" }) | ForEach-Object { $_.Trim() }
    $expectedColumns = @('cruise_altitude', 'cruise_speed', 'flight_type', 
                         'alternate_airport_id', 'estimated_time_enroute', 'pilot_id')
    
    $missing = @()
    foreach ($col in $expectedColumns) {
        if ($columns -notcontains $col) {
            $missing += $col
        }
    }
    
    if ($missing.Count -eq 0) {
        Write-Host "   OK: Toutes les colonnes existent" -ForegroundColor Green
    } else {
        Write-Host "   ERREUR: Colonnes manquantes: $($missing -join ', ')" -ForegroundColor Red
    }
} catch {
    Write-Host "   Erreur lors de la verification" -ForegroundColor Red
}

Write-Host ""

# 3.3 Verifier activity_logs
Write-Host "3.3 Verification finale de activity_logs..." -ForegroundColor Yellow
try {
    $result = & psql.exe -U $DB_USER -d $DB_NAME -t -A -c $checkActivityLogs 2>&1
    if ($result -match "t|true|1") {
        Write-Host "   OK: Table activity_logs existe" -ForegroundColor Green
    } else {
        Write-Host "   ERREUR: Table activity_logs n'existe toujours pas" -ForegroundColor Red
    }
} catch {
    Write-Host "   Erreur lors de la verification" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  MIGRATIONS TERMINEES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Migrations executees: $migrationsExecuted" -ForegroundColor $(if ($migrationsExecuted -gt 0) { "Green" } else { "Yellow" })
Write-Host ""
Write-Host "PROCHAINES ETAPES:" -ForegroundColor Yellow
Write-Host "1. Redemarrer le backend Spring Boot" -ForegroundColor White
Write-Host "2. Tester la creation d'un vol" -ForegroundColor White
Write-Host ""

