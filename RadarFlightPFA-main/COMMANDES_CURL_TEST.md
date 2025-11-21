# 📜 Commandes cURL pour Tester les APIs

## 🔐 Étape 1 : Obtenir un Token JWT

```powershell
# Login pour obtenir le token
curl -X POST "http://localhost:8080/api/auth/login" `
  -H "Content-Type: application/json" `
  -d '{\"username\": \"pilote_cmn1\", \"password\": \"pilote123\"}'
```

**Réponse attendue :**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "pilote_cmn1",
  "role": "PILOTE"
}
```

**💡 Astuce :** Copiez le token de la réponse pour les requêtes suivantes.

---

## ✈️ Étape 2 : Tester le Dashboard Pilote

### Test 2.1 : Récupérer le Dashboard Complet

```powershell
# Remplacez YOUR_TOKEN par le token obtenu
$TOKEN = "YOUR_TOKEN"

curl -X GET "http://localhost:8080/api/pilots/pilote_cmn1/dashboard" `
  -H "Authorization: Bearer $TOKEN" `
  -H "Content-Type: application/json"
```

**Vérifications :**
- ✅ Status 200 OK
- ✅ Contient `flightNumber`, `airline`, `aircraftType`
- ✅ Contient `latitude`, `longitude`, `altitude`
- ✅ Contient `weather` object
- ✅ Contient `kpis` object
- ✅ Contient `atcHistory` array

### Test 2.2 : Récupérer l'Avion du Pilote

```powershell
curl -X GET "http://localhost:8080/api/aircraft/pilot/pilote_cmn1" `
  -H "Authorization: Bearer $TOKEN" `
  -H "Content-Type: application/json"
```

**Vérifications :**
- ✅ Status 200 OK
- ✅ Retourne les données de l'avion
- ✅ Contient `registration`, `model`, `status`

---

## 🛫 Étape 3 : Tester l'Autorisation de Décollage

### Test 3.1 : Demander Autorisation

```powershell
# Remplacez 1 par l'ID de votre avion
curl -X POST "http://localhost:8080/api/atc/request-takeoff-clearance" `
  -H "Authorization: Bearer $TOKEN" `
  -H "Content-Type: application/json" `
  -d '{\"aircraftId\": 1}'
```

**Réponses possibles :**

**✅ Autorisation Accordée (GRANTED) :**
```json
{
  "status": "GRANTED",
  "message": "Autorisation de décollage accordée",
  "details": "Toutes les conditions sont remplies. Vous pouvez décoller.",
  "timestamp": "2026-01-15T10:30:00"
}
```

**❌ Autorisation Refusée (REFUSED) :**
```json
{
  "status": "REFUSED",
  "message": "Conditions météo défavorables",
  "details": "Visibilité insuffisante: 0.40 km (minimum requis: 0.55 km)",
  "timestamp": "2026-01-15T10:30:00"
}
```

**⏳ En Attente (PENDING) :**
```json
{
  "status": "PENDING",
  "message": "Piste occupée. Veuillez patienter.",
  "details": "Un autre avion est en train de décoller ou d'atterrir.",
  "timestamp": "2026-01-15T10:30:00"
}
```

### Test 3.2 : Vérifier le Statut d'Autorisation

```powershell
curl -X GET "http://localhost:8080/api/atc/clearance-status/1" `
  -H "Authorization: Bearer $TOKEN" `
  -H "Content-Type: application/json"
```

---

## 🌦️ Étape 4 : Tester la Météo

```powershell
# Récupérer la météo d'un aéroport (ID 1 = Casablanca)
curl -X GET "http://localhost:8080/api/weather/airport/1" `
  -H "Authorization: Bearer $TOKEN" `
  -H "Content-Type: application/json"

# Récupérer les alertes météo
curl -X GET "http://localhost:8080/api/weather/alerts" `
  -H "Authorization: Bearer $TOKEN" `
  -H "Content-Type: application/json"
```

---

## 📡 Étape 5 : Tester les Communications ATC

```powershell
# Récupérer les messages d'un avion (ID 1)
curl -X GET "http://localhost:8080/api/radar/aircraft/1/messages" `
  -H "Authorization: Bearer $TOKEN" `
  -H "Content-Type: application/json"
```

---

## 🚨 Étape 6 : Tester les Conflits

```powershell
# Récupérer les conflits détectés
curl -X GET "http://localhost:8080/api/conflicts" `
  -H "Authorization: Bearer $TOKEN" `
  -H "Content-Type: application/json"
```

---

## 📋 Script PowerShell Complet

Créez un fichier `test_complet.ps1` :

```powershell
# Configuration
$BASE_URL = "http://localhost:8080/api"
$USERNAME = "pilote_cmn1"
$PASSWORD = "pilote123"

Write-Host "🧪 Tests API - Flight Radar" -ForegroundColor Cyan

# 1. Login
Write-Host "`n🔐 1. Login..." -ForegroundColor Yellow
$loginResponse = curl -X POST "$BASE_URL/auth/login" `
  -H "Content-Type: application/json" `
  -d "{\"username\": \"$USERNAME\", \"password\": \"$PASSWORD\"}"

$TOKEN = ($loginResponse | ConvertFrom-Json).token
Write-Host "✅ Token obtenu: $($TOKEN.Substring(0, 20))..." -ForegroundColor Green

# 2. Dashboard
Write-Host "`n✈️ 2. Dashboard..." -ForegroundColor Yellow
curl -X GET "$BASE_URL/pilots/$USERNAME/dashboard" `
  -H "Authorization: Bearer $TOKEN" `
  -H "Content-Type: application/json"

# 3. Autorisation
Write-Host "`n🛫 3. Autorisation Décollage..." -ForegroundColor Yellow
curl -X POST "$BASE_URL/atc/request-takeoff-clearance" `
  -H "Authorization: Bearer $TOKEN" `
  -H "Content-Type: application/json" `
  -d '{\"aircraftId\": 1}'

Write-Host "`n✅ Tests terminés!" -ForegroundColor Green
```

**Exécuter :**
```powershell
.\test_complet.ps1
```

---

## 🔍 Vérifications Rapides

### Vérifier que le Backend est Démarré

```powershell
# Test simple
curl http://localhost:8080/api/airports

# Devrait retourner la liste des aéroports
```

### Vérifier que le Frontend est Démarré

```powershell
# Ouvrir dans le navigateur
Start-Process "http://localhost:3000"
# ou
Start-Process "http://localhost:3001"
```

---

## 🐛 Dépannage

### Erreur : "Connection refused"

**Solution :**
```powershell
# Vérifier que le backend est démarré
netstat -ano | findstr :8080

# Si rien n'apparaît, démarrer le backend :
cd backend
mvn spring-boot:run
```

### Erreur : "401 Unauthorized"

**Solution :**
- Vérifier que le token est correct
- Vérifier que le token n'a pas expiré (durée : 24h)
- Se reconnecter pour obtenir un nouveau token

### Erreur : "404 Not Found"

**Solution :**
- Vérifier l'URL de l'endpoint
- Vérifier que l'ID de l'avion existe
- Vérifier que le pilote a un avion assigné

---

## 📊 Formatage JSON (Optionnel)

Pour formater les réponses JSON, utilisez `jq` ou PowerShell :

```powershell
# Avec PowerShell
$response | ConvertFrom-Json | ConvertTo-Json -Depth 10

# Ou rediriger vers un fichier
curl ... > response.json
# Puis ouvrir avec un éditeur JSON
```

---

## ✅ Checklist de Test

- [ ] Backend démarré sur port 8080
- [ ] Frontend démarré sur port 3000 ou 3001
- [ ] Login réussi et token obtenu
- [ ] Dashboard récupéré avec toutes les données
- [ ] Autorisation de décollage testée (GRANTED/REFUSED/PENDING)
- [ ] Météo récupérée
- [ ] Communications ATC récupérées
- [ ] Conflits récupérés

