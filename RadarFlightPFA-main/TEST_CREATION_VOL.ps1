# Script de Test - Création de Vol
# PowerShell Script pour tester l'endpoint de création de vol
# Nécessite un utilisateur ADMIN

$BASE_URL = "http://localhost:8080/api"
$ADMIN_USERNAME = "admin"  # Modifier si nécessaire
$ADMIN_PASSWORD = "admin123"  # Modifier si nécessaire

Write-Host "Test de Creation de Vol" -ForegroundColor Cyan
Write-Host "============================" -ForegroundColor Cyan
Write-Host ""

# Fonction pour formater JSON
function Format-Json {
    param($json)
    $json | ConvertFrom-Json | ConvertTo-Json -Depth 10
}

# Etape 1 : Login avec ADMIN
Write-Host "1. Connexion en tant qu'ADMIN..." -ForegroundColor Yellow
$loginBody = @{
    username = $ADMIN_USERNAME
    password = $ADMIN_PASSWORD
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$BASE_URL/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody `
        -ErrorAction Stop
    
    $TOKEN = $loginResponse.token
    
    if ($TOKEN) {
        Write-Host "✅ Login réussi!" -ForegroundColor Green
        Write-Host "   Username: $($loginResponse.username)" -ForegroundColor Gray
        Write-Host "   Role: $($loginResponse.role)" -ForegroundColor Gray
        
        if ($loginResponse.role -ne "ADMIN") {
            Write-Host "ATTENTION: L'utilisateur n'est pas ADMIN. La creation de vol necessite le role ADMIN." -ForegroundColor Yellow
        }
    } else {
        Write-Host "❌ Erreur: Token non reçu" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Erreur de connexion: $_" -ForegroundColor Red
    Write-Host "   Vérifiez que le backend est démarré sur http://localhost:8080" -ForegroundColor Yellow
    Write-Host "   Vérifiez les identifiants ADMIN (username: $ADMIN_USERNAME)" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Headers pour les requêtes authentifiées
$headers = @{
    "Authorization" = "Bearer $TOKEN"
    "Content-Type" = "application/json"
}

# Etape 2 : Recuperer les donnees necessaires
Write-Host "2. Recuperation des donnees necessaires..." -ForegroundColor Yellow

# 2.1 : Recuperer les avions
Write-Host "   Recuperation des avions..." -ForegroundColor Gray
try {
    $aircraftResponse = Invoke-RestMethod -Uri "$BASE_URL/aircraft" `
        -Method GET `
        -Headers $headers `
        -ErrorAction Stop
    
    if ($aircraftResponse.Count -gt 0) {
        $AIRCRAFT_ID = $aircraftResponse[0].id
        Write-Host "   Avion trouve: $($aircraftResponse[0].registration) (ID: $AIRCRAFT_ID)" -ForegroundColor Green
    } else {
        Write-Host "   Aucun avion trouve, utilisation de l'ID par defaut: 1" -ForegroundColor Yellow
        $AIRCRAFT_ID = 1
    }
} catch {
    Write-Host "   Erreur lors de la recuperation des avions: $_" -ForegroundColor Yellow
    Write-Host "   Utilisation de l'ID par defaut: 1" -ForegroundColor Yellow
    $AIRCRAFT_ID = 1
}

# 2.2 : Recuperer les aeroports
Write-Host "   Recuperation des aeroports..." -ForegroundColor Gray
try {
    $airportsResponse = Invoke-RestMethod -Uri "$BASE_URL/airports" `
        -Method GET `
        -Headers $headers `
        -ErrorAction Stop
    
    if ($airportsResponse.Count -ge 2) {
        $DEPARTURE_AIRPORT_ID = $airportsResponse[0].id
        $ARRIVAL_AIRPORT_ID = $airportsResponse[1].id
        Write-Host "   Aeroports trouves:" -ForegroundColor Green
        Write-Host "      Depart: $($airportsResponse[0].name) ($($airportsResponse[0].codeIATA)) (ID: $DEPARTURE_AIRPORT_ID)" -ForegroundColor Gray
        Write-Host "      Arrivee: $($airportsResponse[1].name) ($($airportsResponse[1].codeIATA)) (ID: $ARRIVAL_AIRPORT_ID)" -ForegroundColor Gray
    } else {
        Write-Host "   Moins de 2 aeroports trouves, utilisation des IDs par defaut" -ForegroundColor Yellow
        $DEPARTURE_AIRPORT_ID = 1
        $ARRIVAL_AIRPORT_ID = 2
    }
} catch {
    Write-Host "   Erreur lors de la recuperation des aeroports: $_" -ForegroundColor Yellow
    Write-Host "   Utilisation des IDs par defaut: 1 et 2" -ForegroundColor Yellow
    $DEPARTURE_AIRPORT_ID = 1
    $ARRIVAL_AIRPORT_ID = 2
}

# 2.3 : Recuperer les pilotes via les utilisateurs (optionnel)
Write-Host "   Recuperation des pilotes..." -ForegroundColor Gray
try {
    # Essayer de récupérer les utilisateurs et filtrer les pilotes
    $usersResponse = Invoke-RestMethod -Uri "$BASE_URL/auth/users" `
        -Method GET `
        -Headers $headers `
        -ErrorAction Stop
    
    $pilots = $usersResponse | Where-Object { $_.role -eq "PILOTE" -and $_.pilotId -ne $null }
    
    if ($pilots.Count -gt 0) {
        $PILOT_ID = $pilots[0].pilotId
        Write-Host "   Pilote trouve: $($pilots[0].username) (Pilot ID: $PILOT_ID)" -ForegroundColor Green
    } else {
        Write-Host "   Aucun pilote trouve, le vol sera cree sans pilote assigne" -ForegroundColor Yellow
        $PILOT_ID = $null
    }
} catch {
    Write-Host "   Erreur lors de la recuperation des pilotes: $_" -ForegroundColor Yellow
    Write-Host "   Le vol sera cree sans pilote assigne (optionnel)" -ForegroundColor Yellow
    $PILOT_ID = $null
}

Write-Host ""

# Etape 3 : Creer un vol
Write-Host "3. Creation d'un nouveau vol..." -ForegroundColor Yellow

# Generer un numero de vol unique et court (format: TEST + 4 chiffres aleatoires)
$random = Get-Random -Minimum 1000 -Maximum 9999
$FLIGHT_NUMBER = "TEST$random"

# Calculer les dates (depart dans 2 heures, arrivee dans 4 heures)
$scheduledDeparture = (Get-Date).AddHours(2).ToString("yyyy-MM-ddTHH:mm:ss")
$scheduledArrival = (Get-Date).AddHours(4).ToString("yyyy-MM-ddTHH:mm:ss")

# Préparer le body de la requête
$flightData = @{
    flightNumber = $FLIGHT_NUMBER
    airline = "Test Airlines"
    aircraftId = $AIRCRAFT_ID
    departureAirportId = $DEPARTURE_AIRPORT_ID
    arrivalAirportId = $ARRIVAL_AIRPORT_ID
    scheduledDeparture = $scheduledDeparture
    scheduledArrival = $scheduledArrival
    cruiseAltitude = 35000
    cruiseSpeed = 450
    flightType = "COMMERCIAL"
    flightStatus = "PLANIFIE"
}

# Ajouter le pilote si disponible
if ($PILOT_ID -ne $null) {
    $flightData.pilotId = $PILOT_ID
}

$flightBody = $flightData | ConvertTo-Json

Write-Host "   Donnees du vol:" -ForegroundColor Gray
Write-Host "      Numero: $FLIGHT_NUMBER" -ForegroundColor Gray
Write-Host "      Compagnie: $($flightData.airline)" -ForegroundColor Gray
Write-Host "      Avion ID: $AIRCRAFT_ID" -ForegroundColor Gray
Write-Host "      Depart: Aeroport ID $DEPARTURE_AIRPORT_ID a $scheduledDeparture" -ForegroundColor Gray
Write-Host "      Arrivee: Aeroport ID $ARRIVAL_AIRPORT_ID a $scheduledArrival" -ForegroundColor Gray
Write-Host "      Altitude de croisiere: $($flightData.cruiseAltitude) ft" -ForegroundColor Gray
Write-Host "      Vitesse de croisiere: $($flightData.cruiseSpeed) km/h" -ForegroundColor Gray
if ($PILOT_ID) {
    Write-Host "      Pilote ID: $PILOT_ID" -ForegroundColor Gray
}

Write-Host ""

try {
    $createResponse = Invoke-RestMethod -Uri "$BASE_URL/flight/manage" `
        -Method POST `
        -Headers $headers `
        -Body $flightBody `
        -ErrorAction Stop
    
    Write-Host "Vol cree avec succes!" -ForegroundColor Green
    Write-Host ""
    Write-Host "   Details du vol cree:" -ForegroundColor Cyan
    Write-Host "      ID: $($createResponse.flight.id)" -ForegroundColor Gray
    Write-Host "      Numero: $($createResponse.flight.flightNumber)" -ForegroundColor Gray
    Write-Host "      Compagnie: $($createResponse.flight.airline)" -ForegroundColor Gray
    Write-Host "      Statut: $($createResponse.flight.flightStatus)" -ForegroundColor Gray
    Write-Host "      Depart prevu: $($createResponse.flight.scheduledDeparture)" -ForegroundColor Gray
    Write-Host "      Arrivee prevue: $($createResponse.flight.scheduledArrival)" -ForegroundColor Gray
    if ($createResponse.flight.estimatedTimeEnroute) {
        Write-Host "      Temps estime en route: $($createResponse.flight.estimatedTimeEnroute) minutes" -ForegroundColor Gray
    }
    Write-Host ""
    Write-Host "   Message: $($createResponse.message)" -ForegroundColor Green
    
} catch {
    Write-Host "Erreur lors de la creation du vol" -ForegroundColor Red
    Write-Host ""
    
    # Afficher les details de l'erreur
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "   Code de statut: $statusCode" -ForegroundColor Yellow
        
        try {
            $errorStream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorStream)
            $responseBody = $reader.ReadToEnd()
            $errorJson = $responseBody | ConvertFrom-Json
            
            Write-Host "   Type d'erreur: $($errorJson.type)" -ForegroundColor Yellow
            Write-Host "   Message: $($errorJson.error)" -ForegroundColor Yellow
            if ($errorJson.details) {
                Write-Host "   Details: $($errorJson.details)" -ForegroundColor Yellow
            }
        } catch {
            Write-Host "   Reponse brute: $($_.Exception.Message)" -ForegroundColor Yellow
        }
    } else {
        Write-Host "   Erreur: $($_.Exception.Message)" -ForegroundColor Yellow
    }
    
    Write-Host ""
    Write-Host "Verifications a faire:" -ForegroundColor Cyan
    Write-Host "   1. Verifiez que les migrations SQL ont ete executees" -ForegroundColor Gray
    Write-Host "   2. Verifiez que les IDs (avion, aeroports, pilote) existent dans la base" -ForegroundColor Gray
    Write-Host "   3. Verifiez les logs du backend pour plus de details" -ForegroundColor Gray
    Write-Host "   4. Verifiez que le numero de vol n'existe pas deja" -ForegroundColor Gray
}

Write-Host ""
Write-Host "============================" -ForegroundColor Cyan
Write-Host "Test termine!" -ForegroundColor Green
Write-Host ""
Write-Host "Pour voir les logs detailles, consultez la console du backend Spring Boot." -ForegroundColor Gray

