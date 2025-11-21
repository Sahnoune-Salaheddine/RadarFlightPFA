# Script PowerShell pour executer la correction complete de la table flights
$DB_NAME = "flightradar"
$DB_USER = "postgres"
$SQL_FILE = "backend\database\VERIFIER_ET_CORRIGER_FLIGHTS.sql"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  CORRECTION COMPLETE - TABLE FLIGHTS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Ce script va:" -ForegroundColor Yellow
Write-Host "  1. Verifier la structure actuelle de la table flights" -ForegroundColor Gray
Write-Host "  2. Ajouter la colonne 'airline' si absente" -ForegroundColor Gray
Write-Host "  3. Ajouter toutes les colonnes de migration si absentes" -ForegroundColor Gray
Write-Host "  4. Corriger la longueur de flight_number (VARCHAR(20))" -ForegroundColor Gray
Write-Host "  5. Ajouter les contraintes de cles etrangeres" -ForegroundColor Gray
Write-Host "  6. Creer les index necessaires" -ForegroundColor Gray
Write-Host "  7. Verifier et corriger la contrainte CHECK sur flight_status" -ForegroundColor Gray
Write-Host ""

if (-not (Test-Path $SQL_FILE)) {
    Write-Host "ERREUR: Fichier SQL non trouve: $SQL_FILE" -ForegroundColor Red
    Write-Host ""
    Write-Host "Verifiez que vous etes dans le repertoire racine du projet" -ForegroundColor Yellow
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
        Write-Host ""
        Write-Host "Solutions:" -ForegroundColor Yellow
        Write-Host "  1. Ajouter PostgreSQL au PATH" -ForegroundColor Gray
        Write-Host "  2. Utiliser pgAdmin pour executer le script manuellement" -ForegroundColor Gray
        Write-Host "  3. Specifier le chemin complet vers psql.exe" -ForegroundColor Gray
        exit 1
    }
}

Write-Host ""
Write-Host "Informations de connexion:" -ForegroundColor Cyan
Write-Host "   Base de donnees: $DB_NAME" -ForegroundColor Gray
Write-Host "   Utilisateur: $DB_USER" -ForegroundColor Gray
Write-Host "   Fichier SQL: $SQL_FILE" -ForegroundColor Gray
Write-Host ""

$confirm = Read-Host "Voulez-vous executer la correction ? (O/N)"
if ($confirm -ne "O" -and $confirm -ne "o" -and $confirm -ne "Y" -and $confirm -ne "y") {
    Write-Host "Correction annulee" -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "Execution de la correction..." -ForegroundColor Yellow
Write-Host ""

try {
    $result = & $psqlPath -U $DB_USER -d $DB_NAME -f $SQL_FILE 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "  CORRECTION TERMINEE AVEC SUCCES !" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "Colonnes verifiees/ajoutees:" -ForegroundColor Cyan
        Write-Host "  - airline (VARCHAR(100))" -ForegroundColor Gray
        Write-Host "  - estimated_arrival (TIMESTAMP)" -ForegroundColor Gray
        Write-Host "  - cruise_altitude (INTEGER)" -ForegroundColor Gray
        Write-Host "  - cruise_speed (INTEGER)" -ForegroundColor Gray
        Write-Host "  - flight_type (VARCHAR(20))" -ForegroundColor Gray
        Write-Host "  - alternate_airport_id (BIGINT)" -ForegroundColor Gray
        Write-Host "  - estimated_time_enroute (INTEGER)" -ForegroundColor Gray
        Write-Host "  - pilot_id (BIGINT)" -ForegroundColor Gray
        Write-Host ""
        Write-Host "Corrections appliquees:" -ForegroundColor Cyan
        Write-Host "  - Mapping JPA corrige (flight_status explicite)" -ForegroundColor Gray
        Write-Host "  - Longueur flight_number corrigee (VARCHAR(20))" -ForegroundColor Gray
        Write-Host "  - Contraintes de cles etrangeres ajoutees" -ForegroundColor Gray
        Write-Host "  - Index crees pour les performances" -ForegroundColor Gray
        Write-Host ""
        Write-Host "Prochaines etapes:" -ForegroundColor Yellow
        Write-Host "  1. Redemarrer le backend Spring Boot" -ForegroundColor Gray
        Write-Host "  2. Rafraichir le frontend" -ForegroundColor Gray
        Write-Host "  3. Essayer de creer un vol a nouveau" -ForegroundColor Gray
        Write-Host ""
        Write-Host "Si l'erreur persiste, consultez:" -ForegroundColor Yellow
        Write-Host "  - ANALYSE_COMPLETE_ERREUR_CREATION_VOL.md" -ForegroundColor Gray
        Write-Host "  - Les logs du backend Spring Boot" -ForegroundColor Gray
        Write-Host "  - Les logs PostgreSQL" -ForegroundColor Gray
    } else {
        Write-Host ""
        Write-Host "ERREUR lors de l'execution de la correction" -ForegroundColor Red
        Write-Host ""
        Write-Host "Details:" -ForegroundColor Yellow
        Write-Host $result -ForegroundColor Red
        Write-Host ""
        Write-Host "Verifiez:" -ForegroundColor Yellow
        Write-Host "  - Que PostgreSQL est demarre" -ForegroundColor Gray
        Write-Host "  - Que la base de donnees '$DB_NAME' existe" -ForegroundColor Gray
        Write-Host "  - Que l'utilisateur '$DB_USER' a les permissions" -ForegroundColor Gray
        Write-Host "  - Le mot de passe PostgreSQL (generalement 'postgres')" -ForegroundColor Gray
        Write-Host ""
        Write-Host "Alternative: Executer le script manuellement via pgAdmin" -ForegroundColor Yellow
        exit 1
    }
} catch {
    Write-Host ""
    Write-Host "ERREUR lors de l'execution: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Script termine !" -ForegroundColor Green

