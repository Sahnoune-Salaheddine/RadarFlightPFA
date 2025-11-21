# Script de Test API - Flight Radar PFA 2026
# PowerShell Script pour tester les endpoints

$BASE_URL = "http://localhost:8080/api"
$USERNAME = "pilote_cmn1"
$PASSWORD = "pilote123"

Write-Host "🧪 Tests API - Flight Radar PFA 2026" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Fonction pour formater JSON
function Format-Json {
    param($json)
    $json | ConvertFrom-Json | ConvertTo-Json -Depth 10
}

# Test 1 : Login
Write-Host "🔐 1. Test Login..." -ForegroundColor Yellow
$loginBody = @{
    username = $USERNAME
    password = $PASSWORD
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$BASE_URL/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody
    
    $TOKEN = $loginResponse.token
    
    if ($TOKEN) {
        Write-Host "✅ Login réussi!" -ForegroundColor Green
        Write-Host "   Token: $($TOKEN.Substring(0, 20))..." -ForegroundColor Gray
        Write-Host "   Username: $($loginResponse.username)" -ForegroundColor Gray
        Write-Host "   Role: $($loginResponse.role)" -ForegroundColor Gray
    } else {
        Write-Host "❌ Erreur: Token non reçu" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Erreur de connexion: $_" -ForegroundColor Red
    Write-Host "   Vérifiez que le backend est démarré sur http://localhost:8080" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Test 2 : Dashboard Pilote
Write-Host "✈️ 2. Test Dashboard Pilote..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $TOKEN"
        "Content-Type" = "application/json"
    }
    
    $dashboardResponse = Invoke-RestMethod -Uri "$BASE_URL/pilots/$USERNAME/dashboard" `
        -Method GET `
        -Headers $headers
    
    Write-Host "✅ Dashboard récupéré!" -ForegroundColor Green
    Write-Host "   Numéro de vol: $($dashboardResponse.flightNumber)" -ForegroundColor Gray
    Write-Host "   Compagnie: $($dashboardResponse.airline)" -ForegroundColor Gray
    Write-Host "   Type avion: $($dashboardResponse.aircraftType)" -ForegroundColor Gray
    Write-Host "   Route: $($dashboardResponse.route)" -ForegroundColor Gray
    Write-Host "   Statut: $($dashboardResponse.flightStatus)" -ForegroundColor Gray
    Write-Host "   Latitude: $($dashboardResponse.latitude)" -ForegroundColor Gray
    Write-Host "   Longitude: $($dashboardResponse.longitude)" -ForegroundColor Gray
    Write-Host "   Altitude: $($dashboardResponse.altitudeFeet) ft" -ForegroundColor Gray
    Write-Host "   Vitesse sol: $($dashboardResponse.groundSpeed) km/h" -ForegroundColor Gray
    
    if ($dashboardResponse.kpis) {
        Write-Host "   KPIs présents: ✅" -ForegroundColor Green
    }
    
    if ($dashboardResponse.weather) {
        Write-Host "   Météo présente: ✅" -ForegroundColor Green
    }
    
    if ($dashboardResponse.atcHistory) {
        Write-Host "   Historique ATC: $($dashboardResponse.atcHistory.Count) messages" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ Erreur: $_" -ForegroundColor Red
    Write-Host "   Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
}

Write-Host ""

# Test 3 : Récupérer l'avion du pilote
Write-Host "🛩️ 3. Test Récupération Avion..." -ForegroundColor Yellow
try {
    $aircraftResponse = Invoke-RestMethod -Uri "$BASE_URL/aircraft/pilot/$USERNAME" `
        -Method GET `
        -Headers $headers
    
    Write-Host "✅ Avion récupéré!" -ForegroundColor Green
    Write-Host "   Immatriculation: $($aircraftResponse.registration)" -ForegroundColor Gray
    Write-Host "   Modèle: $($aircraftResponse.model)" -ForegroundColor Gray
    Write-Host "   Statut: $($aircraftResponse.status)" -ForegroundColor Gray
    
    $AIRCRAFT_ID = $aircraftResponse.id
    Write-Host "   ID Avion: $AIRCRAFT_ID" -ForegroundColor Gray
} catch {
    Write-Host "❌ Erreur: $_" -ForegroundColor Red
    Write-Host "   Aucun avion assigné à ce pilote" -ForegroundColor Yellow
    $AIRCRAFT_ID = 1  # Utiliser ID par défaut pour les tests suivants
}

Write-Host ""

# Test 4 : Demander Autorisation de Décollage
Write-Host "🛫 4. Test Demande Autorisation Décollage..." -ForegroundColor Yellow
try {
    $clearanceBody = @{
        aircraftId = $AIRCRAFT_ID
    } | ConvertTo-Json
    
    $clearanceResponse = Invoke-RestMethod -Uri "$BASE_URL/atc/request-takeoff-clearance" `
        -Method POST `
        -Headers $headers `
        -Body $clearanceBody
    
    Write-Host "✅ Réponse reçue!" -ForegroundColor Green
    
    $statusColor = switch ($clearanceResponse.status) {
        "GRANTED" { "Green" }
        "REFUSED" { "Red" }
        "PENDING" { "Yellow" }
        default { "Gray" }
    }
    
    Write-Host "   Statut: $($clearanceResponse.status)" -ForegroundColor $statusColor
    Write-Host "   Message: $($clearanceResponse.message)" -ForegroundColor Gray
    if ($clearanceResponse.details) {
        Write-Host "   Détails: $($clearanceResponse.details)" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ Erreur: $_" -ForegroundColor Red
    Write-Host "   Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
}

Write-Host ""

# Test 5 : Vérifier le statut d'autorisation
Write-Host "📊 5. Test Statut Autorisation..." -ForegroundColor Yellow
try {
    $statusResponse = Invoke-RestMethod -Uri "$BASE_URL/atc/clearance-status/$AIRCRAFT_ID" `
        -Method GET `
        -Headers $headers
    
    Write-Host "✅ Statut récupéré!" -ForegroundColor Green
    Write-Host "   Statut: $($statusResponse.status)" -ForegroundColor Gray
    Write-Host "   Message: $($statusResponse.message)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Erreur: $_" -ForegroundColor Red
}

Write-Host ""

# Test 6 : Récupérer les conflits
Write-Host "🚨 6. Test Conflits..." -ForegroundColor Yellow
try {
    $conflictsResponse = Invoke-RestMethod -Uri "$BASE_URL/conflicts" `
        -Method GET `
        -Headers $headers
    
    Write-Host "✅ Conflits récupérés!" -ForegroundColor Green
    Write-Host "   Nombre de conflits: $($conflictsResponse.Count)" -ForegroundColor Gray
} catch {
    Write-Host "⚠️  Aucun conflit ou erreur: $_" -ForegroundColor Yellow
}

Write-Host ""

# Résumé
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "✅ Tests terminés!" -ForegroundColor Green
Write-Host ""
Write-Host "Pour tester manuellement avec curl, utilisez:" -ForegroundColor Cyan
Write-Host "  curl -X GET `"http://localhost:8080/api/pilots/$USERNAME/dashboard`" -H `"Authorization: Bearer $TOKEN`"" -ForegroundColor Gray

