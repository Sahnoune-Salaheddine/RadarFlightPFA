# 🧪 Tests Postman - Intégration OpenSky Network

## 📋 COLLECTION POSTMAN

### Variables d'environnement

Créer une collection Postman avec les variables suivantes :
- `baseUrl` : `http://localhost:8080`
- `apiBase` : `{{baseUrl}}/api`

---

## 🚀 REQUÊTES POSTMAN

### 1. GET /api/aircraft/live

**Nom** : Get All Live Aircraft  
**Méthode** : GET  
**URL** : `{{apiBase}}/aircraft/live`  
**Headers** : Aucun

**Description** : Récupère tous les avions en temps réel depuis OpenSky Network.

**Réponse attendue** (200 OK) :
```json
[
  {
    "icao24": "abc123",
    "callsign": "AF1234",
    "originCountry": "France",
    "longitude": 2.3522,
    "latitude": 48.8566,
    "altitude": 10000.0,
    "velocity": 850.0,
    "verticalRate": 5.2,
    "model": "A320",
    "status": "climbing",
    "radarStatus": "ok",
    "lastContact": 1704067200
  },
  {
    "icao24": "def456",
    "callsign": "LH5678",
    "originCountry": "Germany",
    "longitude": 13.4050,
    "latitude": 52.5200,
    "altitude": 12000.0,
    "velocity": 900.0,
    "verticalRate": -3.5,
    "model": "B737",
    "status": "descending",
    "radarStatus": "ok",
    "lastContact": 1704067201
  }
]
```

**Tests Postman** :
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response is an array", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.be.an('array');
});

pm.test("Aircraft has required fields", function () {
    var jsonData = pm.response.json();
    if (jsonData.length > 0) {
        var aircraft = jsonData[0];
        pm.expect(aircraft).to.have.property('icao24');
        pm.expect(aircraft).to.have.property('callsign');
        pm.expect(aircraft).to.have.property('status');
        pm.expect(aircraft).to.have.property('radarStatus');
    }
});
```

---

### 2. GET /api/aircraft/live/{icao24}

**Nom** : Get Live Aircraft by ICAO24  
**Méthode** : GET  
**URL** : `{{apiBase}}/aircraft/live/abc123`  
**Headers** : Aucun

**Description** : Récupère un avion spécifique par son identifiant ICAO24.

**Réponse attendue** (200 OK) :
```json
{
  "icao24": "abc123",
  "callsign": "AF1234",
  "originCountry": "France",
  "longitude": 2.3522,
  "latitude": 48.8566,
  "altitude": 10000.0,
  "velocity": 850.0,
  "verticalRate": 5.2,
  "model": "A320",
  "status": "climbing",
  "radarStatus": "ok",
  "lastContact": 1704067200
}
```

**Réponse si non trouvé** (404 Not Found) :
```json
{}
```

**Tests Postman** :
```javascript
pm.test("Status code is 200 or 404", function () {
    pm.expect(pm.response.code).to.be.oneOf([200, 404]);
});

if (pm.response.code === 200) {
    pm.test("Aircraft has ICAO24", function () {
        var jsonData = pm.response.json();
        pm.expect(jsonData).to.have.property('icao24');
    });
}
```

---

### 3. GET /api/aircraft/live/country/{countryCode}

**Nom** : Get Live Aircraft by Country  
**Méthode** : GET  
**URL** : `{{apiBase}}/aircraft/live/country/Morocco`  
**Headers** : Aucun

**Description** : Filtre les avions par pays d'origine.

**Réponse attendue** (200 OK) :
```json
[
  {
    "icao24": "xyz789",
    "callsign": "AT1234",
    "originCountry": "Morocco",
    "longitude": -7.5898,
    "latitude": 33.3675,
    "altitude": 8000.0,
    "velocity": 750.0,
    "verticalRate": 0.0,
    "model": "A330",
    "status": "cruising",
    "radarStatus": "ok",
    "lastContact": 1704067202
  }
]
```

**Tests Postman** :
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("All aircraft are from Morocco", function () {
    var jsonData = pm.response.json();
    jsonData.forEach(function(aircraft) {
        pm.expect(aircraft.originCountry).to.equal("Morocco");
    });
});
```

---

### 4. GET /api/aircraft/live/radar-status/{status}

**Nom** : Get Live Aircraft by Radar Status  
**Méthode** : GET  
**URL** : `{{apiBase}}/aircraft/live/radar-status/danger`  
**Headers** : Aucun

**Description** : Filtre les avions par statut radar (ok, warning, danger).

**Valeurs possibles** :
- `ok` : Conditions normales
- `warning` : Manœuvre brusque détectée
- `danger` : Altitude très basse

**Réponse attendue** (200 OK) :
```json
[
  {
    "icao24": "danger123",
    "callsign": "EM123",
    "originCountry": "Spain",
    "longitude": -3.7038,
    "latitude": 40.4168,
    "altitude": 50.0,
    "velocity": 150.0,
    "verticalRate": -2.0,
    "model": "A320",
    "status": "landing",
    "radarStatus": "danger",
    "lastContact": 1704067203
  }
]
```

**Tests Postman** :
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("All aircraft have danger status", function () {
    var jsonData = pm.response.json();
    jsonData.forEach(function(aircraft) {
        pm.expect(aircraft.radarStatus).to.equal("danger");
    });
});
```

---

### 5. GET /api/aircraft/live/radar-status/warning

**Nom** : Get Live Aircraft with Warning Status  
**Méthode** : GET  
**URL** : `{{apiBase}}/aircraft/live/radar-status/warning`  
**Headers** : Aucun

**Description** : Récupère tous les avions avec statut radar "warning".

**Tests Postman** :
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("All aircraft have warning status", function () {
    var jsonData = pm.response.json();
    jsonData.forEach(function(aircraft) {
        pm.expect(aircraft.radarStatus).to.equal("warning");
    });
});
```

---

## 📥 IMPORT COLLECTION POSTMAN

### Format JSON pour Postman

```json
{
  "info": {
    "name": "Flight Radar - OpenSky Integration",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080",
      "type": "string"
    },
    {
      "key": "apiBase",
      "value": "{{baseUrl}}/api",
      "type": "string"
    }
  ],
  "item": [
    {
      "name": "Get All Live Aircraft",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "{{apiBase}}/aircraft/live",
          "host": ["{{apiBase}}"],
          "path": ["aircraft", "live"]
        }
      }
    },
    {
      "name": "Get Live Aircraft by ICAO24",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "{{apiBase}}/aircraft/live/abc123",
          "host": ["{{apiBase}}"],
          "path": ["aircraft", "live", "abc123"]
        }
      }
    },
    {
      "name": "Get Live Aircraft by Country",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "{{apiBase}}/aircraft/live/country/Morocco",
          "host": ["{{apiBase}}"],
          "path": ["aircraft", "live", "country", "Morocco"]
        }
      }
    },
    {
      "name": "Get Live Aircraft by Radar Status - Danger",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "{{apiBase}}/aircraft/live/radar-status/danger",
          "host": ["{{apiBase}}"],
          "path": ["aircraft", "live", "radar-status", "danger"]
        }
      }
    },
    {
      "name": "Get Live Aircraft by Radar Status - Warning",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "{{apiBase}}/aircraft/live/radar-status/warning",
          "host": ["{{apiBase}}"],
          "path": ["aircraft", "live", "radar-status", "warning"]
        }
      }
    }
  ]
}
```

---

## ✅ CHECKLIST DE VALIDATION

### Avant de tester
- [ ] Backend compilé (`mvn clean compile`)
- [ ] Backend démarré (`mvn spring-boot:run`)
- [ ] Logs montrent "Cache OpenSky mis à jour: X avions"
- [ ] Postman installé et configuré

### Tests à exécuter
- [ ] GET /api/aircraft/live retourne des données
- [ ] Les avions ont tous les champs requis
- [ ] Le statut est calculé correctement (on-ground, climbing, etc.)
- [ ] Le statut radar est calculé correctement (ok, warning, danger)
- [ ] GET /api/aircraft/live/{icao24} fonctionne
- [ ] GET /api/aircraft/live/country/{country} fonctionne
- [ ] GET /api/aircraft/live/radar-status/{status} fonctionne

---

**Date** : 2026  
**API** : OpenSky Network  
**Backend** : Spring Boot 3.2.0

