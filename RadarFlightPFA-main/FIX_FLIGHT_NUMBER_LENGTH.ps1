# Script pour corriger la longueur de la colonne flight_number
# De VARCHAR(10) a VARCHAR(20)

$DB_NAME = "flightradar"
$DB_USER = "postgres"
$DB_HOST = "localhost"
$DB_PORT = "5432"

Write-Host "Correction de la longueur de flight_number..." -ForegroundColor Cyan
Write-Host ""

# Chemin vers psql (ajustez si necessaire)
$PSQL_PATH = "psql.exe"

# Verifier si psql existe
if (-not (Get-Command $PSQL_PATH -ErrorAction SilentlyContinue)) {
    Write-Host "Erreur: psql.exe non trouve dans le PATH" -ForegroundColor Red
    Write-Host "Veuillez installer PostgreSQL ou ajouter psql.exe au PATH" -ForegroundColor Yellow
    exit 1
}

# Executer le script SQL
Write-Host "Execution du script SQL..." -ForegroundColor Yellow

$sqlScript = @"
ALTER TABLE flights 
ALTER COLUMN flight_number TYPE VARCHAR(20);
"@

try {
    $env:PGPASSWORD = Read-Host "Mot de passe PostgreSQL (ou appuyez sur Entree si aucun)" -AsSecureString
    $password = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($env:PGPASSWORD))
    
    if ([string]::IsNullOrEmpty($password)) {
        $result = & $PSQL_PATH -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c $sqlScript 2>&1
    } else {
        $env:PGPASSWORD = $password
        $result = & $PSQL_PATH -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c $sqlScript 2>&1
        Remove-Item Env:\PGPASSWORD
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Succes! La colonne flight_number a ete modifiee en VARCHAR(20)" -ForegroundColor Green
    } else {
        Write-Host "Erreur lors de l'execution:" -ForegroundColor Red
        Write-Host $result -ForegroundColor Yellow
    }
} catch {
    Write-Host "Erreur: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "Verification..." -ForegroundColor Cyan

$verifySql = @"
SELECT column_name, character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'flights' 
  AND column_name = 'flight_number';
"@

try {
    if ([string]::IsNullOrEmpty($password)) {
        $verifyResult = & $PSQL_PATH -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c $verifySql 2>&1
    } else {
        $env:PGPASSWORD = $password
        $verifyResult = & $PSQL_PATH -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c $verifySql 2>&1
        Remove-Item Env:\PGPASSWORD
    }
    
    Write-Host $verifyResult
} catch {
    Write-Host "Erreur lors de la verification: $_" -ForegroundColor Red
}

