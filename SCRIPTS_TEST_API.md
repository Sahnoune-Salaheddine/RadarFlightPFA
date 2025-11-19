# 📜 Scripts de Test API

## 🔐 1. Obtenir un Token JWT

```bash
# Login pour obtenir le token
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "pilote_cmn1",
    "password": "pilote123"
  }'

# Copier le token de la réponse
# Exemple de réponse :
# {
#   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "username": "pilote_cmn1",
#   "role": "PILOTE"
# }
```

---

## ✈️ 2. Test Dashboard Pilote

```bash
# Remplacer YOUR_TOKEN par le token obtenu
TOKEN="YOUR_TOKEN"

# Test 1 : Récupérer l'avion du pilote
curl -X GET "http://localhost:8080/api/pilots/pilote_cmn1/aircraft" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"

# Test 2 : Récupérer le dashboard complet
curl -X GET "http://localhost:8080/api/pilots/pilote_cmn1/dashboard" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq

# Test 3 : Récupérer l'avion par username (alternative)
curl -X GET "http://localhost:8080/api/aircraft/pilot/pilote_cmn1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

---

## 🛫 3. Test Autorisation Décollage

```bash
TOKEN="YOUR_TOKEN"

# Test 1 : Demander autorisation (avion ID 1)
curl -X POST "http://localhost:8080/api/atc/request-takeoff-clearance" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "aircraftId": 1
  }' | jq

# Test 2 : Vérifier le statut d'autorisation
curl -X GET "http://localhost:8080/api/atc/clearance-status/1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq
```

---

## 🌦️ 4. Test Météo

```bash
TOKEN="YOUR_TOKEN"

# Récupérer la météo d'un aéroport
curl -X GET "http://localhost:8080/api/weather/airport/1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq

# Récupérer les alertes météo
curl -X GET "http://localhost:8080/api/weather/alerts" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq
```

---

## 📡 5. Test Communications ATC

```bash
TOKEN="YOUR_TOKEN"

# Récupérer les messages d'un avion
curl -X GET "http://localhost:8080/api/radar/aircraft/1/messages" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq
```

---

## 🚨 6. Test Conflits

```bash
TOKEN="YOUR_TOKEN"

# Récupérer les conflits détectés
curl -X GET "http://localhost:8080/api/conflicts" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq
```

---

## 📊 7. Test Complet (Script Bash)

Créer un fichier `test_api.sh` :

```bash
#!/bin/bash

# Configuration
BASE_URL="http://localhost:8080/api"
USERNAME="pilote_cmn1"
PASSWORD="pilote123"

echo "🔐 1. Login..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"$USERNAME\", \"password\": \"$PASSWORD\"}")

TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.token')

if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
  echo "❌ Erreur de connexion"
  exit 1
fi

echo "✅ Token obtenu: ${TOKEN:0:20}..."

echo ""
echo "✈️ 2. Test Dashboard Pilote..."
curl -s -X GET "$BASE_URL/pilots/$USERNAME/dashboard" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq '{
    flightNumber,
    airline,
    aircraftType,
    route,
    flightStatus,
    latitude,
    longitude,
    altitude,
    groundSpeed
  }'

echo ""
echo "🛫 3. Test Autorisation Décollage..."
curl -s -X POST "$BASE_URL/atc/request-takeoff-clearance" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"aircraftId": 1}' | jq

echo ""
echo "✅ Tests terminés"
```

**Exécuter :**

```bash
chmod +x test_api.sh
./test_api.sh
```

---

## 🧪 8. Tests avec Postman

### Collection Postman

Créer une collection avec les requêtes suivantes :

1. **Login**
   - Method: POST
   - URL: `http://localhost:8080/api/auth/login`
   - Body (JSON):
     ```json
     {
       "username": "pilote_cmn1",
       "password": "pilote123"
     }
     ```
   - Tests:
     ```javascript
     pm.test("Status 200", function () {
       pm.response.to.have.status(200);
     });
     pm.test("Token présent", function () {
       var jsonData = pm.response.json();
       pm.expect(jsonData.token).to.exist;
       pm.environment.set("token", jsonData.token);
     });
     ```

2. **Dashboard Pilote**
   - Method: GET
   - URL: `http://localhost:8080/api/pilots/{{username}}/dashboard`
   - Headers:
     - `Authorization: Bearer {{token}}`
   - Tests:
     ```javascript
     pm.test("Status 200", function () {
       pm.response.to.have.status(200);
     });
     pm.test("Données complètes", function () {
       var jsonData = pm.response.json();
       pm.expect(jsonData.flightNumber).to.exist;
       pm.expect(jsonData.kpis).to.exist;
     });
     ```

3. **Demander Autorisation**
   - Method: POST
   - URL: `http://localhost:8080/api/atc/request-takeoff-clearance`
   - Headers:
     - `Authorization: Bearer {{token}}`
   - Body (JSON):
     ```json
     {
       "aircraftId": 1
     }
     ```
   - Tests:
     ```javascript
     pm.test("Status 200", function () {
       pm.response.to.have.status(200);
     });
     pm.test("Statut présent", function () {
       var jsonData = pm.response.json();
       pm.expect(jsonData.status).to.be.oneOf(["GRANTED", "REFUSED", "PENDING"]);
     });
     ```

---

## 📝 Notes

- Installer `jq` pour formater les réponses JSON : `brew install jq` (Mac) ou `apt-get install jq` (Linux)
- Remplacer `YOUR_TOKEN` par le token obtenu lors du login
- Remplacer `pilote_cmn1` par le username du pilote à tester
- Remplacer `1` par l'ID de l'avion à tester

