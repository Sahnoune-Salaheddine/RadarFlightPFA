# =====================================================
# Script PowerShell - Exécution Migration SQL
# =====================================================
# Ce script exécute la migration complète de la table flights
# =====================================================

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  MIGRATION COMPLÈTE - Table flights" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$DB_NAME = "flightradar"
$DB_USER = "postgres"
$DB_HOST = "localhost"
$DB_PORT = "5432"
$MIGRATION_FILE = "backend\database\MIGRATION_COMPLETE_FLIGHTS.sql"

# Verifier que le fichier existe
if (-not (Test-Path $MIGRATION_FILE)) {
    Write-Host "ERREUR: Fichier de migration introuvable: $MIGRATION_FILE" -ForegroundColor Red
    Write-Host "   Verifiez que vous etes dans le bon repertoire." -ForegroundColor Yellow
    exit 1
}

Write-Host "Fichier de migration trouve: $MIGRATION_FILE" -ForegroundColor Green
Write-Host ""

# Demander confirmation
Write-Host "ATTENTION: Cette migration va modifier la structure de la table 'flights'." -ForegroundColor Yellow
Write-Host "   Assurez-vous d'avoir une sauvegarde de votre base de donnees." -ForegroundColor Yellow
Write-Host ""
$confirmation = Read-Host "Voulez-vous continuer? (O/N)"

if ($confirmation -ne "O" -and $confirmation -ne "o" -and $confirmation -ne "Y" -and $confirmation -ne "y") {
    Write-Host "Migration annulee par l'utilisateur." -ForegroundColor Red
    exit 0
}

Write-Host ""
Write-Host "Execution de la migration..." -ForegroundColor Yellow
Write-Host ""

# Exécuter la migration
try {
    $env:PGPASSWORD = Read-Host "Mot de passe PostgreSQL pour l'utilisateur '$DB_USER' (masqué)" -AsSecureString
    $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($env:PGPASSWORD)
    $plainPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
    
    $env:PGPASSWORD = $plainPassword
    
    $result = psql -U $DB_USER -d $DB_NAME -h $DB_HOST -p $DB_PORT -f $MIGRATION_FILE 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "Migration executee avec succes !" -ForegroundColor Green
        Write-Host ""
        Write-Host "Verification de la structure de la table..." -ForegroundColor Yellow
        
        # Vérifier la structure
        $verifyQuery = @"
SELECT 
    'Total colonnes: ' || COUNT(*) as resume,
    COUNT(CASE WHEN column_name = 'airline' THEN 1 END) as has_airline,
    COUNT(CASE WHEN column_name = 'estimated_arrival' THEN 1 END) as has_estimated_arrival,
    COUNT(CASE WHEN column_name = 'cruise_altitude' THEN 1 END) as has_cruise_altitude,
    COUNT(CASE WHEN column_name = 'cruise_speed' THEN 1 END) as has_cruise_speed,
    COUNT(CASE WHEN column_name = 'flight_type' THEN 1 END) as has_flight_type,
    COUNT(CASE WHEN column_name = 'alternate_airport_id' THEN 1 END) as has_alternate_airport_id,
    COUNT(CASE WHEN column_name = 'estimated_time_enroute' THEN 1 END) as has_estimated_time_enroute,
    COUNT(CASE WHEN column_name = 'pilot_id' THEN 1 END) as has_pilot_id
FROM information_schema.columns
WHERE table_name = 'flights';
"@
        
        $verifyResult = psql -U $DB_USER -d $DB_NAME -h $DB_HOST -p $DB_PORT -c $verifyQuery 2>&1
        
        Write-Host ""
        Write-Host "================================================" -ForegroundColor Cyan
        Write-Host "  RESULTAT DE LA VERIFICATION" -ForegroundColor Cyan
        Write-Host "================================================" -ForegroundColor Cyan
        Write-Host $verifyResult
        Write-Host ""
        
        Write-Host "Migration terminee avec succes !" -ForegroundColor Green
        Write-Host ""
        Write-Host "PROCHAINES ETAPES:" -ForegroundColor Yellow
        Write-Host "   1. Redemarrer le backend Spring Boot" -ForegroundColor White
        Write-Host "   2. Rafraichir le frontend (F5)" -ForegroundColor White
        Write-Host "   3. Tester la creation d'un vol" -ForegroundColor White
        Write-Host ""
        
    } else {
        Write-Host ""
        Write-Host "ERREUR lors de l'execution de la migration" -ForegroundColor Red
        Write-Host "   Code de sortie: $LASTEXITCODE" -ForegroundColor Red
        Write-Host ""
        Write-Host "Sortie de psql:" -ForegroundColor Yellow
        Write-Host $result
        Write-Host ""
        Write-Host "Verifiez:" -ForegroundColor Yellow
        Write-Host "   - Que PostgreSQL est demarre" -ForegroundColor White
        Write-Host "   - Que la base de donnees '$DB_NAME' existe" -ForegroundColor White
        Write-Host "   - Que l'utilisateur '$DB_USER' a les permissions necessaires" -ForegroundColor White
        Write-Host ""
        exit 1
    }
    
} catch {
    Write-Host ""
    Write-Host "ERREUR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Verifiez que psql est installe et dans le PATH." -ForegroundColor Yellow
    exit 1
} finally {
    # Nettoyer le mot de passe de l'environnement
    Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue
}

Write-Host ""

