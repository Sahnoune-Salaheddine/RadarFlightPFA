# 📋 ARCHITECTURE COMPLÈTE DU PROJET RADAR FLIGHT

## 🎯 Vue d'ensemble

**RadarFlight** est une application web complète de gestion et suivi de trafic aérien en temps réel. Elle permet de gérer les vols, suivre les positions des avions, gérer les communications ATC (Air Traffic Control), surveiller les conditions météorologiques, et détecter les conflits entre avions.

**Technologies principales :**
- **Backend** : Spring Boot 3.2.0 (Java 17)
- **Frontend** : React 18.2.0 + Vite 5.0.8
- **Base de données** : PostgreSQL 14+
- **Communication temps réel** : WebSocket (STOMP/SockJS)
- **Sécurité** : JWT + Spring Security

---

## 🏗️ ARCHITECTURE GLOBALE

```
┌─────────────────────────────────────────────────────────┐
│                    FRONTEND (React)                      │
│                  Port 3000 / 3001                        │
│  - React 18.2.0 + Vite 5.0.8                            │
│  - TailwindCSS 3.3.6                                     │
│  - Leaflet (Cartes)                                      │
│  - WebSocket Client (STOMP/SockJS)                      │
└────────────────────┬──────────────────────────────────────┘
                     │ HTTP REST API + WebSocket
                     │ Authorization: Bearer {JWT}
                     │
┌────────────────────▼──────────────────────────────────────┐
│                    BACKEND (Spring Boot)                  │
│                      Port 8080                            │
│  - Spring Boot 3.2.0                                      │
│  - Spring Security + JWT                                  │
│  - Spring Data JPA / Hibernate                           │
│  - Spring WebSocket (STOMP)                              │
│  - Maven                                                  │
└────────────────────┬──────────────────────────────────────┘
                     │ JDBC (PostgreSQL Driver)
                     │
┌────────────────────▼──────────────────────────────────────┐
│              BASE DE DONNÉES (PostgreSQL)                  │
│                      Port 5432                            │
│  - Base: flightradar                                      │
│  - 10 tables principales                                  │
│  - Relations avec contraintes FK                          │
└───────────────────────────────────────────────────────────┘
```

---

## 🗄️ BASE DE DONNÉES (PostgreSQL)

### Structure Complète des Tables

#### 1. **users** - Utilisateurs du système
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- Hashé avec BCrypt
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'PILOTE', 'CENTRE_RADAR')),
    airport_id BIGINT,  -- Pour CENTRE_RADAR
    pilot_id BIGINT,    -- Pour PILOTE
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Rôles et permissions :**
- **ADMIN** : Accès complet, gestion des vols, utilisateurs, avions, statistiques
- **PILOTE** : Accès à son dashboard, gestion de ses vols assignés, communication ATC
- **CENTRE_RADAR** : Surveillance du trafic aérien, communications ATC, autorisations

#### 2. **airports** - Aéroports
```sql
CREATE TABLE airports (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    code_iata VARCHAR(3) UNIQUE NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Aéroports initialisés :**
- Casablanca (CMN) : 33.3675, -7.5898
- Rabat (RBA) : 34.0515, -6.7515
- Marrakech (RAK) : 31.6069, -8.0363
- Tanger (TNG) : 35.7269, -5.9169

#### 3. **pilots** - Pilotes
```sql
CREATE TABLE pilots (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    license VARCHAR(50) UNIQUE NOT NULL,
    experience_years INTEGER NOT NULL DEFAULT 0,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    assigned_aircraft_id BIGINT,
    user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);
```

#### 4. **aircraft** - Avions
```sql
CREATE TABLE aircraft (
    id BIGSERIAL PRIMARY KEY,
    model VARCHAR(50) NOT NULL,
    registration VARCHAR(20) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('AU_SOL', 'DECOLLAGE', 'EN_VOL', 'ATTERRISSAGE', 'EN_ATTENTE')),
    airport_id BIGINT,
    pilot_id BIGINT,
    username_pilote VARCHAR(50),
    position_lat DECIMAL(10, 8),
    position_lon DECIMAL(11, 8),
    altitude DECIMAL(10, 2) DEFAULT 0,  -- en mètres
    speed DECIMAL(8, 2) DEFAULT 0,      -- en km/h
    heading DECIMAL(5, 2) DEFAULT 0 CHECK (heading >= 0 AND heading < 360),
    air_speed DECIMAL(8, 2),
    vertical_speed DECIMAL(8, 2),        -- m/s
    transponder_code VARCHAR(4),
    trajectoire_prevue TEXT,             -- JSON
    trajectoire_reelle TEXT,             -- JSON
    last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (airport_id) REFERENCES airports(id) ON DELETE SET NULL,
    FOREIGN KEY (pilot_id) REFERENCES pilots(id) ON DELETE SET NULL
);
```

#### 5. **flights** - Vols
```sql
CREATE TABLE flights (
    id BIGSERIAL PRIMARY KEY,
    flight_number VARCHAR(20) UNIQUE NOT NULL,
    airline VARCHAR(100),
    aircraft_id BIGINT,
    departure_airport_id BIGINT NOT NULL,
    arrival_airport_id BIGINT NOT NULL,
    flight_status VARCHAR(20) NOT NULL CHECK (flight_status IN ('PLANIFIE', 'EN_COURS', 'TERMINE', 'ANNULE', 'RETARDE')),
    scheduled_departure TIMESTAMP,
    scheduled_arrival TIMESTAMP,
    actual_departure TIMESTAMP,
    actual_arrival TIMESTAMP,
    estimated_arrival TIMESTAMP,
    cruise_altitude INTEGER,            -- en pieds
    cruise_speed INTEGER,                -- en nœuds
    flight_type VARCHAR(20) CHECK (flight_type IN ('COMMERCIAL', 'CARGO', 'PRIVATE', 'MILITARY', 'TRAINING')),
    pilot_id BIGINT,
    alternate_airport_id BIGINT,
    estimated_time_enroute INTEGER,      -- en minutes
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (aircraft_id) REFERENCES aircraft(id) ON DELETE SET NULL,
    FOREIGN KEY (departure_airport_id) REFERENCES airports(id),
    FOREIGN KEY (arrival_airport_id) REFERENCES airports(id),
    FOREIGN KEY (pilot_id) REFERENCES pilots(id) ON DELETE SET NULL,
    FOREIGN KEY (alternate_airport_id) REFERENCES airports(id) ON DELETE SET NULL
);
```

#### 6. **runways** - Pistes d'atterrissage
```sql
CREATE TABLE runways (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(10) NOT NULL,
    orientation DECIMAL(5, 2) NOT NULL CHECK (orientation >= 0 AND orientation < 360),
    length_meters INTEGER NOT NULL,
    width_meters INTEGER NOT NULL,
    airport_id BIGINT NOT NULL,
    FOREIGN KEY (airport_id) REFERENCES airports(id) ON DELETE CASCADE,
    UNIQUE(airport_id, name)
);
```

#### 7. **radar_centers** - Centres de contrôle radar
```sql
CREATE TABLE radar_centers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    frequency DECIMAL(6, 2) NOT NULL,
    airport_id BIGINT NOT NULL,
    user_id BIGINT,
    FOREIGN KEY (airport_id) REFERENCES airports(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);
```

**Note importante :** Un aéroport peut avoir plusieurs centres radar (gestion des doublons dans le code).

#### 8. **weather_data** - Données météorologiques
```sql
CREATE TABLE weather_data (
    id BIGSERIAL PRIMARY KEY,
    airport_id BIGINT NOT NULL,
    wind_speed DECIMAL(6, 2),            -- km/h
    wind_direction DECIMAL(5, 2),       -- 0-360 degrés
    visibility DECIMAL(6, 2),           -- km
    temperature DECIMAL(5, 2),          -- °C
    humidity INTEGER,                    -- 0-100%
    pressure DECIMAL(7, 2),             -- hPa
    conditions VARCHAR(50),
    crosswind DECIMAL(6, 2),
    alert BOOLEAN DEFAULT FALSE,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (airport_id) REFERENCES airports(id) ON DELETE CASCADE
);
```

#### 9. **communications** - Communications ATC
```sql
CREATE TABLE communications (
    id BIGSERIAL PRIMARY KEY,
    sender_type VARCHAR(20) NOT NULL CHECK (sender_type IN ('RADAR', 'AIRCRAFT', 'AIRPORT')),
    sender_id BIGINT NOT NULL,
    receiver_type VARCHAR(20) NOT NULL CHECK (receiver_type IN ('RADAR', 'AIRCRAFT', 'AIRPORT')),
    receiver_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 10. **activity_logs** - Journal d'activité
```sql
CREATE TABLE activity_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    activity_type VARCHAR(50),
    entity_type VARCHAR(50),
    entity_id BIGINT,
    description TEXT,
    severity VARCHAR(20),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);
```

### Relations entre Tables

```
users (1) ──< (1) pilots
users (1) ──< (1) radar_centers

airports (1) ──< (N) runways
airports (1) ──< (N) aircraft
airports (1) ──< (N) radar_centers
airports (1) ──< (N) weather_data
airports (1) ──< (N) flights (departure)
airports (1) ──< (N) flights (arrival)
airports (1) ──< (N) flights (alternate)

pilots (1) ──< (N) aircraft
aircraft (1) ──< (N) flights
```

### Scripts SQL Disponibles

**Scripts de migration :**
- `schema.sql` - Schéma complet de la base de données
- `add_flight_fields.sql` - Ajout de colonnes aux vols
- `fix_flight_number_length.sql` - Correction de la longueur du numéro de vol
- `VERIFIER_ET_CORRIGER_FLIGHTS.sql` - Vérification et correction de la table flights
- `CORRIGER_FLIGHTS_FORCE.sql` - Correction forcée de la table flights

**Scripts d'assignation :**
- `assign_aircraft_to_pilot.sql` - Assignation d'avion au pilote
- `ASSIGNER_AVION_PILOTE_IMMEDIAT.sql` - Assignation automatique pour tous les pilotes
- `ASSIGNER_AVION_RAPIDE.sql` - Script rapide d'assignation

**Scripts de diagnostic :**
- `VERIFIER_COLONNES_FLIGHTS.sql` - Vérification des colonnes
- `VERIFIER_DOUBLONS_RADAR_CENTERS.sql` - Détection de doublons
- `verifier_et_corriger_pilotes.sql` - Vérification des pilotes

---

## ⚙️ BACKEND (Spring Boot)

### Technologies Utilisées

- **Framework** : Spring Boot 3.2.0
- **Java** : Version 17
- **ORM** : Spring Data JPA / Hibernate 6.x
- **Sécurité** : Spring Security 6.x + JWT (JJWT 0.12.3)
- **WebSocket** : Spring WebSocket (STOMP)
- **Base de données** : PostgreSQL Driver 42.7.1
- **Build** : Maven 3.9+
- **Logging** : SLF4J + Logback

### Structure des Packages

```
com.flightradar
├── config/              # Configurations
│   ├── SecurityConfig.java
│   ├── WebSocketConfig.java
│   ├── CorsConfig.java
│   └── DataInitializer.java
├── controller/          # Contrôleurs REST
│   ├── AuthController.java
│   ├── FlightController.java
│   ├── PilotDashboardController.java
│   ├── RadarDashboardController.java
│   ├── AdminDashboardController.java
│   ├── AircraftController.java
│   ├── AirportController.java
│   ├── WeatherController.java
│   ├── RadarController.java
│   ├── ATCController.java
│   ├── ConflictController.java
│   └── RunwayController.java
├── model/               # Entités JPA
│   ├── User.java
│   ├── Pilot.java
│   ├── Aircraft.java
│   ├── Flight.java
│   ├── Airport.java
│   ├── RadarCenter.java
│   ├── WeatherData.java
│   ├── Communication.java
│   ├── ActivityLog.java
│   └── dto/            # Data Transfer Objects
│       ├── PilotDashboardDTO.java
│       └── LiveAircraft.java
├── repository/          # Repositories JPA
│   ├── UserRepository.java
│   ├── PilotRepository.java
│   ├── AircraftRepository.java
│   ├── FlightRepository.java
│   ├── AirportRepository.java
│   ├── RadarCenterRepository.java
│   ├── WeatherDataRepository.java
│   ├── CommunicationRepository.java
│   └── ActivityLogRepository.java
├── service/             # Services métier
│   ├── AuthService.java
│   ├── FlightSimulationService.java
│   ├── FlightManagementService.java
│   ├── PilotDashboardService.java
│   ├── RadarDashboardService.java
│   ├── AdminDashboardService.java
│   ├── AircraftService.java
│   ├── WeatherService.java
│   ├── ATCService.java
│   ├── RadarService.java
│   └── ConflictDetectionService.java
└── security/            # Filtres de sécurité
    ├── JwtAuthenticationFilter.java
    └── JwtTokenProvider.java
```

### Contrôleurs REST - Endpoints Complets

#### 1. **AuthController** (`/api/auth`)
- `POST /api/auth/login` - Authentification (retourne JWT)
  - Body: `{ "username": "string", "password": "string" }`
  - Response: `{ "token": "string", "role": "ADMIN|PILOTE|CENTRE_RADAR" }`
- `POST /api/auth/register` - Création de compte (ADMIN uniquement)
  - Body: `{ "username": "string", "password": "string", "role": "string" }`

#### 2. **FlightController** (`/api/flight`)
- `POST /api/flight/simulate-takeoff` - Démarrer simulation de vol
  - Body: `{ "aircraftId": number, "departureAirportId": number, "arrivalAirportId": number }`
  - Response: `{ "success": boolean, "flightId": number, "estimatedArrival": "timestamp" }`
- `GET /api/flight/{flightId}` - Statut d'un vol
- `GET /api/flight` - Liste tous les vols (ADMIN/RADAR)
- `POST /api/flight/manage` - Créer un vol (ADMIN uniquement)
  - Body: `{ "flightNumber": "string", "airline": "string", ... }`
- `PUT /api/flight/manage/{flightId}` - Modifier un vol (ADMIN uniquement)
- `DELETE /api/flight/manage/{flightId}` - Supprimer un vol (ADMIN uniquement)
- `GET /api/flight/pilot/{pilotId}` - Vols d'un pilote (par ID)
- `GET /api/flight/pilot/username/{username}` - Vols d'un pilote (par username)
  - Requiert: `@PreAuthorize("hasAnyRole('ADMIN', 'PILOTE')")`

#### 3. **PilotDashboardController** (`/api/pilots`)
- `GET /api/pilots/{username}/dashboard` - Dashboard complet du pilote
  - Requiert: `@PreAuthorize("hasAnyRole('PILOTE', 'ADMIN')")`
  - Retourne: `PilotDashboardDTO` avec toutes les informations
  - Erreur 404 si aucun avion assigné (code: `NO_AIRCRAFT_ASSIGNED`)
- `GET /api/pilots/{username}/aircraft` - Avion assigné au pilote

#### 4. **RadarDashboardController** (`/api/radar`)
- `GET /api/radar/dashboard` - Dashboard radar complet
- `GET /api/radar/dashboard/aircraft` - Avions dans le secteur
- `GET /api/radar/dashboard/atis` - Données ATIS

#### 5. **AdminDashboardController** (`/api/admin`)
- `GET /api/admin/dashboard` - Dashboard admin
- `GET /api/admin/activity-logs` - Journal d'activité

#### 6. **AircraftController** (`/api/aircraft`)
- `GET /api/aircraft` - Liste tous les avions (public)
- `GET /api/aircraft/{id}` - Détails d'un avion
- `GET /api/aircraft/airport/{airportId}` - Avions d'un aéroport
- `GET /api/aircraft/in-flight` - Avions en vol
- `GET /api/aircraft/pilot/{username}` - Avion d'un pilote

#### 7. **AirportController** (`/api/airports`)
- `GET /api/airports` - Liste tous les aéroports (public)
- `GET /api/airports/{id}` - Détails d'un aéroport
- `GET /api/airports/code/{codeIATA}` - Aéroport par code IATA
- `GET /api/airports/{id}/weather` - Météo d'un aéroport

#### 8. **WeatherController** (`/api/weather`)
- `GET /api/weather/airport/{id}` - Météo d'un aéroport
- `GET /api/weather/alerts` - Alertes météo

#### 9. **RadarController** (`/api/radar`)
- `POST /api/radar/sendMessage` - Envoyer un message depuis le radar
  - Body: `{ "radarCenterId": number, "receiverType": "string", "receiverId": number, "message": "string" }`
- `GET /api/radar/messages` - Messages d'un centre radar
- `GET /api/radar/aircraft/{aircraftId}/messages` - Communications d'un avion

#### 10. **ATCController** (`/api/atc`)
- `POST /api/atc/request-takeoff-clearance` - Demander autorisation de décollage
  - Body: `{ "aircraftId": number }`
  - Response: `{ "status": "GRANTED|REFUSED|PENDING", "message": "string" }`
- `GET /api/atc/clearance-status/{aircraftId}` - Statut d'une autorisation

#### 11. **ConflictController** (`/api/conflicts`)
- `GET /api/conflicts` - Liste des conflits détectés

#### 12. **RunwayController** (`/api/runways`)
- `GET /api/runways` - Liste toutes les pistes
- `GET /api/runways/{id}` - Détails d'une piste
- `GET /api/runways/airport/{airportId}` - Pistes d'un aéroport

### Services Principaux - Détails Techniques

#### 1. **FlightSimulationService**
**Responsabilité** : Simuler les vols en temps réel après décollage

**Fonctionnalités :**
- Calcul de trajectoire entre aéroports (formule de Haversine)
- Simulation de montée : 0 → 10 000m à 10 m/s
- Simulation de croisière : 10 000m, 800 km/h
- Simulation de descente : 10 000m → 0 à 8 m/s
- Mise à jour position toutes les 5 secondes
- Calcul ETA (Estimated Time of Arrival)
- Broadcast WebSocket des positions via `/topic/aircraft/{id}`

**Constantes de simulation :**
- Altitude de croisière : 10 000 mètres
- Vitesse de croisière : 800 km/h
- Taux de montée : 10 m/s
- Taux de descente : 8 m/s
- Vitesse au décollage : 250 km/h

**Méthodes principales :**
- `simulateFlight(Long aircraftId, Long departureAirportId, Long arrivalAirportId)` - Démarre la simulation
- `calculateTrajectory()` - Calcule la trajectoire complète
- `updateAircraftPosition()` - Met à jour la position de l'avion

#### 2. **FlightManagementService**
**Responsabilité** : CRUD complet des vols

**Fonctionnalités :**
- Création de vols avec validation complète
- Parsing des dates `datetime-local` (format `YYYY-MM-DDTHH:mm` → `YYYY-MM-DDTHH:mm:ss`)
- Validation des contraintes (aéroports, avion, pilote)
- Modification (uniquement si vol non en cours)
- Suppression (uniquement si vol non en cours)
- Récupération des détails complets avec météo
- Gestion des transactions (`@Transactional`)

**Gestion d'erreur :**
- `IllegalArgumentException` : Erreurs de validation
- `DataIntegrityViolationException` : Erreurs de contraintes DB
- Messages d'erreur détaillés avec `type` et `details`

**Méthodes principales :**
- `createFlight(Map<String, Object> flightData, String username)` - Créer un vol
- `updateFlight(Long flightId, Map<String, Object> flightData)` - Modifier un vol
- `deleteFlight(Long flightId)` - Supprimer un vol
- `getFlightDetails(Long flightId)` - Détails complets avec météo
- `getFlightsByPilot(Long pilotId)` - Vols d'un pilote

#### 3. **PilotDashboardService**
**Responsabilité** : Rassemble toutes les données pour le dashboard pilote

**Fonctionnalités :**
- Récupération de l'avion assigné au pilote
- Récupération du vol actif
- Données de position (ADS-B)
- Météo du vol
- Communications ATC
- Centre radar responsable
- KPIs (distance restante, ETA, consommation carburant, etc.)
- Détection de trafic dans un rayon de 30 km

**Gestion d'erreur :**
- `RuntimeException` si pilote non trouvé
- `RuntimeException` avec code `NO_AIRCRAFT_ASSIGNED` si aucun avion assigné
- Gestion des doublons de centres radar (prend le premier)

**Méthodes principales :**
- `getPilotDashboard(String username)` - Dashboard complet
- `calculateKPIs(Aircraft aircraft, Flight flight)` - Calcul des KPIs
- `calculateTrafficDensity(Aircraft aircraft, double radiusKm)` - Densité de trafic
- `calculateAircraftHealthScore(Aircraft aircraft)` - Score de santé (0-100)

#### 4. **RealtimeUpdateService**
**Responsabilité** : Broadcast des mises à jour via WebSocket

**Tâches planifiées :**
- `@Scheduled(fixedRate = 5000)` : Positions avions toutes les 5s
- `@Scheduled(fixedRate = 30000)` : Alertes météo toutes les 30s
- `@Scheduled(fixedRate = 5000)` : Alertes de conflit toutes les 5s

**Topics WebSocket :**
- `/topic/aircraft` - Positions de tous les avions
- `/topic/aircraft/{id}` - Mises à jour d'un avion spécifique
- `/topic/weather-alerts` - Alertes météo
- `/topic/weather/{airportId}` - Météo d'un aéroport
- `/topic/conflicts` - Conflits détectés

#### 5. **ATCService**
**Responsabilité** : Gestion des autorisations de décollage/atterrissage

**Règles d'autorisation (ICAO/FAA) :**
- Vérification de la météo (visibilité, vent, précipitations)
- Vérification de la disponibilité de la piste
- Vérification du statut de l'avion
- Statuts possibles : `GRANTED`, `REFUSED`, `PENDING`

**Méthodes principales :**
- `requestTakeoffClearance(Long aircraftId)` - Demander autorisation
- `checkWeatherConditions(Airport airport)` - Vérifier météo
- `checkRunwayAvailability(Airport airport)` - Vérifier piste

#### 6. **WeatherService**
**Responsabilité** : Récupération des données météo

**Source** : API Open-Meteo (gratuite, pas de clé API)
- URL : `https://api.open-meteo.com/v1/forecast`
- Mise à jour automatique des données météo
- Cache : 1 heure pour éviter trop d'appels API

**Données récupérées :**
- Température
- Vitesse du vent
- Direction du vent
- Visibilité
- Pression atmosphérique
- Conditions météo

#### 7. **ConflictDetectionService**
**Responsabilité** : Détection de conflits entre avions

**Critères de détection :**
- Distance horizontale < seuil (par défaut 5 km)
- Altitude similaire (différence < 1000m)
- Trajectoires convergentes
- Calcul toutes les 5 secondes

**Méthodes principales :**
- `detectConflicts()` - Détecte tous les conflits
- `calculateDistance()` - Distance entre deux avions
- `areTrajectoriesConverging()` - Vérifie convergence

#### 8. **RadarService**
**Responsabilité** : Gestion des communications radar

**Fonctionnalités :**
- Envoi de messages depuis le radar
- Réception de messages
- Gestion des communications VHF
- Vérification de piste avant décollage
- Autorisation/défense de décollage selon météo

#### 9. **AdminDashboardService**
**Responsabilité** : Calcul de tous les KPIs pour le dashboard admin

**KPIs calculés :**
- Nombre total d'avions
- Nombre d'avions en vol
- Nombre de vols actifs
- Nombre de conflits détectés
- Statistiques par aéroport
- Graphiques de performance

#### 10. **RadarDashboardService**
**Responsabilité** : Gestion de la vue radar et communications ATC

**Fonctionnalités :**
- Liste des avions dans le secteur
- Données ATIS (Automatic Terminal Information Service)
- Communications ATC
- Alertes de conflit
- Météo des aéroports

### Sécurité

#### Configuration Spring Security

**Fichier** : `SecurityConfig.java`

**Filtres :**
- `JwtAuthenticationFilter` : Vérifie le token JWT dans le header `Authorization: Bearer {token}`
- Filtre exécuté avant `UsernamePasswordAuthenticationFilter`

**Endpoints publics :**
- `/api/auth/login`
- `/api/airports/**`
- `/api/aircraft/**`
- `/api/weather/**`
- `/api/flights/**` (lecture seule)
- `/api/runways/**`
- `/api/conflicts/**`
- `/ws/**` (WebSocket)

**Endpoints protégés par rôle :**
- `/api/admin/**` → `hasRole("ADMIN")`
- `/api/radar/**` → `hasAnyRole("CENTRE_RADAR", "ADMIN")`
- `/api/pilots/**` → `hasAnyRole("PILOTE", "ADMIN")`
- `/api/atc/**` → `hasAnyRole("PILOTE", "CENTRE_RADAR", "ADMIN")`
- `/api/flight/manage/**` → `hasRole("ADMIN")`
- `/api/flight/pilot/**` → `hasAnyRole("ADMIN", "PILOTE")`

**CORS :**
- Origines autorisées : `http://localhost:3000`, `http://localhost:3001`
- Méthodes : GET, POST, PUT, DELETE, OPTIONS
- Headers : Tous autorisés
- Credentials : `allowCredentials(true)`

**JWT Configuration :**
- Secret : `flightradar-secret-key-2026-very-secure-key-for-jwt-token-generation`
- Expiration : 86400000 ms (24 heures)
- Algorithme : HS256
- Claims : `username`, `role`

### WebSocket Configuration

**Fichier** : `WebSocketConfig.java`

**Endpoint** : `/ws`
**Protocole** : STOMP over SockJS

**Broker de messages :**
- Préfixe topics : `/topic`
- Préfixe queues : `/queue`
- Préfixe application : `/app`

**Origines autorisées :**
- `http://localhost:3000`
- `http://localhost:3001`

**Configuration :**
- `enableSimpleBroker("/topic", "/queue")` - Broker en mémoire
- `setApplicationDestinationPrefixes("/app")` - Préfixe pour les messages clients

---

## 🎨 FRONTEND (React + Vite)

### Technologies Utilisées

- **Framework** : React 18.2.0
- **Build Tool** : Vite 5.0.8
- **Styling** : TailwindCSS 3.3.6
- **Routing** : React Router DOM 6.20.0
- **HTTP Client** : Axios 1.6.2
- **WebSocket** : SockJS 1.6.1 + @stomp/stompjs 7.0.0
- **Cartes** : Leaflet 1.9.4 + React-Leaflet 4.2.1
- **Graphiques** : Chart.js 4.4.0 + React-Chartjs-2 5.2.0
- **UI Components** : Headless UI + Heroicons

### Structure des Dossiers

```
frontend/src/
├── components/          # Composants réutilisables
│   ├── Login.jsx
│   ├── Dashboard.jsx
│   ├── FlightMap.jsx
│   ├── AircraftList.jsx
│   ├── WeatherPanel.jsx
│   ├── CommunicationPanel.jsx
│   ├── AlertPanel.jsx
│   ├── FlightManagement.jsx
│   ├── OperationsOverview.jsx
│   └── PilotsAircraftList.jsx
├── pages/              # Pages principales
│   ├── PilotDashboard.jsx
│   ├── RadarDashboard.jsx
│   └── AdminDashboard.jsx
├── context/            # Contextes React
│   └── AuthContext.jsx
├── hooks/              # Hooks personnalisés
│   └── useWebSocket.js
├── services/           # Services API
│   └── api.js
├── App.jsx             # Composant racine
├── main.jsx            # Point d'entrée
└── index.css           # Styles globaux
```

### Pages Principales

#### 1. **Login** (`/login`)
- Authentification avec username/password
- Stockage du token JWT dans localStorage
- Redirection selon le rôle après connexion :
  - `ADMIN` → `/admin`
  - `PILOTE` → `/pilot`
  - `CENTRE_RADAR` → `/radar`

#### 2. **PilotDashboard** (`/pilot`)
**Accès** : Rôle PILOTE

**Fonctionnalités :**
- Affichage de l'avion assigné (ou message "NO AIRCRAFT ASSIGNED")
- Liste des vols assignés
- Carte en temps réel avec position de l'avion (Leaflet)
- Données de vol (altitude, vitesse, cap, etc.) - Style HUD
- Communication avec ATC
- Demande d'autorisation de décollage
- Suivi du vol en temps réel via WebSocket
- Graphiques de performance
- Météo du vol
- Alertes et notifications

**Gestion d'erreur :**
- Affichage du message "NO AIRCRAFT ASSIGNED" si erreur 404 avec code `NO_AIRCRAFT_ASSIGNED`
- Gestion gracieuse des erreurs de connexion

#### 3. **RadarDashboard** (`/radar`)
**Accès** : Rôle CENTRE_RADAR

**Fonctionnalités :**
- Carte avec tous les avions dans le secteur
- Liste des avions en vol
- Données ATIS (Automatic Terminal Information Service)
- Communications ATC
- Alertes de conflit
- Météo des aéroports
- Surveillance du trafic aérien en temps réel

#### 4. **AdminDashboard** (`/admin`)
**Accès** : Rôle ADMIN

**Fonctionnalités :**
- Vue d'ensemble complète du système
- Gestion des vols (CRUD complet)
- Gestion des avions
- Gestion des utilisateurs
- Journal d'activité
- Statistiques globales
- Graphiques de performance
- KPIs aéronautiques

### Gestion de l'Authentification

**Fichier** : `context/AuthContext.jsx`

**Fonctionnalités :**
- Stockage du token JWT dans localStorage
- Ajout automatique du header `Authorization: Bearer {token}` aux requêtes Axios
- Gestion de l'état d'authentification
- Redirection automatique si non authentifié
- Fonction `logout()` pour déconnexion

**Données stockées :**
- `token` : Token JWT
- `username` : Nom d'utilisateur
- `role` : Rôle de l'utilisateur

**Configuration Axios :**
```javascript
api.defaults.headers.common['Authorization'] = `Bearer ${token}`
```

### Communication WebSocket

**Fichier** : `hooks/useWebSocket.js`

**Fonctionnalités :**
- Connexion automatique au serveur WebSocket (`ws://localhost:8080/ws`)
- Abonnement aux topics STOMP
- Reconnexion automatique en cas de déconnexion
- Heartbeat pour maintenir la connexion
- Gestion des erreurs de connexion

**Utilisation :**
```javascript
const { connected } = useWebSocket('/topic/aircraft', (data) => {
  // Traiter les données reçues
})
```

**Topics utilisés :**
- `/topic/aircraft` - Positions de tous les avions
- `/topic/aircraft/{id}` - Mises à jour d'un avion spécifique
- `/topic/weather-alerts` - Alertes météo
- `/topic/conflicts` - Conflits détectés

### Service API

**Fichier** : `services/api.js`

**Configuration :**
- Base URL : `http://localhost:8080/api`
- Timeout : 10 secondes
- Headers : `Content-Type: application/json`
- Intercepteur pour gérer les erreurs de connexion

**Gestion des erreurs :**
- Erreurs réseau (backend non accessible) → Message clair
- Timeout → Avertissement
- Erreurs HTTP (4xx, 5xx) → Logging approprié
- Erreurs d'authentification (401) → Redirection vers login
- Erreurs 404 → Debug (normal pour certaines routes)

---

## 🔄 FLUX DE DONNÉES DÉTAILLÉS

### 1. Authentification

```
1. Utilisateur saisit username/password dans Login.jsx
2. Frontend → POST /api/auth/login
   Body: { "username": "pilote_cmn1", "password": "pilote123" }
3. Backend (AuthService) vérifie credentials
   - Hashage BCrypt du password
   - Vérification dans UserRepository
4. Backend génère JWT token avec claims (username, role)
5. Frontend reçoit { "token": "...", "role": "PILOTE" }
6. Frontend stocke token dans localStorage
7. Frontend ajoute token aux requêtes suivantes :
   api.defaults.headers.common['Authorization'] = `Bearer ${token}`
8. Frontend redirige selon le rôle :
   - ADMIN → /admin
   - PILOTE → /pilot
   - CENTRE_RADAR → /radar
```

### 2. Création d'un Vol

```
1. Admin crée un vol via AdminDashboard → FlightManagement.jsx
2. Formulaire collecte les données :
   - flightNumber, airline, aircraftId, departureAirportId, etc.
   - scheduledDeparture, scheduledArrival (format datetime-local)
3. Frontend → POST /api/flight/manage
   Headers: { "Authorization": "Bearer {token}" }
   Body: { "flightNumber": "AT1001", ... }
4. Backend (FlightController) reçoit la requête
5. Backend (FlightManagementService) :
   - Parse les dates (datetime-local → LocalDateTime)
   - Valide les données (aéroports, avion, pilote)
   - Crée l'entité Flight en base
   - Gère les transactions (@Transactional)
6. Backend retourne le vol créé
7. Frontend met à jour l'affichage
8. Si erreur : Backend retourne { "error": "...", "type": "...", "details": "..." }
```

### 3. Simulation d'un Vol

```
1. Pilote demande autorisation de décollage
   Frontend → POST /api/atc/request-takeoff-clearance
   Body: { "aircraftId": 1 }
2. Backend (ATCService) vérifie :
   - Météo de l'aéroport (visibilité, vent, précipitations)
   - Disponibilité de la piste
   - Statut de l'avion
3. Backend retourne { "status": "GRANTED|REFUSED|PENDING", "message": "..." }
4. Si GRANTED, pilote démarre le vol
   Frontend → POST /api/flight/simulate-takeoff
   Body: { "aircraftId": 1, "departureAirportId": 1, "arrivalAirportId": 2 }
5. Backend (FlightSimulationService) :
   - Calcule la trajectoire (formule de Haversine)
   - Démarre la simulation
   - Met à jour position toutes les 5s
6. Backend broadcast via WebSocket → /topic/aircraft/{id}
7. Frontend (PilotDashboard) reçoit les mises à jour en temps réel
8. Frontend met à jour la carte et les données (altitude, vitesse, cap)
```

### 4. Mises à Jour en Temps Réel

```
1. RealtimeUpdateService (@Scheduled toutes les 5s)
2. Récupère positions de tous les avions depuis AircraftRepository
3. Calcule nouvelles positions (si en vol)
4. Met à jour en base de données
5. Broadcast via WebSocket → /topic/aircraft
   Payload: [{ "id": 1, "lat": 33.5, "lon": -7.5, "altitude": 10000, ... }, ...]
6. Frontend (RadarDashboard) reçoit les mises à jour
7. Frontend met à jour la carte avec nouvelles positions
8. Frontend met à jour les listes d'avions
```

### 5. Détection de Conflits

```
1. ConflictDetectionService (@Scheduled toutes les 5s)
2. Récupère tous les avions en vol
3. Pour chaque paire d'avions :
   - Calcule distance horizontale
   - Vérifie différence d'altitude
   - Vérifie convergence des trajectoires
4. Si conflit détecté :
   - Crée alerte
   - Broadcast via WebSocket → /topic/conflicts
5. Frontend (RadarDashboard, AdminDashboard) reçoit les alertes
6. Frontend affiche les alertes visuellement
```

### 6. Dashboard Pilote

```
1. Frontend (PilotDashboard) charge
2. Frontend → GET /api/pilots/{username}/dashboard
   Headers: { "Authorization": "Bearer {token}" }
3. Backend (PilotDashboardService) :
   - Trouve le User par username
   - Trouve le Pilot associé
   - Trouve l'Aircraft assigné (ou lance exception si aucun)
   - Trouve le Flight actif
   - Récupère météo, communications, centre radar
   - Calcule KPIs
4. Backend retourne PilotDashboardDTO complet
5. Frontend affiche toutes les données
6. Si erreur 404 avec code NO_AIRCRAFT_ASSIGNED :
   Frontend affiche "NO AIRCRAFT ASSIGNED - Contact administrator"
```

---

## 🚀 DÉMARRAGE DU PROJET

### Prérequis

1. **Java 17** installé et configuré (`JAVA_HOME`)
2. **Node.js** (v16+) installé
3. **PostgreSQL** installé et démarré
4. **Maven** installé (ou utiliser le wrapper `mvnw`)

### Configuration Base de Données

**Fichier** : `backend/src/main/resources/application.properties`

```properties
# Base de données
spring.datasource.url=jdbc:postgresql://localhost:5432/flightradar
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# JWT
jwt.secret=flightradar-secret-key-2026-very-secure-key-for-jwt-token-generation
jwt.expiration=86400000

# WebSocket
spring.websocket.allowed-origins=http://localhost:3000,http://localhost:3001
```

**Créer la base de données :**
```sql
CREATE DATABASE flightradar;
```

**Initialisation automatique :**
- Les tables sont créées automatiquement par Hibernate (`spring.jpa.hibernate.ddl-auto=update`)
- Les données initiales sont créées par `DataInitializer` au premier démarrage :
  - Utilisateurs (admin, pilote_cmn1, radar_cmn)
  - Aéroports (CMN, RBA, RAK, TNG)
  - Pilotes
  - Avions
  - Centres radar

### Démarrer le Backend

```bash
cd RadarFlightPFA-main/backend
mvn spring-boot:run
```

Le backend démarre sur `http://localhost:8080`

**Vérification :**
- Ouvrir `http://localhost:8080/api/airports` (doit retourner la liste des aéroports)

### Démarrer le Frontend

```bash
cd RadarFlightPFA-main/frontend
npm install  # Si première fois
npm run dev
```

Le frontend démarre sur `http://localhost:3000` (ou 3001 si 3000 est occupé)

**Vérification :**
- Ouvrir `http://localhost:3000` (doit afficher la page de login)

### Comptes par Défaut

**Admin :**
- Username : `admin`
- Password : `admin123`
- Rôle : `ADMIN`

**Pilote :**
- Username : `pilote_cmn1`
- Password : `pilote123`
- Rôle : `PILOTE`

**Radar :**
- Username : `radar_cmn`
- Password : `radar123`
- Rôle : `CENTRE_RADAR`

**Note importante :** Si le pilote n'a pas d'avion assigné, exécuter le script SQL :
```bash
.\EXECUTER_ASSIGNATION_AVION.ps1
# ou
psql -U postgres -d flightradar -f ASSIGNER_AVION_RAPIDE.sql
```

---

## 📊 FONCTIONNALITÉS PRINCIPALES

### 1. Gestion des Vols
- **Création** : Formulaire complet avec validation
- **Modification** : Uniquement si vol non en cours
- **Suppression** : Uniquement si vol non en cours
- **Planification** : Départ, arrivée, aéroports, horaires
- **Suivi** : Temps réel des vols en cours
- **Simulation** : Automatique après autorisation de décollage

### 2. Suivi en Temps Réel
- **Positions GPS** : Latitude, longitude
- **Données de vol** : Altitude, vitesse, cap
- **Trajectoires** : Prévue et réelle
- **Mises à jour** : Toutes les 5 secondes
- **WebSocket** : Broadcast en temps réel

### 3. Communication ATC
- **Messages** : Entre pilotes et contrôleurs
- **Autorisations** : Décollage/atterrissage
- **Instructions** : De vol
- **Historique** : Toutes les communications

### 4. Météorologie
- **Données météo** : En temps réel (API Open-Meteo)
- **Alertes** : Météorologiques
- **Conditions** : Par aéroport
- **Mise à jour** : Automatique toutes les heures

### 5. Détection de Conflits
- **Détection automatique** : Toutes les 5 secondes
- **Critères** : Distance, altitude, trajectoires
- **Alertes** : En temps réel
- **Prévention** : Des collisions

### 6. Cartographie
- **Carte interactive** : Leaflet
- **Avions** : Affichage en temps réel
- **Trajectoires** : Des vols
- **Aéroports** : Et pistes

---

## 🔐 SÉCURITÉ

### Authentification
- **JWT** : JSON Web Tokens
- **Expiration** : 24 heures
- **Stockage** : Côté client (localStorage)
- **Header** : `Authorization: Bearer {token}`

### Autorisation
- **Rôles** : ADMIN, PILOTE, CENTRE_RADAR
- **Contrôle d'accès** : Par endpoint
- **Vérification** : Des permissions
- **Annotations** : `@PreAuthorize` sur les contrôleurs

### Mots de passe
- **Hashage** : BCrypt
- **Stockage** : Jamais en clair
- **Force** : Minimum 6 caractères (recommandé)

### CORS
- **Origines autorisées** : `http://localhost:3000`, `http://localhost:3001`
- **Méthodes** : GET, POST, PUT, DELETE, OPTIONS
- **Headers** : Tous autorisés
- **Credentials** : Autorisés

---

## 📡 API REST - Référence Complète

### Base URL
```
http://localhost:8080/api
```

### Format des Réponses

**Succès :**
```json
{
  "success": true,
  "data": { ... }
}
```

**Erreur :**
```json
{
  "error": "Message d'erreur",
  "type": "ERROR_TYPE",
  "details": "Détails techniques (optionnel)"
}
```

**Codes d'erreur :**
- `VALIDATION_ERROR` : Erreur de validation
- `DATA_INTEGRITY_ERROR` : Erreur de contrainte DB
- `RUNTIME_ERROR` : Erreur runtime
- `NO_AIRCRAFT_ASSIGNED` : Aucun avion assigné au pilote
- `UNKNOWN_ERROR` : Erreur inconnue

### Authentification

Toutes les requêtes (sauf `/api/auth/login`) nécessitent le header :
```
Authorization: Bearer {token}
```

### Endpoints Principaux

Voir section "Contrôleurs REST - Endpoints Complets" ci-dessus.

---

## 🔌 WEBSOCKET

### Connexion

**Endpoint** : `ws://localhost:8080/ws`

**Protocole** : STOMP over SockJS

**Client JavaScript :**
```javascript
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

const socket = new SockJS('http://localhost:8080/ws')
const client = new Client({
  webSocketFactory: () => socket,
  reconnectDelay: 5000,
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000
})

client.activate()
```

### Topics Disponibles

1. **`/topic/aircraft`**
   - Broadcast toutes les 5 secondes
   - Liste de tous les avions avec positions
   - Payload : `[{ "id": 1, "lat": 33.5, "lon": -7.5, "altitude": 10000, ... }, ...]`

2. **`/topic/aircraft/{id}`**
   - Mises à jour d'un avion spécifique
   - Payload : `{ "id": 1, "lat": 33.5, "lon": -7.5, "altitude": 10000, ... }`

3. **`/topic/weather-alerts`**
   - Alertes météorologiques
   - Payload : `{ "airportId": 1, "alert": "Strong winds", ... }`

4. **`/topic/conflicts`**
   - Alertes de conflit entre avions
   - Payload : `{ "aircraft1": 1, "aircraft2": 2, "distance": 4.5, ... }`

### Abonnement

```javascript
client.subscribe('/topic/aircraft', (message) => {
  const data = JSON.parse(message.body)
  // Traiter les données
})
```

---

## 🛠️ DÉPANNAGE

### Problèmes Courants

#### 1. Erreur "NO AIRCRAFT ASSIGNED"
**Symptôme** : Le dashboard pilote affiche "NO AIRCRAFT ASSIGNED"

**Solution** : Exécuter le script SQL pour assigner un avion :
```bash
.\EXECUTER_ASSIGNATION_AVION.ps1
# ou
psql -U postgres -d flightradar -f ASSIGNER_AVION_RAPIDE.sql
```

#### 2. Erreur 404 sur `/api/pilots/{username}/dashboard`
**Symptôme** : Erreur 404 avec message "Aucun avion assigné au pilote"

**Solution** : Vérifier que le pilote a un avion assigné dans la base de données :
```sql
SELECT u.username, p.name, a.registration 
FROM users u
LEFT JOIN pilots p ON p.user_id = u.id
LEFT JOIN aircraft a ON a.pilot_id = p.id
WHERE u.username = 'pilote_cmn1';
```

#### 3. Erreur 403 sur les endpoints
**Symptôme** : Erreur 403 Forbidden

**Solution** : Vérifier que :
- Le token JWT est présent dans localStorage
- Le token est envoyé dans le header `Authorization: Bearer {token}`
- Le token n'est pas expiré (24h)
- L'utilisateur a le bon rôle

#### 4. Erreur de création de vol
**Symptôme** : "Erreur de base de données. Vérifiez que les colonnes existent"

**Solution** : Exécuter les scripts de migration SQL :
```bash
psql -U postgres -d flightradar -f backend/database/VERIFIER_ET_CORRIGER_FLIGHTS.sql
```

#### 5. Erreur "Query did not return a unique result"
**Symptôme** : Erreur lors de la récupération du centre radar

**Solution** : Le code gère maintenant les doublons automatiquement (prend le premier). Si le problème persiste :
```sql
-- Vérifier les doublons
SELECT airport_id, COUNT(*) 
FROM radar_centers 
GROUP BY airport_id 
HAVING COUNT(*) > 1;
```

### Logs

**Backend** : Les logs sont affichés dans la console. Niveau : INFO, WARN, ERROR

**Frontend** : Les logs sont dans la console du navigateur (F12)

---

## 🎯 POINTS CLÉS DE L'ARCHITECTURE

1. **Séparation des responsabilités** : Backend (logique métier), Frontend (présentation)
2. **Temps réel** : WebSocket pour les mises à jour instantanées
3. **Sécurité** : JWT pour l'authentification, Spring Security pour l'autorisation
4. **Scalabilité** : Architecture modulaire, services indépendants
5. **Maintenabilité** : Code structuré, documentation complète
6. **Gestion d'erreur** : Messages clairs, codes d'erreur structurés
7. **Transactions** : Gestion des transactions avec `@Transactional`
8. **Validation** : Validation des données côté backend

---

## 📝 NOTES IMPORTANTES

- Le backend doit être démarré avant le frontend
- PostgreSQL doit être démarré avant le backend
- Les ports 8080 (backend) et 3000/3001 (frontend) doivent être libres
- Les données initiales sont créées automatiquement au premier démarrage
- Les simulations de vol sont automatiques après autorisation de décollage
- Les mises à jour en temps réel sont envoyées toutes les 5 secondes
- Les dates doivent être au format `YYYY-MM-DDTHH:mm` (datetime-local) ou `YYYY-MM-DDTHH:mm:ss`
- Les pilotes doivent avoir un avion assigné pour accéder au dashboard

---

## 📚 RESSOURCES

### Documentation Technique
- Spring Boot : https://spring.io/projects/spring-boot
- React : https://react.dev
- PostgreSQL : https://www.postgresql.org/docs/
- Leaflet : https://leafletjs.com
- WebSocket STOMP : https://stomp.github.io

### Scripts SQL
- Voir dossier `backend/database/`
- Scripts PowerShell : `*.ps1` à la racine

### Fichiers de Configuration
- `backend/src/main/resources/application.properties`
- `frontend/vite.config.js`
- `frontend/package.json`

---

**Document créé le** : 2025-01-27  
**Dernière mise à jour** : 2025-01-27  
**Version du projet** : 1.0.0  
**Auteur** : Équipe RadarFlight
