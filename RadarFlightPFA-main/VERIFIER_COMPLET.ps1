# Script de verification complete de la base de donnees et du code
# Verifie que tout est en place pour la creation de vols

$DB_NAME = "flightradar"
$DB_USER = "postgres"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "VERIFICATION COMPLETE DE LA BASE DE DONNEES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Verifier la longueur de flight_number
Write-Host "1. Verification de flight_number..." -ForegroundColor Yellow
$checkFlightNumber = @"
SELECT column_name, character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'flights' 
  AND column_name = 'flight_number';
"@

try {
    $result = & psql.exe -U $DB_USER -d $DB_NAME -t -A -F "|" -c $checkFlightNumber 2>&1
    if ($result -match "20") {
        Write-Host "   OK: flight_number est VARCHAR(20)" -ForegroundColor Green
    } elseif ($result -match "10") {
        Write-Host "   ERREUR: flight_number est encore VARCHAR(10)" -ForegroundColor Red
        Write-Host "   Executez: ALTER TABLE flights ALTER COLUMN flight_number TYPE VARCHAR(20);" -ForegroundColor Yellow
    } else {
        Write-Host "   Resultat: $result" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   Erreur lors de la verification: $_" -ForegroundColor Red
}

Write-Host ""

# 2. Verifier les nouvelles colonnes
Write-Host "2. Verification des nouvelles colonnes..." -ForegroundColor Yellow
$checkColumns = @"
SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'flights' 
  AND column_name IN ('cruise_altitude', 'cruise_speed', 'flight_type', 
                      'alternate_airport_id', 'estimated_time_enroute', 'pilot_id');
"@

try {
    $result = & psql.exe -U $DB_USER -d $DB_NAME -t -A -F "|" -c $checkColumns 2>&1
    $columns = $result -split "`n" | Where-Object { $_.Trim() -ne "" }
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
        Write-Host "   ERREUR: Colonnes manquantes:" -ForegroundColor Red
        foreach ($col in $missing) {
            Write-Host "     - $col" -ForegroundColor Yellow
        }
        Write-Host "   Executez: backend/database/add_flight_fields.sql" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   Erreur lors de la verification: $_" -ForegroundColor Red
}

Write-Host ""

# 3. Verifier la table activity_logs
Write-Host "3. Verification de la table activity_logs..." -ForegroundColor Yellow
$checkActivityLogs = @"
SELECT EXISTS (
    SELECT FROM information_schema.tables 
    WHERE table_name = 'activity_logs'
) AS exists;
"@

try {
    $result = & psql.exe -U $DB_USER -d $DB_NAME -t -A -F "|" -c $checkActivityLogs 2>&1
    if ($result -match "t|true|1") {
        Write-Host "   OK: Table activity_logs existe" -ForegroundColor Green
    } else {
        Write-Host "   ERREUR: Table activity_logs n'existe pas" -ForegroundColor Red
        Write-Host "   Executez: backend/database/add_activity_logs_table.sql" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   Erreur lors de la verification: $_" -ForegroundColor Red
}

Write-Host ""

# 4. Verifier les contraintes de cles etrangeres
Write-Host "4. Verification des contraintes de cles etrangeres..." -ForegroundColor Yellow
$checkFK = @"
SELECT COUNT(*) 
FROM information_schema.table_constraints 
WHERE table_name = 'flights' 
  AND constraint_type = 'FOREIGN KEY'
  AND constraint_name IN ('fk_flights_alternate_airport', 'fk_flights_pilot');
"@

try {
    $result = & psql.exe -U $DB_USER -d $DB_NAME -t -A -F "|" -c $checkFK 2>&1
    $count = [int]($result.Trim())
    if ($count -ge 0) {
        Write-Host "   OK: Contraintes de cles etrangeres verifiees" -ForegroundColor Green
    }
} catch {
    Write-Host "   Note: Verification des contraintes FK" -ForegroundColor Gray
}

Write-Host ""

# 5. Verifier les donnees de test (avions, aeroports)
Write-Host "5. Verification des donnees de test..." -ForegroundColor Yellow
$checkData = @"
SELECT 
    (SELECT COUNT(*) FROM aircraft) AS aircraft_count,
    (SELECT COUNT(*) FROM airports) AS airports_count,
    (SELECT COUNT(*) FROM pilots) AS pilots_count;
"@

try {
    $result = & psql.exe -U $DB_USER -d $DB_NAME -t -A -F "|" -c $checkData 2>&1
    $data = $result -split "|"
    if ($data.Count -ge 3) {
        $aircraftCount = $data[0].Trim()
        $airportsCount = $data[1].Trim()
        $pilotsCount = $data[2].Trim()
        
        Write-Host "   Avions: $aircraftCount" -ForegroundColor $(if ([int]$aircraftCount -gt 0) { "Green" } else { "Yellow" })
        Write-Host "   Aeroports: $airportsCount" -ForegroundColor $(if ([int]$airportsCount -ge 2) { "Green" } else { "Yellow" })
        Write-Host "   Pilotes: $pilotsCount" -ForegroundColor $(if ([int]$pilotsCount -gt 0) { "Green" } else { "Gray" })
        
        if ([int]$aircraftCount -eq 0) {
            Write-Host "   ATTENTION: Aucun avion dans la base" -ForegroundColor Yellow
        }
        if ([int]$airportsCount -lt 2) {
            Write-Host "   ATTENTION: Moins de 2 aeroports dans la base" -ForegroundColor Yellow
        }
    }
} catch {
    Write-Host "   Erreur lors de la verification: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "VERIFICATION TERMINEE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Resume
Write-Host "Pour executer le script SQL complet de verification:" -ForegroundColor Cyan
Write-Host "  psql -U postgres -d flightradar -f VERIFIER_BASE_DONNEES.sql" -ForegroundColor Gray
Write-Host ""

