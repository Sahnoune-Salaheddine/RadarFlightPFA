# 📋 DÉFINITION COMPLÈTE DU PROJET - Flight Radar 2026

## 🎯 1. VUE D'ENSEMBLE

### Description du Projet

**Flight Radar 2026** est une application web complète de suivi aérien en temps réel, similaire à Flightradar24, développée dans le cadre d'un Projet de Fin d'Année (PFA). L'application permet de :

- Suivre les avions en temps réel avec leurs positions GPS, altitude, vitesse et cap
- Gérer les communications entre centres radar et pilotes (ATC)
- Surveiller les conditions météorologiques pour chaque aéroport
- Détecter les conflits potentiels entre avions
- Fournir des dashboards spécialisés pour 3 types d'utilisateurs : ADMIN, CENTRE_RADAR, et PILOTE

### Objectif Principal

**Améliorer la sécurité aérienne** en fournissant un système de suivi et de communication en temps réel pour les opérations aériennes au Maroc.

### Contexte

- **Type** : Projet de Fin d'Année (PFA) - 2026
- **Domaine** : Aéronautique / Système de gestion du trafic aérien
- **Portée** : Application web complète avec backend et frontend

---

## 🏗️ 2. ARCHITECTURE GÉNÉRALE

### Type d'Architecture

**Architecture Monolithique Modulaire** (prête pour migration vers microservices)

```
┌─────────────────────────────────────────┐
│         Frontend (React 18)            │
│  - Pages (Admin, Radar, Pilot)        │
│  - Composants réutilisables            │
│  - Services API                         │
│  - WebSocket Client                     │
└──────────────┬──────────────────────────┘
               │ HTTP REST + WebSocket
┌──────────────▼──────────────────────────┐
│      Backend (Spring Boot 3.2.0)        │
│  ┌────────────────────────────────────┐ │
│  │ Controllers (REST API)             │ │
│  └──────────┬─────────────────────────┘ │
│  ┌──────────▼─────────────────────────┐ │
│  │ Services (Business Logic)          │ │
│  └──────────┬─────────────────────────┘ │
│  ┌──────────▼─────────────────────────┐ │
│  │ Repositories (Data Access)         │ │
│  └──────────┬─────────────────────────┘ │
└──────────────┬──────────────────────────┘
               │ JPA/Hibernate
┌──────────────▼──────────────────────────┐
│      Database (PostgreSQL)              │
└─────────────────────────────────────────┘
```

### Stack Technologique

#### Backend
- **Framework** : Spring Boot 3.2.0
- **Langage** : Java 17
- **ORM** : JPA / Hibernate 6.3.1
- **Base de données** : PostgreSQL 14+
- **Sécurité** : Spring Security + JWT (jjwt 0.12.3)
- **Temps réel** : WebSocket (Spring WebSocket)
- **HTTP Client** : WebFlux (pour APIs externes)
- **Build** : Maven 3.8+
- **Outils** : Lombok (réduction de code boilerplate)

#### Frontend
- **Framework** : React 18.2.0
- **Build Tool** : Vite 5.0.8
- **Styling** : Tailwind CSS 3.3.6
- **Cartes** : Leaflet 1.9.4 + React-Leaflet 4.2.1
- **Graphiques** : Chart.js 4.5.1 + React-Chartjs-2 5.3.1
- **HTTP Client** : Axios 1.6.2
- **WebSocket** : Socket.io-client 4.5.4
- **Routing** : React Router DOM 6.20.0
- **UI Components** : Headless UI 1.7.17 + Heroicons 2.1.1

#### APIs Externes
- **Météo** : Open-Meteo API (gratuite, pas de clé API requise)
- **Avions Live** : OpenSky Network API (optionnel)

---

## 📁 3. STRUCTURE DU PROJET

### Organisation des Fichiers

```
RadarFlightPFA-main/
├── backend/
│   ├── src/main/java/com/flightradar/
│   │   ├── FlightRadarApplication.java    # Point d'entrée Spring Boot
│   │   ├── config/                        # Configuration
│   │   │   ├── DataInitializer.java       # Initialisation données
│   │   │   ├── SecurityConfig.java        # Configuration sécurité
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── WebSocketConfig.java
│   │   │   └── RestTemplateConfig.java
│   │   ├── controller/                    # Contrôleurs REST (11)
│   │   │   ├── AdminDashboardController.java
│   │   │   ├── AircraftController.java
│   │   │   ├── AirportController.java
│   │   │   ├── ATCController.java
│   │   │   ├── AuthController.java
│   │   │   ├── ConflictController.java
│   │   │   ├── FlightController.java
│   │   │   ├── PilotDashboardController.java
│   │   │   ├── RadarController.java
│   │   │   ├── RadarDashboardController.java
│   │   │   ├── RunwayController.java
│   │   │   └── WeatherController.java
│   │   ├── service/                        # Services métier (14)
│   │   │   ├── AdminDashboardService.java
│   │   │   ├── AircraftService.java
│   │   │   ├── ATCService.java
│   │   │   ├── AuthService.java
│   │   │   ├── ConflictDetectionService.java
│   │   │   ├── FlightService.java
│   │   │   ├── JwtService.java
│   │   │   ├── OpenSkyService.java
│   │   │   ├── OpenSkyMapper.java
│   │   │   ├── PilotDashboardService.java
│   │   │   ├── RadarDashboardService.java
│   │   │   ├── RadarService.java
│   │   │   ├── RealtimeUpdateService.java
│   │   │   └── WeatherService.java
│   │   ├── repository/                    # Repositories JPA (11)
│   │   │   ├── AircraftRepository.java
│   │   │   ├── AirportRepository.java
│   │   │   ├── ATCMessageRepository.java
│   │   │   ├── ATISDataRepository.java
│   │   │   ├── CommunicationRepository.java
│   │   │   ├── FlightRepository.java
│   │   │   ├── PilotRepository.java
│   │   │   ├── RadarCenterRepository.java
│   │   │   ├── RunwayRepository.java
│   │   │   ├── UserRepository.java
│   │   │   └── WeatherDataRepository.java
│   │   ├── model/                         # Entités JPA (13)
│   │   │   ├── Aircraft.java
│   │   │   ├── AircraftStatus.java
│   │   │   ├── Airport.java
│   │   │   ├── ATCMessage.java
│   │   │   ├── ATISData.java
│   │   │   ├── Communication.java
│   │   │   ├── Flight.java
│   │   │   ├── FlightStatus.java
│   │   │   ├── Pilot.java
│   │   │   ├── RadarCenter.java
│   │   │   ├── ReceiverType.java
│   │   │   ├── Role.java
│   │   │   ├── Runway.java
│   │   │   ├── SenderType.java
│   │   │   ├── User.java
│   │   │   ├── WeatherData.java
│   │   │   └── dto/                       # DTOs
│   │   │       ├── LiveAircraft.java
│   │   │       ├── OpenSkyResponse.java
│   │   │       ├── PilotDashboardDTO.java
│   │   │       ├── TakeoffClearanceRequestDTO.java
│   │   │       └── TakeoffClearanceResponseDTO.java
│   │   └── resources/
│   │       └── application.properties     # Configuration
│   ├── database/                          # Scripts SQL
│   │   ├── schema.sql
│   │   ├── schema_complete.sql
│   │   ├── seed_data.sql
│   │   └── init.sql
│   └── pom.xml                            # Dépendances Maven
│
├── frontend/
│   ├── src/
│   │   ├── App.jsx                        # Composant principal
│   │   ├── main.jsx                       # Point d'entrée
│   │   ├── index.css                      # Styles globaux
│   │   ├── pages/                         # Pages principales
│   │   │   ├── AdminDashboard.jsx
│   │   │   ├── PilotDashboard.jsx
│   │   │   └── RadarDashboard.jsx
│   │   ├── components/                    # Composants réutilisables
│   │   │   ├── AircraftList.jsx
│   │   │   ├── AlertPanel.jsx
│   │   │   ├── CommunicationPanel.jsx
│   │   │   ├── Dashboard.jsx
│   │   │   ├── FlightMap.jsx
│   │   │   ├── Login.jsx
│   │   │   └── WeatherPanel.jsx
│   │   ├── context/                       # Context React
│   │   │   └── AuthContext.jsx
│   │   ├── hooks/                         # Hooks personnalisés
│   │   │   └── useWebSocket.js
│   │   └── services/                      # Services API
│   │       └── api.js
│   ├── package.json                       # Dépendances npm
│   ├── vite.config.js                     # Configuration Vite
│   ├── tailwind.config.js                 # Configuration Tailwind
│   └── postcss.config.js                  # Configuration PostCSS
│
└── documentation/                          # Documentation (100+ fichiers)
    ├── README.md
    ├── ARCHITECTURE.md
    ├── GUIDE_UTILISATION_COMPLET.md
    └── ... (autres fichiers de documentation)
```

---

## 🔐 4. SYSTÈME D'AUTHENTIFICATION

### Rôles Utilisateurs

1. **ADMIN**
   - Accès complet à tous les dashboards
   - Gestion des utilisateurs (création, modification, suppression)
   - Visualisation de tous les KPIs et statistiques
   - Accès à toutes les données

2. **CENTRE_RADAR**
   - Accès au dashboard radar uniquement
   - Visualisation des avions dans le secteur (50 km)
   - Gestion des communications ATC
   - Réception des demandes d'autorisation de décollage
   - Consultation des données ATIS

3. **PILOTE**
   - Accès au dashboard pilote uniquement
   - Visualisation de son avion assigné
   - Consultation des communications ATC
   - Demande d'autorisation de décollage
   - Consultation de la météo du vol

### Mécanisme d'Authentification

- **Type** : JWT (JSON Web Tokens)
- **Durée de validité** : 24 heures (86400000 ms)
- **Algorithme** : HS256 (HMAC-SHA256)
- **Stockage** : LocalStorage côté frontend
- **Hash des mots de passe** : BCrypt

### Endpoints d'Authentification

- `POST /api/auth/login` - Connexion (public)
- `POST /api/auth/register` - Création de compte (ADMIN uniquement)
- `GET /api/auth/users` - Liste des utilisateurs (ADMIN)
- `PUT /api/auth/users/{id}` - Modification utilisateur (ADMIN)
- `DELETE /api/auth/users/{id}` - Suppression utilisateur (ADMIN)

### Comptes par Défaut

Créés automatiquement par `DataInitializer` au démarrage :

- **Admin** : `admin` / `admin123`
- **Radar CMN** : `radar_cmn` / `radar123`
- **Radar RBA** : `radar_rba` / `radar123`
- **Radar RAK** : `radar_rak` / `radar123`
- **Radar TNG** : `radar_tng` / `radar123`
- **Pilote CMN1** : `pilote_cmn1` / `pilote123`
- **Pilote CMN2** : `pilote_cmn2` / `pilote123`
- (et autres pilotes pour chaque aéroport)

---

## 📊 5. DASHBOARDS

### 5.1 Dashboard ADMIN

**URL** : `/admin`

**Fonctionnalités** :
- **KPIs Aéronautiques** :
  - Nombre total d'avions en vol
  - Nombre de vols actifs
  - Retards moyens
  - Taux de ponctualité
  - Conflits détectés
  - Alertes météo actives
- **Graphiques** :
  - Trafic par aéroport
  - Évolution du trafic dans le temps
  - Répartition des retards
  - Performance ATC
- **Statistiques** :
  - Nombre de communications ATC
  - Temps de réponse moyen ATC
  - DMAN (Target Takeoff Time)
- **Gestion** :
  - Liste des utilisateurs
  - Création/modification de comptes

**Endpoint Backend** : `GET /api/admin/dashboard`

### 5.2 Dashboard CENTRE RADAR

**URL** : `/radar`

**Fonctionnalités** :
- **Carte Radar Interactive** :
  - Affichage des avions dans un rayon de 50 km
  - Positions en temps réel (latitude, longitude)
  - Altitude, vitesse, cap de chaque avion
  - Trajectoires prévues
- **Données ATIS** :
  - Conditions météorologiques de l'aéroport
  - Vent (vitesse et direction)
  - Visibilité
  - Pression atmosphérique
  - Température
- **Communications ATC** :
  - Historique des communications
  - Messages en temps réel
  - Demandes d'autorisation de décollage
- **Détection de Conflits** :
  - Alertes si deux avions sont trop proches
  - Calcul de distance minimale
  - Suggestions de résolution

**Endpoints Backend** :
- `GET /api/radar/dashboard` - Dashboard complet
- `GET /api/radar/dashboard/aircraft` - Avions dans le secteur
- `GET /api/radar/dashboard/atis` - Données ATIS

### 5.3 Dashboard PILOTE

**URL** : `/pilot`

**Fonctionnalités** :
- **Informations Générales du Vol** :
  - Numéro de vol
  - Compagnie aérienne
  - Type d'avion
  - Route : Aéroport départ → Aéroport arrivée
- **Position & Mouvement (ADS-B)** :
  - Carte interactive avec position actuelle
  - Latitude / Longitude
  - Altitude (en mètres et pieds)
  - Vitesse sol (ground speed)
  - Vitesse air (air speed)
  - Cap (heading en degrés)
  - Taux de montée/descente (vertical speed)
  - Code transpondeur
  - Trajectoire en temps réel
- **Statut du Vol** :
  - Statut : Au sol / Décollage / En vol / Atterrissage
  - Heure réelle de départ / arrivée
  - Heure prévue de départ / arrivée
  - Retards éventuels
  - Porte / piste associée
- **Météo du Vol** :
  - Vent (vitesse et direction)
  - Visibilité
  - Précipitations
  - Turbulence
  - Température
  - Pression
  - Alertes météo
- **Communications ATC** :
  - Dernier message ATC
  - Instructions en cours
  - Centre radar responsable
  - Historique des communications
- **Sécurité / Suivi ADS-B** :
  - Code transpondeur
  - Trajectoire en temps réel
  - Alertes techniques ou météo
  - Niveau de risque
- **KPIs Temps Réel** :
  - Distance restante
  - ETA (Estimated Time of Arrival)
  - Consommation carburant estimée
  - Niveau de carburant
  - Vitesse moyenne
  - Altitude stable
  - Turbulence détectée
  - Sévérité météo
  - Indice de risque de trajectoire
  - Densité de trafic dans 30 km
  - Score d'état avion
- **Demande d'Autorisation de Décollage** :
  - Bouton pour demander l'autorisation
  - Statut : GRANTED / REFUSED / PENDING

**Endpoints Backend** :
- `GET /api/pilots/{username}/dashboard` - Dashboard complet
- `POST /api/atc/request-takeoff-clearance` - Demander autorisation décollage

---

## 🗄️ 6. MODÈLE DE DONNÉES

### 6.1 Entités Principales (13 entités)

#### 1. User (Utilisateur)
- **Table** : `users`
- **Champs** : id, username, password, role, airport_id, pilot_id
- **Relations** : 
  - Peut être lié à un aéroport (si CENTRE_RADAR)
  - Peut être lié à un pilote (si PILOTE)

#### 2. Airport (Aéroport)
- **Table** : `airports`
- **Champs** : id, name, city, code_iata, latitude, longitude
- **Relations** :
  - 1→N Runways
  - 1→N Aircraft
  - 1→1 RadarCenter
  - 1→N WeatherData
  - 1→N Flights (départ)
  - 1→N Flights (arrivée)

#### 3. Runway (Piste)
- **Table** : `runways`
- **Champs** : id, name, orientation, length_meters, width_meters, airport_id
- **Relations** : N→1 Airport

#### 4. Pilot (Pilote)
- **Table** : `pilots`
- **Champs** : id, name, license, experience_years, user_id, assigned_aircraft_id, first_name, last_name
- **Relations** :
  - N→1 User
  - 1→N Aircraft

#### 5. Aircraft (Avion)
- **Table** : `aircraft`
- **Champs** : id, model, registration, status, airport_id, pilot_id, position_lat, position_lon, altitude, speed, heading, air_speed, vertical_speed, transponder_code, username_pilote, last_update
- **Relations** :
  - N→1 Airport
  - N→1 Pilot
  - 1→N Flights

#### 6. Flight (Vol)
- **Table** : `flights`
- **Champs** : id, flight_number, airline, aircraft_id, departure_airport_id, arrival_airport_id, flight_status, scheduled_departure, scheduled_arrival, actual_departure, actual_arrival, created_at
- **Relations** :
  - N→1 Aircraft
  - N→1 Airport (départ)
  - N→1 Airport (arrivée)

#### 7. RadarCenter (Centre Radar)
- **Table** : `radar_centers`
- **Champs** : id, name, code, frequency, airport_id, user_id
- **Relations** :
  - 1→1 Airport
  - N→1 User

#### 8. WeatherData (Données Météo)
- **Table** : `weather_data`
- **Champs** : id, airport_id, timestamp, temperature, wind_speed, wind_direction, visibility, pressure, conditions, crosswind, alert
- **Relations** : N→1 Airport

#### 9. Communication (Communication VHF)
- **Table** : `communications`
- **Champs** : id, sender_type, sender_id, receiver_type, receiver_id, message, frequency, timestamp
- **Relations** : Polymorphe (sender/receiver peuvent être RADAR, AIRCRAFT, ou AIRPORT)

#### 10. ATCMessage (Message ATC)
- **Table** : `atc_messages`
- **Champs** : id, aircraft_id, radar_center_id, message_type, message, timestamp, status
- **Relations** :
  - N→1 Aircraft
  - N→1 RadarCenter

#### 11. ATISData (Données ATIS)
- **Table** : `atis_data`
- **Champs** : id, airport_id, timestamp, temperature, pression, vent, visibilité
- **Relations** : N→1 Airport

#### 12. Role (Énumération)
- **Valeurs** : ADMIN, CENTRE_RADAR, PILOTE

#### 13. AircraftStatus (Énumération)
- **Valeurs** : AU_SOL, DECOLLAGE, EN_VOL, ATTERRISSAGE, EN_ATTENTE

### 6.2 Relations Principales

```
User ──┐
       ├──→ Pilot ──→ Aircraft ──→ Flight
       │                    │
       └──→ RadarCenter ────┼──→ Airport ──→ Runway
                            │         │
                            │         └──→ WeatherData
                            │         └──→ ATISData
                            │
                            └──→ Communication (polymorphe)
```

---

## 🔄 7. FLUX DE DONNÉES

### 7.1 Flux d'Authentification

```
1. Client → POST /api/auth/login {username, password}
2. Backend → AuthService.authenticate()
3. Backend → Vérification credentials (BCrypt)
4. Backend → Génération JWT (JwtService)
5. Backend → Retour {token, role}
6. Frontend → Stockage token dans LocalStorage
7. Frontend → Ajout header Authorization: Bearer {token}
```

### 7.2 Flux Dashboard Pilote

```
1. Frontend → GET /api/pilots/{username}/dashboard
2. Backend → PilotDashboardService.getPilotDashboard()
3. Backend → Recherche User par username
4. Backend → Recherche Pilot par userId
5. Backend → Recherche Aircraft par pilotId
6. Backend → Recherche Flight actif
7. Backend → Récupération WeatherData
8. Backend → Récupération Communications
9. Backend → Calcul KPIs
10. Backend → Construction PilotDashboardDTO
11. Backend → Retour JSON complet
12. Frontend → Affichage dashboard
```

### 7.3 Flux Demande Autorisation Décollage

```
1. Frontend → POST /api/atc/request-takeoff-clearance {aircraftId}
2. Backend → ATCService.processTakeoffRequest()
3. Backend → Vérification conditions (météo, trafic, piste)
4. Backend → Création ATCMessage
5. Backend → Notification via WebSocket
6. Backend → Retour {status: GRANTED/REFUSED/PENDING, message}
7. Frontend → Affichage statut
```

### 7.4 Flux Mise à Jour Temps Réel

```
1. Backend → RealtimeUpdateService (scheduled task)
2. Backend → Mise à jour positions avions
3. Backend → Détection conflits
4. Backend → WebSocket → Broadcast /topic/aircraft
5. Frontend → Réception via WebSocket
6. Frontend → Mise à jour carte et données
```

### 7.5 Flux Récupération Météo

```
1. Frontend → GET /api/weather/airport/{id}
2. Backend → WeatherService.getWeatherForAirport()
3. Backend → Vérification cache (dernière mise à jour < 1h)
4. Si cache expiré :
   - Backend → Appel Open-Meteo API
   - Backend → Parsing réponse
   - Backend → Sauvegarde WeatherData
5. Backend → Retour WeatherData
6. Frontend → Affichage météo
```

### 7.6 Flux Détection Conflits

```
1. Backend → ConflictDetectionService (scheduled task)
2. Backend → Récupération tous avions en vol
3. Backend → Calcul distances entre avions
4. Backend → Détection si distance < seuil (ex: 5 km)
5. Backend → Création alerte
6. Backend → WebSocket → Broadcast /topic/conflicts
7. Frontend → Affichage alerte
```

---

## 🔧 8. SERVICES BACKEND

### Liste des Services (14 services)

1. **AdminDashboardService**
   - Calcul des KPIs aéronautiques
   - Statistiques de trafic
   - Performance ATC
   - DMAN (Target Takeoff Time)

2. **AircraftService**
   - Gestion des avions
   - Recherche par pilote
   - Mise à jour positions
   - Intégration OpenSky Network

3. **ATCService**
   - Traitement des demandes d'autorisation
   - Vérification conditions décollage
   - Gestion des messages ATC

4. **AuthService**
   - Authentification utilisateurs
   - Génération JWT
   - Gestion des rôles

5. **ConflictDetectionService**
   - Détection de conflits entre avions
   - Calcul de distances
   - Génération d'alertes

6. **FlightService**
   - Gestion des vols
   - Recherche vols actifs
   - Calcul retards

7. **JwtService**
   - Génération tokens JWT
   - Validation tokens
   - Extraction claims

8. **OpenSkyService**
   - Intégration API OpenSky Network
   - Récupération avions en temps réel
   - Mapping données externes

9. **OpenSkyMapper**
   - Conversion données OpenSky → Aircraft
   - Mapping des champs

10. **PilotDashboardService**
    - Construction dashboard pilote complet
    - Agrégation données (avion, vol, météo, ATC, KPIs)

11. **RadarDashboardService**
    - Construction dashboard radar
    - Filtrage avions dans secteur (50 km)
    - Données ATIS

12. **RadarService**
    - Gestion communications radar
    - Historique communications

13. **RealtimeUpdateService**
    - Mises à jour positions en temps réel
    - Broadcast WebSocket
    - Scheduled tasks

14. **WeatherService**
    - Récupération données météo
    - Intégration Open-Meteo API
    - Cache des données
    - Calcul vent de travers

---

## 🌐 9. CONTRÔLEURS REST

### Liste des Contrôleurs (11 contrôleurs)

1. **AdminDashboardController**
   - `GET /api/admin/dashboard` - Dashboard complet
   - `GET /api/admin/kpis` - KPIs uniquement

2. **AircraftController**
   - `GET /api/aircraft` - Liste tous avions
   - `GET /api/aircraft/{id}` - Avion par ID
   - `GET /api/aircraft/airport/{airportId}` - Avions par aéroport
   - `GET /api/aircraft/in-flight` - Avions en vol
   - `GET /api/aircraft/live` - Avions live (OpenSky)
   - `GET /api/aircraft/live/{icao24}` - Avion live spécifique
   - `GET /api/aircraft/pilot/{username}` - Avion par pilote

3. **AirportController**
   - `GET /api/airports` - Liste tous aéroports
   - `GET /api/airports/{id}` - Aéroport par ID
   - `GET /api/airports/code/{codeIATA}` - Aéroport par code IATA
   - `GET /api/airports/{id}/weather` - Météo aéroport

4. **ATCController**
   - `POST /api/atc/request-takeoff-clearance` - Demande autorisation décollage
   - `GET /api/atc/messages/{aircraftId}` - Messages ATC pour avion

5. **AuthController**
   - `POST /api/auth/login` - Connexion
   - `POST /api/auth/register` - Création compte (ADMIN)
   - `GET /api/auth/users` - Liste utilisateurs (ADMIN)
   - `PUT /api/auth/users/{id}` - Modification utilisateur (ADMIN)
   - `DELETE /api/auth/users/{id}` - Suppression utilisateur (ADMIN)

6. **ConflictController**
   - `GET /api/conflicts` - Liste conflits détectés
   - `GET /api/conflicts/active` - Conflits actifs

7. **FlightController**
   - `GET /api/flights` - Liste tous vols
   - `GET /api/flights/{id}` - Vol par ID
   - `GET /api/flights/active` - Vols actifs
   - `POST /api/flights` - Création vol

8. **PilotDashboardController**
   - `GET /api/pilots/{username}/dashboard` - Dashboard complet
   - `GET /api/pilots/{username}/aircraft` - Avion du pilote

9. **RadarController**
   - `GET /api/radar/centers` - Liste centres radar
   - `GET /api/radar/communications` - Communications

10. **RadarDashboardController**
    - `GET /api/radar/dashboard` - Dashboard complet
    - `GET /api/radar/dashboard/aircraft` - Avions dans secteur
    - `GET /api/radar/dashboard/atis` - Données ATIS

11. **RunwayController**
    - `GET /api/runways` - Liste pistes
    - `GET /api/runways/{id}` - Piste par ID
    - `GET /api/runways/airport/{airportId}` - Pistes par aéroport

12. **WeatherController**
    - `GET /api/weather/airport/{id}` - Météo aéroport
    - `GET /api/weather/alerts` - Alertes météo

---

## 🔌 10. WEBSOCKETS

### Configuration

- **Endpoint** : `/ws`
- **Protocol** : STOMP over WebSocket
- **Configuration** : `WebSocketConfig.java`

### Topics Disponibles

1. **`/topic/aircraft`**
   - Mises à jour positions avions
   - Broadcast toutes les 5 secondes
   - Payload : Liste des avions avec positions

2. **`/topic/weather-alerts`**
   - Alertes météorologiques
   - Broadcast en temps réel
   - Payload : Données d'alerte

3. **`/topic/conflicts`**
   - Conflits détectés entre avions
   - Broadcast en temps réel
   - Payload : Informations de conflit

4. **`/topic/atc-messages`**
   - Messages ATC en temps réel
   - Broadcast immédiat
   - Payload : Message ATC

### Service RealtimeUpdateService

- **Fréquence** : Mise à jour toutes les 5 secondes
- **Tâches** :
  - Mise à jour positions avions
  - Calcul nouvelles positions (simulation)
  - Détection conflits
  - Broadcast via WebSocket

---

## 🔒 11. SÉCURITÉ

### Configuration Spring Security

- **Fichier** : `SecurityConfig.java`
- **Filtre JWT** : `JwtAuthenticationFilter`
- **Session** : STATELESS (JWT uniquement)
- **CSRF** : Désactivé (API REST)
- **CORS** : Configuré pour localhost:3000 et localhost:3001

### Protection par Rôle

```java
// Endpoints publics
/api/auth/login → permitAll()

// Endpoints ADMIN
/api/admin/** → hasRole("ADMIN")

// Endpoints RADAR
/api/radar/** → hasAnyRole("CENTRE_RADAR", "ADMIN")

// Endpoints PILOTE
/api/pilots/** → hasAnyRole("PILOTE", "ADMIN")
/api/atc/** → hasAnyRole("PILOTE", "CENTRE_RADAR", "ADMIN")

// Endpoints publics (lecture seule)
/api/airports/** → permitAll()
/api/aircraft/** → permitAll()
/api/weather/** → permitAll()
```

### JWT Configuration

- **Secret** : `flightradar-secret-key-2026-very-secure-key-for-jwt-token-generation`
- **Expiration** : 86400000 ms (24 heures)
- **Algorithme** : HS256
- **Claims** : username, role

---

## 🌍 12. API EXTERNE

### Open-Meteo API

- **URL** : `https://api.open-meteo.com/v1/forecast`
- **Type** : Gratuite, pas de clé API requise
- **Usage** : Données météorologiques pour aéroports
- **Service** : `WeatherService`
- **Cache** : 1 heure (évite trop d'appels API)
- **Données récupérées** :
  - Température
  - Vitesse du vent
  - Direction du vent
  - Visibilité
  - Pression atmosphérique
  - Conditions météo

### OpenSky Network API (Optionnel)

- **URL** : `https://opensky-network.org/api`
- **Usage** : Avions en temps réel (optionnel)
- **Service** : `OpenSkyService`
- **Mapper** : `OpenSkyMapper`

---

## 💾 13. BASE DE DONNÉES

### PostgreSQL

- **Version** : 14+
- **Nom de la base** : `flightradar`
- **Port** : 5432
- **Configuration** : `application.properties`

### Tables Principales

1. `users` - Utilisateurs
2. `airports` - Aéroports
3. `runways` - Pistes
4. `pilots` - Pilotes
5. `aircraft` - Avions
6. `flights` - Vols
7. `radar_centers` - Centres radar
8. `weather_data` - Données météo
9. `communications` - Communications VHF
10. `atc_messages` - Messages ATC
11. `atis_data` - Données ATIS

### Configuration JPA

- **DDL Auto** : `update` (création automatique des tables)
- **Show SQL** : `true` (développement)
- **Dialect** : PostgreSQLDialect
- **Format SQL** : `true`

### Initialisation des Données

- **Classe** : `DataInitializer.java`
- **Déclenchement** : Au démarrage si base vide
- **Données créées** :
  - 4 aéroports marocains (CMN, RBA, RAK, TNG)
  - 2 pistes par aéroport
  - 4 centres radar (1 par aéroport)
  - 8 avions (2 par aéroport)
  - 8 pilotes (1 par avion)
  - 1 utilisateur admin
  - Utilisateurs radar et pilotes

---

## 📈 14. ÉTAT ACTUEL DU PROJET

### Progression : ~75% Complété

#### ✅ Fonctionnalités Implémentées

- [x] Architecture backend complète
- [x] Authentification JWT
- [x] 3 dashboards (Admin, Radar, Pilot)
- [x] Modèle de données complet (13 entités)
- [x] API REST complète (11 contrôleurs)
- [x] Services métier (14 services)
- [x] WebSocket pour temps réel
- [x] Intégration Open-Meteo API
- [x] Détection de conflits
- [x] Frontend React complet
- [x] Base de données PostgreSQL
- [x] Documentation complète

#### ⚠️ Fonctionnalités Partielles

- [~] Dashboard Admin (KPIs de base implémentés, graphiques à améliorer)
- [~] Dashboard Radar (carte fonctionnelle, détection conflits basique)
- [~] Dashboard Pilot (données complètes, quelques KPIs à affiner)

#### ❌ Fonctionnalités Non Implémentées

- [ ] Tests unitaires et d'intégration
- [ ] Déploiement en production
- [ ] Monitoring et logging avancé
- [ ] Migration vers microservices (planifié)
- [ ] Intégration complète OpenSky Network
- [ ] Notifications push
- [ ] Export de rapports

---

## 🎯 15. OBJECTIFS DU PROJET

### Objectifs Principaux

1. **Sécurité Aérienne**
   - Réduire les risques d'accidents
   - Améliorer la coordination entre pilotes et contrôleurs
   - Détection proactive de conflits

2. **Efficacité Opérationnelle**
   - Optimisation des décollages (DMAN)
   - Réduction des retards
   - Amélioration de la gestion du trafic

3. **Formation et Apprentissage**
   - Compréhension des systèmes aéronautiques
   - Maîtrise des technologies modernes (Spring Boot, React)
   - Gestion de projet complet

### Objectifs Techniques

1. **Architecture Moderne**
   - Spring Boot 3.2.0
   - React 18
   - WebSocket temps réel
   - API RESTful

2. **Sécurité**
   - Authentification JWT
   - Protection par rôle
   - Mots de passe hashés

3. **Performance**
   - Cache des données météo
   - WebSocket pour mises à jour efficaces
   - Requêtes optimisées

---

## 🚀 16. DÉPLOIEMENT

### Prérequis

- Java 17+
- Node.js 18+
- PostgreSQL 14+
- Maven 3.8+

### Étapes de Déploiement

1. **Cloner le projet**
```bash
git clone <repository-url>
cd RadarFlightPFA-main
```

2. **Configurer PostgreSQL**
```bash
# Créer la base de données
createdb flightradar

# Ou via psql
psql -U postgres
CREATE DATABASE flightradar;
```

3. **Configurer le Backend**
```bash
cd backend
# Copier le fichier d'exemple
cp src/main/resources/application.properties.example src/main/resources/application.properties
# Éditer application.properties avec vos credentials PostgreSQL
```

4. **Démarrer le Backend**
```bash
mvn spring-boot:run
# Le backend démarre sur http://localhost:8080
```

5. **Configurer le Frontend**
```bash
cd frontend
npm install
```

6. **Démarrer le Frontend**
```bash
npm run dev
# Le frontend démarre sur http://localhost:3000
```

### Configuration Production

⚠️ **Important pour la production** :
- Changer la clé JWT secrète
- Configurer des credentials de base de données sécurisés
- Activer HTTPS
- Configurer CORS correctement pour votre domaine
- Désactiver `show-sql` dans `application.properties`
- Configurer un logging approprié
- Mettre en place un monitoring

---

## 📚 17. DOCUMENTATION

### Fichiers de Documentation Disponibles

- `README.md` - Vue d'ensemble du projet
- `ARCHITECTURE.md` - Architecture détaillée
- `GUIDE_UTILISATION_COMPLET.md` - Guide d'utilisation
- `API_DOCUMENTATION.md` - Documentation API
- `DEFINITION_PROJET_COMPLETE.md` - Ce document
- Et 100+ autres fichiers de documentation

---

## 📝 18. CONCLUSION

Ce projet représente une application complète de suivi aérien en temps réel, avec :

- **Architecture moderne** : Spring Boot + React
- **Fonctionnalités complètes** : 3 dashboards spécialisés
- **Sécurité robuste** : JWT + protection par rôle
- **Temps réel** : WebSocket pour mises à jour
- **Intégration APIs** : Open-Meteo pour météo
- **Base de données** : PostgreSQL avec 13 entités
- **Documentation** : Complète et détaillée

Le projet est prêt pour la présentation et peut servir de base pour un déploiement en production après les ajustements de sécurité nécessaires.

---

**Date de création** : 2025-11-20  
**Version** : 1.0.0  
**Statut** : En développement (75% complété)

