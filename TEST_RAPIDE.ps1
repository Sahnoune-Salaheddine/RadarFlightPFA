# Test Rapide des APIs
Write-Host "🧪 Test Rapide des APIs" -ForegroundColor Cyan
Write-Host "=======================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$BASE_URL = "http://localhost:8080/api"
$USERNAME = "pilote_cmn1"
$PASSWORD = "pilote123"

# Test 1 : Vérifier que le backend est accessible
Write-Host "1️⃣  Vérification du Backend..." -ForegroundColor Yellow
try {
    $test = Invoke-WebRequest -Uri "$BASE_URL/airports" -Method GET -UseBasicParsing -TimeoutSec 5
    Write-Host "   ✅ Backend accessible" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Backend non accessible - Vérifiez qu'il est démarré" -ForegroundColor Red
    Write-Host "   💡 Exécutez: .\DEMARRER_BACKEND.ps1" -ForegroundColor Yellow
    exit 1
}

# Test 2 : Login
Write-Host "`n2️⃣  Test Login..." -ForegroundColor Yellow
try {
    $loginBody = @{
        username = $USERNAME
        password = $PASSWORD
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri "$BASE_URL/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody
    
    $TOKEN = $loginResponse.token
    
    if ($TOKEN) {
        Write-Host "   ✅ Login réussi" -ForegroundColor Green
        Write-Host "   📝 Token: $($TOKEN.Substring(0, 30))..." -ForegroundColor Gray
    } else {
        Write-Host "   ❌ Token non reçu" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "   ❌ Erreur de login: $_" -ForegroundColor Red
    exit 1
}

# Test 3 : Dashboard
Write-Host "`n3️⃣  Test Dashboard..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $TOKEN"
        "Content-Type" = "application/json"
    }
    
    $dashboard = Invoke-RestMethod -Uri "$BASE_URL/pilots/$USERNAME/dashboard" `
        -Method GET `
        -Headers $headers
    
    Write-Host "   ✅ Dashboard récupéré" -ForegroundColor Green
    Write-Host "   📊 Vol: $($dashboard.flightNumber) | $($dashboard.route)" -ForegroundColor Gray
    Write-Host "   📍 Position: $($dashboard.latitude), $($dashboard.longitude)" -ForegroundColor Gray
    Write-Host "   ✈️  Statut: $($dashboard.flightStatus)" -ForegroundColor Gray
} catch {
    Write-Host "   ❌ Erreur: $_" -ForegroundColor Red
}

# Test 4 : Autorisation
Write-Host "`n4️⃣  Test Autorisation Décollage..." -ForegroundColor Yellow
try {
    # Récupérer l'avion d'abord
    $aircraft = Invoke-RestMethod -Uri "$BASE_URL/aircraft/pilot/$USERNAME" `
        -Method GET `
        -Headers $headers
    
    $aircraftId = $aircraft.id
    
    $clearanceBody = @{
        aircraftId = $aircraftId
    } | ConvertTo-Json
    
    $clearance = Invoke-RestMethod -Uri "$BASE_URL/atc/request-takeoff-clearance" `
        -Method POST `
        -Headers $headers `
        -Body $clearanceBody
    
    $statusColor = switch ($clearance.status) {
        "GRANTED" { "Green" }
        "REFUSED" { "Red" }
        "PENDING" { "Yellow" }
        default { "Gray" }
    }
    
    Write-Host "   ✅ Réponse reçue" -ForegroundColor Green
    Write-Host "   📋 Statut: $($clearance.status)" -ForegroundColor $statusColor
    Write-Host "   💬 Message: $($clearance.message)" -ForegroundColor Gray
} catch {
    Write-Host "   ❌ Erreur: $_" -ForegroundColor Red
}

Write-Host "`n✅ Tests terminés!" -ForegroundColor Green
Write-Host "`n💡 Pour des tests complets, exécutez: .\test_api.ps1" -ForegroundColor Cyan

