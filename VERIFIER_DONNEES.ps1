# Script pour verifier l'etat de la base de donnees Flight Radar
# Verifie que toutes les donnees sont bien initialisees

Write-Host "Verification de la base de donnees Flight Radar" -ForegroundColor Cyan
Write-Host ""

# Ajouter PostgreSQL au PATH
$env:Path += ";C:\Program Files\PostgreSQL\16\bin"
$env:PGPASSWORD = "postgres"

Write-Host "Statistiques de la base de donnees..." -ForegroundColor Yellow
Write-Host ""

# Verifier les aeroports
Write-Host "AEROPORTS" -ForegroundColor Green
$airports = psql -U postgres -d flightradar -t -c "SELECT COUNT(*) FROM airports;"
Write-Host "   Nombre d'aeroports : $($airports.Trim())" -ForegroundColor White

if ([int]$airports.Trim() -gt 0) {
    Write-Host "   Liste des aeroports :" -ForegroundColor Yellow
    psql -U postgres -d flightradar -c "SELECT id, name, city, code_iata FROM airports ORDER BY id;"
}
Write-Host ""

# Verifier les avions
Write-Host "AVIONS" -ForegroundColor Green
$aircraft = psql -U postgres -d flightradar -t -c "SELECT COUNT(*) FROM aircraft;"
Write-Host "   Nombre d'avions : $($aircraft.Trim())" -ForegroundColor White

if ([int]$aircraft.Trim() -gt 0) {
    Write-Host "   Liste des avions :" -ForegroundColor Yellow
    psql -U postgres -d flightradar -c "SELECT id, registration, model, status FROM aircraft ORDER BY id;"
}
Write-Host ""

# Verifier les pilotes
Write-Host "PILOTES" -ForegroundColor Green
$pilots = psql -U postgres -d flightradar -t -c "SELECT COUNT(*) FROM pilots;"
Write-Host "   Nombre de pilotes : $($pilots.Trim())" -ForegroundColor White
Write-Host ""

# Verifier les centres radar
Write-Host "CENTRES RADAR" -ForegroundColor Green
$radars = psql -U postgres -d flightradar -t -c "SELECT COUNT(*) FROM radar_centers;"
Write-Host "   Nombre de centres radar : $($radars.Trim())" -ForegroundColor White
Write-Host ""

# Verifier les utilisateurs
Write-Host "UTILISATEURS" -ForegroundColor Green
$users = psql -U postgres -d flightradar -t -c "SELECT COUNT(*) FROM users;"
Write-Host "   Nombre d'utilisateurs : $($users.Trim())" -ForegroundColor White

if ([int]$users.Trim() -gt 0) {
    Write-Host "   Liste des utilisateurs :" -ForegroundColor Yellow
    psql -U postgres -d flightradar -c "SELECT id, username, role FROM users ORDER BY id;"
}
Write-Host ""

# Verifier les pistes
Write-Host "PISTES" -ForegroundColor Green
$runways = psql -U postgres -d flightradar -t -c "SELECT COUNT(*) FROM runways;"
Write-Host "   Nombre de pistes : $($runways.Trim())" -ForegroundColor White
Write-Host ""

# Verifier les donnees meteo
Write-Host "DONNEES METEO" -ForegroundColor Green
$weather = psql -U postgres -d flightradar -t -c "SELECT COUNT(*) FROM weather_data;"
Write-Host "   Nombre d'enregistrements meteo : $($weather.Trim())" -ForegroundColor White
Write-Host ""

# Resume
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "RESUME" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan

$expectedAirports = 4
$expectedAircraft = 8
$expectedPilots = 8
$expectedRadars = 4
$expectedUsers = 13

$allOk = $true

if ([int]$airports.Trim() -eq $expectedAirports) {
    Write-Host "OK Aeroports : $expectedAirports" -ForegroundColor Green
} else {
    Write-Host "ERREUR Aeroports : $($airports.Trim())/$expectedAirports" -ForegroundColor Red
    $allOk = $false
}

if ([int]$aircraft.Trim() -eq $expectedAircraft) {
    Write-Host "OK Avions : $expectedAircraft" -ForegroundColor Green
} else {
    Write-Host "ERREUR Avions : $($aircraft.Trim())/$expectedAircraft" -ForegroundColor Red
    $allOk = $false
}

if ([int]$pilots.Trim() -eq $expectedPilots) {
    Write-Host "OK Pilotes : $expectedPilots" -ForegroundColor Green
} else {
    Write-Host "ERREUR Pilotes : $($pilots.Trim())/$expectedPilots" -ForegroundColor Red
    $allOk = $false
}

if ([int]$radars.Trim() -eq $expectedRadars) {
    Write-Host "OK Centres radar : $expectedRadars" -ForegroundColor Green
} else {
    Write-Host "ERREUR Centres radar : $($radars.Trim())/$expectedRadars" -ForegroundColor Red
    $allOk = $false
}

Write-Host ""

if ($allOk) {
    Write-Host "Toutes les donnees sont correctement initialisees !" -ForegroundColor Green
    Write-Host ""
    Write-Host "L'application est prete a etre utilisee" -ForegroundColor Cyan
} else {
    Write-Host "Certaines donnees manquent" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Solution : Redemarrer l'application Spring Boot" -ForegroundColor Yellow
    Write-Host "   Le DataInitializer initialisera automatiquement les donnees" -ForegroundColor Yellow
}

Write-Host ""
Remove-Item Env:\PGPASSWORD
