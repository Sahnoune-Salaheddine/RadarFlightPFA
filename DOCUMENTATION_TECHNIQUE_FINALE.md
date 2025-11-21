# 📚 DOCUMENTATION TECHNIQUE COMPLÈTE - Flight Radar 2026

## 🎯 VUE D'ENSEMBLE

**Flight Radar 2026** est une application web similaire à Flightradar24, permettant de suivre en temps réel les avions, la météo et les communications entre les centres radar et les aéroports.

**Objectif** : Améliorer la sécurité aérienne et réduire les risques d'accidents.

---

## 🏗️ ARCHITECTURE

### Stack Technologique

**Backend** :
- Spring Boot 3.2.0
- Java 17
- PostgreSQL
- JWT (JSON Web Tokens)
- WebSocket (temps réel)
- RestTemplate / WebClient (APIs externes)

**Frontend** :
- React 18
- Tailwind CSS
- Leaflet (cartes)
- Axios (HTTP client)

**APIs Externes** :
- OpenSky Network (positions avions)
- OpenWeatherMap (météo)

---

## 📁 STRUCTURE DU PROJET

```
PFA-2026/
├── backend/
│   ├── src/main/java/com/flightradar/
│   │   ├── model/          # Entités JPA
│   │   ├── repository/     # Repositories Spring Data
│   │   ├── service/        # Services métier
│   │   ├── controller/     # Contrôleurs REST
│   │   └── config/         # Configuration
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── database/
│   │   └── schema_complete.sql
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/     # Composants React
│   │   ├── services/       # Services API
│   │   └── App.jsx
│   └── package.json
└── documentation/
```

---

## 🗄️ MODÈLE DE DONNÉES

### Entités Principales

#### Airport
- **Relations** : 1→N Runways, 1→N Aircraft, 1→1 RadarCenter
- **Champs** : name, city, codeIATA, latitude, longitude

#### Aircraft
- **Relations** : N→1 Airport, N→1 Pilot, 1→N Flights
- **Champs** : model, registration, status, positionLat, positionLon, altitude, speed, heading
- **Sources** : Base de données (simulés) + OpenSky Network (live)

#### Pilot
- **Relations** : 1→N Aircraft
- **Champs** : name, license, experienceYears

#### RadarCenter
- **Relations** : 1→1 Airport
- **Champs** : name, code, frequency

#### WeatherData
- **Relations** : N→1 Airport
- **Champs** : windSpeed, windDirection, visibility, temperature, humidity, pressure, conditions, crosswind, alert
- **Source** : OpenWeatherMap API

#### Communication
- **Relations** : Pas de FK (polymorphique)
- **Champs** : senderType, senderId, receiverType, receiverId, message, frequency, timestamp

#### Flight
- **Relations** : N→1 Aircraft, N→1 Airport (departure), N→1 Airport (arrival)
- **Champs** : flightNumber, flightStatus, scheduledDeparture, scheduledArrival, actualDeparture, actualArrival

---

## 🔌 API REST

### Authentification

#### POST /api/auth/login
**Body** :
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Réponse** :
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "role": "ADMIN"
}
```

---

### Aéroports

#### GET /api/airports
**Réponse** : Liste de tous les aéroports

#### GET /api/airports/{id}
**Réponse** : Détails d'un aéroport

#### GET /api/airports/{id}/runways
**Réponse** : Liste des pistes d'un aéroport

---

### Avions

#### GET /api/aircraft
**Réponse** : Liste de tous les avions (base de données)

#### GET /api/aircraft/live
**Réponse** : Liste des avions live depuis OpenSky Network

#### GET /api/aircraft/live/{icao24}
**Réponse** : Avion spécifique depuis OpenSky

#### GET /api/aircraft/live/country/{countryCode}
**Réponse** : Avions filtrés par pays

#### GET /api/aircraft/live/radar-status/{status}
**Réponse** : Avions filtrés par statut radar (ok, warning, danger)

#### GET /api/aircraft/in-flight
**Réponse** : Avions en vol (base de données)

#### PUT /api/aircraft/{id}/updatePosition
**Body** :
```json
{
  "latitude": 33.3675,
  "longitude": -7.5898,
  "altitude": 10000.0,
  "speed": 850.0,
  "heading": 45.0
}
```

---

### Météo

#### GET /api/weather/airport/{id}
**Réponse** : Données météo d'un aéroport

#### GET /api/weather/alerts
**Réponse** : Liste des alertes météo actives

---

### Communications Radar

#### GET /api/radar/messages?radarCenterId={id}
**Réponse** : Messages d'un centre radar

#### POST /api/radar/send-message
**Body** :
```json
{
  "radarCenterId": 1,
  "receiverType": "AIRCRAFT",
  "receiverId": 1,
  "message": "Cleared for landing",
  "frequency": 121.5
}
```

---

### Vols

#### GET /api/flights
**Réponse** : Liste de tous les vols

#### GET /api/flights/{id}
**Réponse** : Détails d'un vol

#### POST /api/flights
**Body** :
```json
{
  "flightNumber": "AT1001",
  "aircraftId": 1,
  "departureAirportId": 1,
  "arrivalAirportId": 2,
  "scheduledDeparture": "2026-01-15T10:00:00",
  "scheduledArrival": "2026-01-15T11:30:00"
}
```

---

## 🔄 TEMPS RÉEL

### WebSocket

**Endpoint** : `/ws/realtime`

**Messages envoyés** :
- Positions avions (toutes les 5 secondes)
- Données météo (toutes les 10 minutes)
- Communications (en temps réel)
- Alertes (en temps réel)

### Polling (Alternative)

Le frontend peut utiliser polling si WebSocket n'est pas disponible :
- Positions : 5 secondes
- Météo : 10 minutes
- Communications : 5 secondes
- Alertes : 5 secondes

---

## 🔐 SÉCURITÉ

### JWT (JSON Web Tokens)

**Configuration** :
- Secret : `jwt.secret` (application.properties)
- Expiration : `jwt.expiration` (millisecondes)

**Rôles** :
- `ADMIN` : Accès complet
- `PILOTE` : Accès limité (avions, communications)
- `CENTRE_RADAR` : Accès radar (communications, avions)

### CORS

**Configuration** : `http://localhost:3000` (frontend React)

---

## 📊 INTÉGRATION OPENSKY NETWORK

### Service : OpenSkyService

**Fonctionnalités** :
- Récupération automatique toutes les 5 secondes
- Transformation des données brutes en objets normalisés
- Calcul automatique du statut (on-ground, climbing, descending, cruising, landing, takeoff)
- Calcul automatique du statut radar (ok, warning, danger)
- Enrichissement avec modèle d'avion

**API** : `https://opensky-network.org/api/states/all`

**Mapping** :
- `icao24` → Identifiant unique
- `callsign` → Indicatif d'appel
- `originCountry` → Pays d'origine
- `longitude`, `latitude` → Position GPS
- `baroAltitude` → Altitude (mètres)
- `velocity` → Vitesse (m/s → km/h)
- `verticalRate` → Taux vertical (m/s)

---

## 🌤️ INTÉGRATION OPENWEATHERMAP

### Service : WeatherService

**Fonctionnalités** :
- Récupération météo par coordonnées GPS
- Stockage en base de données
- Calcul vent de travers selon orientation piste
- Génération alertes automatiques
- Mise à jour automatique toutes les 10 minutes

**API** : `https://api.openweathermap.org/data/2.5/weather`

**Calculs** :
- Vent de travers : `windSpeed * sin(angle_diff)`
- Alertes :
  - Visibilité < 1km
  - Vent > 50km/h
  - Vent travers > 15km/h
  - Conditions dangereuses (Thunderstorm, Heavy Rain, Fog, Blizzard)

---

## 🚀 DÉMARRAGE

### Backend

```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

**URL** : `http://localhost:8080`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

**URL** : `http://localhost:3000`

### Base de données

```bash
psql -U postgres -d flightradar -f backend/database/schema_complete.sql
```

---

## 🧪 TESTS

### Tests Backend

```bash
cd backend
mvn test
```

### Tests API (curl)

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Avions
curl http://localhost:8080/api/aircraft

# Avions live
curl http://localhost:8080/api/aircraft/live

# Météo
curl http://localhost:8080/api/weather/airport/1
```

---

## 📝 CONFIGURATION

### application.properties

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/flightradar
spring.datasource.username=postgres
spring.datasource.password=postgres

# JWT
jwt.secret=your-secret-key-here-minimum-256-bits
jwt.expiration=86400000

# Weather API
weather.api.key=your-openweather-api-key
weather.api.url=https://api.openweathermap.org/data/2.5/weather

# CORS
spring.web.cors.allowed-origins=http://localhost:3000
```

---

## ✅ CHECKLIST DE VALIDATION

### Backend
- [ ] Compilation réussie
- [ ] Application démarre sans erreur
- [ ] Base de données initialisée
- [ ] Endpoints REST fonctionnels
- [ ] OpenSky intégré
- [ ] OpenWeather intégré
- [ ] WebSocket configuré
- [ ] JWT fonctionnel

### Frontend
- [ ] Application démarre
- [ ] Carte Leaflet affiche les avions
- [ ] Météo s'affiche
- [ ] Communications fonctionnent
- [ ] Alertes s'affichent
- [ ] Authentification fonctionne

---

## 🎯 FONCTIONNALITÉS

### ✅ Implémentées

- Affichage temps réel des avions (base + OpenSky)
- Météo par aéroport (OpenWeather)
- Communications VHF
- Alertes météo automatiques
- Authentification JWT
- WebSocket temps réel
- 4 aéroports marocains
- 8 avions Airbus
- 8 pilotes
- 4 centres radar

### 🔄 En cours / Améliorations possibles

- Historique des vols
- Statistiques avancées
- Notifications push
- Export de données
- API publique documentée (Swagger)

---

**Date** : 2026  
**Version** : 2.0  
**Auteur** : Équipe Flight Radar 2026

