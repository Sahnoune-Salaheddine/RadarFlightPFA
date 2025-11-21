# Script PowerShell pour vérifier et assigner un aéroport à un utilisateur CENTRE_RADAR

Write-Host "=== Vérification et Assignation Aéroport - Centre Radar ===" -ForegroundColor Cyan
Write-Host ""

# Configuration PostgreSQL
$dbHost = "localhost"
$dbPort = "5432"
$dbName = "flightradar"
$dbUser = "postgres"
$dbPassword = "postgres"

# Connexion à PostgreSQL
$connectionString = "host=$dbHost port=$dbPort dbname=$dbName user=$dbUser password=$dbPassword"

Write-Host "1. Vérification des utilisateurs CENTRE_RADAR..." -ForegroundColor Yellow

# Vérifier les utilisateurs CENTRE_RADAR
$queryUsers = @"
SELECT id, username, role, airport_id, pilot_id 
FROM users 
WHERE role = 'CENTRE_RADAR';
"@

try {
    $users = psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -t -A -F "|" -c $queryUsers
    
    if ($users) {
        Write-Host "Utilisateurs CENTRE_RADAR trouvés:" -ForegroundColor Green
        $users | ForEach-Object {
            $fields = $_ -split '\|'
            if ($fields.Count -ge 5) {
                $userId = $fields[0].Trim()
                $username = $fields[1].Trim()
                $role = $fields[2].Trim()
                $airportId = $fields[3].Trim()
                $pilotId = $fields[4].Trim()
                
                Write-Host "  - ID: $userId, Username: $username, Airport ID: $airportId" -ForegroundColor White
                
                if ([string]::IsNullOrWhiteSpace($airportId) -or $airportId -eq "") {
                    Write-Host "    ⚠️  Aucun aéroport associé !" -ForegroundColor Red
                }
            }
        }
    } else {
        Write-Host "Aucun utilisateur CENTRE_RADAR trouvé." -ForegroundColor Yellow
    }
} catch {
    Write-Host "Erreur lors de la vérification des utilisateurs: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "2. Liste des aéroports disponibles..." -ForegroundColor Yellow

# Lister les aéroports
$queryAirports = @"
SELECT id, name, code_iata, city 
FROM airports 
ORDER BY id;
"@

try {
    $airports = psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -t -A -F "|" -c $queryAirports
    
    if ($airports) {
        Write-Host "Aéroports disponibles:" -ForegroundColor Green
        $airports | ForEach-Object {
            $fields = $_ -split '\|'
            if ($fields.Count -ge 4) {
                $airportId = $fields[0].Trim()
                $airportName = $fields[1].Trim()
                $codeIATA = $fields[2].Trim()
                $city = $fields[3].Trim()
                
                Write-Host "  - ID: $airportId, Nom: $airportName ($codeIATA), Ville: $city" -ForegroundColor White
            }
        }
    } else {
        Write-Host "Aucun aéroport trouvé." -ForegroundColor Yellow
    }
} catch {
    Write-Host "Erreur lors de la récupération des aéroports: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "3. Assignation d'un aéroport à un utilisateur CENTRE_RADAR" -ForegroundColor Yellow
Write-Host ""

# Demander les informations
$username = Read-Host "Entrez le username de l'utilisateur CENTRE_RADAR"
$airportId = Read-Host "Entrez l'ID de l'aéroport à assigner"

if ([string]::IsNullOrWhiteSpace($username) -or [string]::IsNullOrWhiteSpace($airportId)) {
    Write-Host "Username et Airport ID sont requis !" -ForegroundColor Red
    exit 1
}

# Vérifier que l'utilisateur existe et est CENTRE_RADAR
$queryCheckUser = @"
SELECT id, role FROM users WHERE username = '$username' AND role = 'CENTRE_RADAR';
"@

try {
    $userCheck = psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -t -A -F "|" -c $queryCheckUser
    
    if ([string]::IsNullOrWhiteSpace($userCheck)) {
        Write-Host "Erreur: Utilisateur '$username' non trouvé ou n'est pas CENTRE_RADAR !" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "Utilisateur trouvé. Assignation de l'aéroport $airportId..." -ForegroundColor Green
    
    # Assigner l'aéroport
    $queryAssign = @"
UPDATE users 
SET airport_id = $airportId 
WHERE username = '$username' AND role = 'CENTRE_RADAR';
"@
    
    psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -c $queryAssign | Out-Null
    
    Write-Host "✅ Aéroport assigné avec succès !" -ForegroundColor Green
    Write-Host ""
    Write-Host "L'utilisateur $username peut maintenant accéder au dashboard radar." -ForegroundColor Cyan
    
} catch {
    Write-Host "Erreur lors de l'assignation: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=== Script terminé ===" -ForegroundColor Cyan

