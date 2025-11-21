# Script simple pour corriger la longueur de flight_number
# Execute directement la commande SQL

$DB_NAME = "flightradar"
$DB_USER = "postgres"

Write-Host "Correction de la longueur de flight_number..." -ForegroundColor Cyan
Write-Host "De VARCHAR(10) a VARCHAR(20)" -ForegroundColor Yellow
Write-Host ""

# Commande SQL
$sqlCommand = "ALTER TABLE flights ALTER COLUMN flight_number TYPE VARCHAR(20);"

Write-Host "Execution de la commande SQL..." -ForegroundColor Yellow
Write-Host "Commande: $sqlCommand" -ForegroundColor Gray
Write-Host ""

# Executer avec psql
# L'utilisateur devra entrer le mot de passe manuellement
try {
    & psql.exe -U $DB_USER -d $DB_NAME -c $sqlCommand
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "Succes! La colonne flight_number a ete modifiee en VARCHAR(20)" -ForegroundColor Green
        Write-Host ""
        Write-Host "Vous pouvez maintenant relancer le test de creation de vol." -ForegroundColor Cyan
    } else {
        Write-Host ""
        Write-Host "Erreur lors de l'execution. Code de sortie: $LASTEXITCODE" -ForegroundColor Red
    }
} catch {
    Write-Host "Erreur: $_" -ForegroundColor Red
    Write-Host "Verifiez que psql.exe est dans votre PATH" -ForegroundColor Yellow
}

