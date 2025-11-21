# Architecture du Projet Flight Radar

## 📐 Vue d'ensemble

Le projet suit une architecture en couches (Layered Architecture) avec séparation claire des responsabilités :

```
┌─────────────────────────────────────────┐
│         Frontend (React)                │
│  - Components                           │
│  - Pages                                │
│  - Services (API calls)                 │
│  - WebSocket Client                     │
└──────────────┬──────────────────────────┘
               │ HTTP REST + WebSocket
┌──────────────▼──────────────────────────┐
│      Backend (Spring Boot)              │
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

## 🏗️ Structure des Modules Backend

### 1. Module Auth
- **Entités** : `User`
- **Service** : `AuthService` (JWT)
- **Controller** : `AuthController`
- **Responsabilité** : Authentification et autorisation

### 2. Module Airport
- **Entités** : `Airport`, `Runway`
- **Repositories** : `AirportRepository`, `RunwayRepository`
- **Service** : Intégré dans `WeatherService`
- **Controller** : `AirportController`, `RunwayController`
- **Responsabilité** : Gestion des aéroports et pistes

### 3. Module Aircraft
- **Entités** : `Aircraft`, `Pilot`
- **Repositories** : `AircraftRepository`, `PilotRepository`
- **Service** : `AircraftService`
- **Controller** : `AircraftController`
- **Responsabilité** : Gestion des avions et pilotes

### 4. Module Radar
- **Entités** : `RadarCenter`, `Communication`
- **Repositories** : `RadarCenterRepository`, `CommunicationRepository`
- **Service** : `RadarService`
- **Controller** : `RadarController`
- **Responsabilité** : Communications VHF

### 5. Module Weather
- **Entités** : `WeatherData`
- **Repository** : `WeatherDataRepository`
- **Service** : `WeatherService`
- **Controller** : `WeatherController`
- **Responsabilité** : Données météorologiques et alertes

### 6. Module Flight
- **Entités** : `Flight`
- **Repository** : `FlightRepository`
- **Service** : `FlightService`
- **Controller** : `FlightController`
- **Responsabilité** : Gestion des vols

### 7. Module Realtime
- **Service** : `RealtimeUpdateService`
- **Config** : `WebSocketConfig`
- **Responsabilité** : Mises à jour en temps réel via WebSocket

## 🔄 Flux de Données

### 1. Authentification
```
Client → POST /api/auth/login
       → AuthService.authenticate()
       → Génération JWT
       → Retour token
```

### 2. Récupération des Avions
```
Client → GET /api/aircraft
       → AircraftController
       → AircraftService.getAllAircraft()
       → AircraftRepository.findAll()
       → Retour liste avions
```

### 3. Mise à jour Temps Réel
```
AircraftService.simulateAircraftMovement() (toutes les 5s)
       → RealtimeUpdateService.broadcastAircraftPositions()
       → WebSocket /topic/aircraft
       → Clients connectés
```

### 4. Récupération Météo
```
Client → GET /api/weather/airport/{id}
       → WeatherController
       → WeatherService.getCurrentWeather()
       → WeatherDataRepository
       → Retour données météo
```

## 📊 Relations entre Entités

### Relations Principales

1. **Airport ↔ Runway** (1:N)
   - Un aéroport a plusieurs pistes
   - Cascade DELETE

2. **Airport ↔ RadarCenter** (1:1)
   - Un aéroport a un seul centre radar
   - Unique constraint

3. **Airport ↔ Aircraft** (1:N)
   - Un aéroport peut avoir plusieurs avions basés
   - SET NULL on delete

4. **Pilot ↔ Aircraft** (1:N)
   - Un pilote peut piloter plusieurs avions
   - SET NULL on delete

5. **Aircraft ↔ Flight** (1:N)
   - Un avion peut effectuer plusieurs vols
   - Cascade DELETE

6. **Airport ↔ Flight** (N:M via departure/arrival)
   - Un aéroport peut être départ ou arrivée
   - RESTRICT on delete

7. **Airport ↔ WeatherData** (1:N)
   - Historique météorologique
   - Cascade DELETE

8. **Communication** (Polymorphe)
   - sender_type + sender_id
   - receiver_type + receiver_id

## 🔐 Sécurité

### Authentification JWT
- Token généré lors du login
- Validité : 24 heures
- Stocké côté client (localStorage)
- Inclus dans header : `Authorization: Bearer <token>`

### Rôles
- **ADMIN** : Accès complet
- **PILOTE** : Accès limité aux avions assignés
- **CENTRE_RADAR** : Accès aux communications radar

### CORS
- Origine autorisée : `http://localhost:3000`
- Méthodes : GET, POST, PUT, DELETE, OPTIONS

## ⚡ Performance

### Optimisations
- **Lazy Loading** : Relations @ManyToOne et @OneToMany
- **Indexes** : Sur colonnes fréquemment requêtées
- **Caching** : Données météo mises en cache 10 minutes
- **WebSocket** : Réduit le polling HTTP

### Scheduled Tasks
- **Aircraft Movement** : Toutes les 5 secondes
- **Weather Update** : Toutes les 10 minutes
- **WebSocket Broadcast** : Toutes les 5 secondes

## 📡 API REST Endpoints

### Authentification
- `POST /api/auth/login`

### Airports
- `GET /api/airports`
- `GET /api/airports/{id}`
- `GET /api/airports/{id}/weather`

### Aircraft
- `GET /api/aircraft`
- `GET /api/aircraft/{id}`
- `PUT /api/aircraft/{id}/updatePosition`
- `POST /api/aircraft/{id}/start-flight`

### Radar
- `POST /api/radar/sendMessage`
- `GET /api/radar/messages`

### Weather
- `GET /api/weather/airport/{airportId}`
- `GET /api/weather/alerts`

### Flights
- `GET /api/flights`
- `POST /api/flights`
- `POST /api/flights/{id}/start`
- `POST /api/flights/{id}/complete`

## 🔌 WebSocket Topics

- `/topic/aircraft` : Positions des avions
- `/topic/aircraft/{id}` : Mise à jour d'un avion spécifique
- `/topic/weather-alerts` : Alertes météo
- `/topic/weather/{airportId}` : Météo d'un aéroport

## 📦 Dépendances Principales

### Backend
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security
- Spring WebSocket
- PostgreSQL Driver
- JWT (jjwt)
- WebFlux (pour API externe)

### Frontend
- React 18
- React Router
- Axios
- Leaflet / React-Leaflet
- SockJS / STOMP (WebSocket)
- Tailwind CSS

## 🎯 Principes de Conception

1. **Séparation des Responsabilités** : Chaque classe a une responsabilité unique
2. **DRY (Don't Repeat Yourself)** : Réutilisation du code via services
3. **SOLID** : Principes appliqués dans l'architecture
4. **RESTful** : API REST respectant les conventions
5. **Stateless** : Authentification JWT sans session serveur

