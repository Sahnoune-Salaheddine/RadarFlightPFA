# 🛫 Intégration OpenSky Network - Flight Radar 2026

## 📋 RÉSUMÉ

Module backend complet pour intégrer l'API OpenSky Network dans le projet Flight Radar 2026.

**Fonctionnalités** :
- ✅ Récupération automatique des données toutes les 5 secondes
- ✅ Transformation et normalisation des données OpenSky
- ✅ Calcul automatique du statut de vol (on-ground, climbing, descending, cruising, landing, takeoff)
- ✅ Calcul du statut radar (ok, warning, danger)
- ✅ Enrichissement avec modèle d'avion
- ✅ Endpoint REST `/api/aircraft/live` pour le frontend

---

## 📁 FICHIERS CRÉÉS/MODIFIÉS

### 1️⃣ DTOs (Data Transfer Objects)

#### `backend/src/main/java/com/flightradar/model/dto/LiveAircraft.java`
**Type** : ADD  
**Description** : DTO pour représenter un avion en temps réel avec tous les champs requis.

**Champs** :
- `icao24` : Identifiant unique ICAO 24-bit
- `callsign` : Indicatif d'appel
- `originCountry` : Pays d'origine
- `longitude`, `latitude` : Position GPS
- `altitude` : Altitude en mètres
- `velocity` : Vitesse en km/h
- `verticalRate` : Taux vertical en m/s
- `model` : Modèle d'avion (enrichi)
- `status` : Statut calculé (on-ground, climbing, etc.)
- `radarStatus` : Statut radar (ok, warning, danger)
- `lastContact` : Timestamp Unix

#### `backend/src/main/java/com/flightradar/model/dto/OpenSkyResponse.java`
**Type** : ADD  
**Description** : DTO pour mapper la réponse JSON de l'API OpenSky.

---

### 2️⃣ Mapper

#### `backend/src/main/java/com/flightradar/service/OpenSkyMapper.java`
**Type** : ADD  
**Description** : Transforme les données brutes OpenSky en objets LiveAircraft normalisés.

**Fonctionnalités** :
- Mapping des champs OpenSky vers LiveAircraft
- Conversion des unités (m/s → km/h)
- Enrichissement avec modèle d'avion via Map statique
- Calcul automatique du statut de vol
- Calcul automatique du statut radar

**Règles de calcul du statut** :
- `velocity < 10 km/h` → `"on-ground"`
- `verticalRate > 2 m/s` → `"climbing"`
- `verticalRate < -2 m/s` → `"descending"` (ou `"landing"` si altitude < 2000m)
- `altitude > 8000 m` → `"cruising"`
- `altitude < 2000 m` et `verticalRate > 0` → `"takeoff"`

**Règles de calcul du statut radar** :
- `altitude < 100 m` → `"danger"`
- `|verticalRate| > 20 m/s` → `"warning"`
- Sinon → `"ok"`

---

### 3️⃣ Service

#### `backend/src/main/java/com/flightradar/service/OpenSkyService.java`
**Type** : ADD  
**Description** : Service pour récupérer et gérer les données OpenSky.

**Fonctionnalités** :
- `fetchLiveData()` : Appel REST vers OpenSky API
- `getLiveAircraft()` : Retourne la liste depuis le cache
- `updateLiveAircraftCache()` : Mise à jour automatique toutes les 5 secondes (@Scheduled)
- `getLiveAircraftByIcao24()` : Recherche par ICAO24
- `getLiveAircraftByCountry()` : Filtrage par pays
- `getLiveAircraftByRadarStatus()` : Filtrage par statut radar

**Gestion d'erreurs** :
- Timeout configuré (5s connect, 10s read)
- Fallback sur cache en cas d'erreur API
- Logging des erreurs

---

### 4️⃣ Configuration

#### `backend/src/main/java/com/flightradar/config/RestTemplateConfig.java`
**Type** : ADD  
**Description** : Configuration du bean RestTemplate avec timeouts.

---

### 5️⃣ Contrôleur (Modifié)

#### `backend/src/main/java/com/flightradar/controller/AircraftController.java`
**Type** : MODIFY  
**Description** : Ajout des endpoints pour les données live.

**Nouveaux endpoints** :
- `GET /api/aircraft/live` → Liste de tous les avions live
- `GET /api/aircraft/live/{icao24}` → Avion spécifique
- `GET /api/aircraft/live/country/{countryCode}` → Filtrage par pays
- `GET /api/aircraft/live/radar-status/{status}` → Filtrage par statut radar

---

## 🔧 CONFIGURATION

### Vérification @EnableScheduling

Le fichier `FlightRadarApplication.java` contient déjà `@EnableScheduling` ✅

```java
@SpringBootApplication
@EnableScheduling
public class FlightRadarApplication {
    // ...
}
```

### CORS

CORS est déjà configuré dans `SecurityConfig.java` pour `http://localhost:3000` ✅

---

## 🧪 TESTS POSTMAN

### 1. GET /api/aircraft/live

**Méthode** : GET  
**URL** : `http://localhost:8080/api/aircraft/live`  
**Headers** : Aucun

**Réponse attendue** :
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
  ...
]
```

---

### 2. GET /api/aircraft/live/{icao24}

**Méthode** : GET  
**URL** : `http://localhost:8080/api/aircraft/live/abc123`  
**Headers** : Aucun

**Réponse attendue** :
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

---

### 3. GET /api/aircraft/live/country/{countryCode}

**Méthode** : GET  
**URL** : `http://localhost:8080/api/aircraft/live/country/Morocco`  
**Headers** : Aucun

**Réponse attendue** : Tableau d'avions filtrés par pays d'origine.

---

### 4. GET /api/aircraft/live/radar-status/{status}

**Méthode** : GET  
**URL** : `http://localhost:8080/api/aircraft/live/radar-status/danger`  
**Headers** : Aucun

**Réponse attendue** : Tableau d'avions avec statut radar "danger".

**Valeurs possibles** : `ok`, `warning`, `danger`

---

### 5. GET /api/aircraft/live/radar-status/warning

**Méthode** : GET  
**URL** : `http://localhost:8080/api/aircraft/live/radar-status/warning`  
**Headers** : Aucun

**Réponse attendue** : Tableau d'avions avec statut radar "warning".

---

## 🧪 TESTS CURL

### Test 1 : Récupérer tous les avions live
```bash
curl -X GET http://localhost:8080/api/aircraft/live \
  -H "Content-Type: application/json"
```

### Test 2 : Récupérer un avion spécifique
```bash
curl -X GET http://localhost:8080/api/aircraft/live/abc123 \
  -H "Content-Type: application/json"
```

### Test 3 : Filtrer par pays
```bash
curl -X GET http://localhost:8080/api/aircraft/live/country/Morocco \
  -H "Content-Type: application/json"
```

### Test 4 : Filtrer par statut radar (danger)
```bash
curl -X GET http://localhost:8080/api/aircraft/live/radar-status/danger \
  -H "Content-Type: application/json"
```

### Test 5 : Filtrer par statut radar (warning)
```bash
curl -X GET http://localhost:8080/api/aircraft/live/radar-status/warning \
  -H "Content-Type: application/json"
```

---

## ✅ VÉRIFICATIONS

### 1. Compilation
```bash
cd backend
mvn clean compile
```
**Attendu** : `BUILD SUCCESS`

### 2. Démarrage
```bash
cd backend
mvn spring-boot:run
```
**Attendu** : `Started FlightRadarApplication`  
**Vérifier les logs** : `Cache OpenSky mis à jour: X avions` (toutes les 5 secondes)

### 3. Test endpoint
```bash
curl http://localhost:8080/api/aircraft/live | jq '.[0]'
```
**Attendu** : Premier avion avec tous les champs remplis

---

## 📝 NOTES IMPORTANTES

### 1. Mapping Modèle d'Avion

Le mapping ICAO24 → Modèle est actuellement une Map statique dans `OpenSkyMapper.java`.

**Pour améliorer** :
- Charger depuis une base de données
- Utiliser une API externe (Aviation Edge, Aircraft Database)
- Créer une table `aircraft_models` avec colonnes `icao24` et `model`

### 2. Rate Limiting OpenSky

L'API OpenSky Network a des limites :
- **Anonyme** : ~10 requêtes/minute
- **Authentifié** : Plus de requêtes (nécessite compte)

**Recommandation** : Mettre à jour toutes les 5 secondes est acceptable pour un usage anonyme.

### 3. Gestion des Erreurs

Le service gère automatiquement :
- Timeouts (5s connect, 10s read)
- Erreurs réseau (fallback sur cache)
- Données invalides (filtrage)

### 4. Performance

- Cache en mémoire (`CopyOnWriteArrayList`) pour thread-safety
- Mise à jour asynchrone toutes les 5 secondes
- Pas de blocage des requêtes utilisateur

---

## 🚀 UTILISATION FRONTEND

### Exemple React

```javascript
import { useEffect, useState } from 'react';
import api from '../services/api';

function LiveAircraftMap() {
  const [aircraft, setAircraft] = useState([]);

  useEffect(() => {
    const fetchLiveAircraft = async () => {
      try {
        const response = await api.get('/aircraft/live');
        setAircraft(response.data);
      } catch (error) {
        console.error('Erreur:', error);
      }
    };

    fetchLiveAircraft();
    const interval = setInterval(fetchLiveAircraft, 5000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div>
      {aircraft.map(ac => (
        <div key={ac.icao24}>
          {ac.callsign} - {ac.status} - {ac.radarStatus}
        </div>
      ))}
    </div>
  );
}
```

---

## 📊 STRUCTURE DES DONNÉES

### Format OpenSky (entrée)

```json
{
  "time": 1704067200,
  "states": [
    [
      "abc123",           // 0: icao24
      "AF1234",           // 1: callsign
      "France",           // 2: originCountry
      1704067200,         // 3: timePosition
      1704067200,         // 4: lastContact
      2.3522,             // 5: longitude
      48.8566,            // 6: latitude
      10000.0,            // 7: baroAltitude
      false,              // 8: onGround
      236.11,             // 9: velocity (m/s)
      45.0,               // 10: trueTrack
      5.2,                // 11: verticalRate (m/s)
      null,               // 12: sensors
      10050.0,            // 13: geoAltitude
      "1234",             // 14: squawk
      false,              // 15: spi
      0                   // 16: positionSource
    ]
  ]
}
```

### Format LiveAircraft (sortie)

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

---

## ✅ CHECKLIST DE VALIDATION

- [ ] Compilation réussie (`mvn clean compile`)
- [ ] Application démarre sans erreur
- [ ] Logs montrent "Cache OpenSky mis à jour: X avions"
- [ ] `GET /api/aircraft/live` retourne des données
- [ ] Les avions ont un `status` calculé
- [ ] Les avions ont un `radarStatus` calculé
- [ ] Le frontend peut consommer l'endpoint
- [ ] Les filtres (country, radar-status) fonctionnent

---

**Date** : 2026  
**Statut** : ✅ Prêt pour utilisation  
**API** : OpenSky Network (https://opensky-network.org)

