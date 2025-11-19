# 🏗️ Architecture Microservices - Flight Radar PFA 2026

## 📐 Vue d'ensemble

```
┌─────────────────────────────────────────────────────────────────┐
│                        Frontend (React)                        │
│  - Dashboard Pilote                                            │
│  - Dashboard Radar                                             │
│  - WebSocket Client                                            │
└───────────────────────┬───────────────────────────────────────┘
                        │ HTTP/REST + WebSocket
┌───────────────────────▼───────────────────────────────────────┐
│                    API Gateway (Spring Cloud)                 │
│  - Routing                                                      │
│  - Load Balancing                                               │
│  - Authentication (JWT)                                        │
│  - Rate Limiting                                               │
└───────────────────────┬───────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┬───────────────┐
        │               │               │               │
┌───────▼──────┐ ┌──────▼──────┐ ┌──────▼──────┐ ┌──────▼──────┐
│   Eureka     │ │   Service   │ │   Service   │ │   Service   │
│   Server     │ │   Pilote    │ │   Avion     │ │   Radar     │
│              │ │             │ │             │ │             │
│  Port: 8761  │ │  Port: 8081 │ │  Port: 8082 │ │  Port: 8083 │
└──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘
                        │               │               │
┌───────────────────────▼───────────────▼───────────────▼───────┐
│                    Service Météo                              │
│                    Port: 8084                                 │
└───────────────────────────────────────────────────────────────┘
                        │
┌───────────────────────▼───────────────────────────────────────┐
│                    Service ATC                                │
│                    Port: 8085                                 │
│  - Autorisation décollage/atterrissage                        │
│  - Règles ICAO/FAA                                            │
│  - Détection conflits                                         │
└───────────────────────────────────────────────────────────────┘
                        │
┌───────────────────────▼───────────────────────────────────────┐
│              Base de données PostgreSQL                       │
│  - Database: flightradar_db                                   │
│  - Tables partagées entre services                             │
└───────────────────────────────────────────────────────────────┘
```

## 🔧 Services Microservices

### 1. Eureka Server (Service Discovery)
- **Port**: 8761
- **Responsabilité**: Découverte et enregistrement des services
- **Technologie**: Spring Cloud Netflix Eureka

### 2. API Gateway
- **Port**: 8080
- **Responsabilité**: 
  - Routage des requêtes
  - Authentification JWT
  - Load balancing
  - Rate limiting
- **Technologie**: Spring Cloud Gateway

### 3. Service Pilote (Pilot Service)
- **Port**: 8081
- **Responsabilité**:
  - Gestion des pilotes
  - Liaison Pilote ↔ Avion
  - Dashboard pilote (API)
- **Endpoints**:
  - `GET /api/pilots/{username}/aircraft` - Récupérer l'avion du pilote
  - `GET /api/pilots/{username}/dashboard` - Données complètes dashboard
  - `GET /api/pilots/{username}/flight-info` - Informations du vol

### 4. Service Avion (Aircraft Service)
- **Port**: 8082
- **Responsabilité**:
  - Gestion des avions
  - Position et mouvement (ADS-B)
  - Statut des vols
- **Endpoints**:
  - `GET /api/aircraft/{id}/position` - Position temps réel
  - `GET /api/aircraft/{id}/movement` - Mouvement (vitesse, cap, altitude)
  - `GET /api/aircraft/{id}/status` - Statut du vol
  - `PUT /api/aircraft/{id}/update-position` - Mise à jour position

### 5. Service Radar (Radar Service)
- **Port**: 8083
- **Responsabilité**:
  - Communications VHF
  - Messages ATC
  - Historique communications
- **Endpoints**:
  - `GET /api/radar/aircraft/{id}/messages` - Messages pour un avion
  - `POST /api/radar/send-message` - Envoyer message
  - `GET /api/radar/atc-history/{aircraftId}` - Historique ATC

### 6. Service Météo (Weather Service)
- **Port**: 8084
- **Responsabilité**:
  - Données météo temps réel
  - Alertes météo
  - Conditions pour décollage/atterrissage
- **Endpoints**:
  - `GET /api/weather/airport/{id}` - Météo aéroport
  - `GET /api/weather/flight/{aircraftId}` - Météo pour un vol
  - `GET /api/weather/alerts` - Alertes météo

### 7. Service ATC (Air Traffic Control Service)
- **Port**: 8085
- **Responsabilité**:
  - Autorisation décollage/atterrissage
  - Règles ICAO/FAA
  - Analyse trafic aérien
  - Détection risques
- **Endpoints**:
  - `POST /api/atc/request-takeoff-clearance` - Demander autorisation décollage
  - `POST /api/atc/request-landing-clearance` - Demander autorisation atterrissage
  - `GET /api/atc/clearance-status/{aircraftId}` - Statut autorisation
  - `GET /api/atc/traffic-analysis/{airportId}` - Analyse trafic

## 📊 Base de données

### Tables partagées
- `users` - Utilisateurs (pilotes, ATC, admin)
- `pilots` - Pilotes
- `aircraft` - Avions
- `flights` - Vols
- `airports` - Aéroports
- `runways` - Pistes
- `weather_data` - Données météo
- `communications` - Communications VHF
- `radar_centers` - Centres radar

## 🔐 Sécurité

- **JWT** pour authentification
- **OAuth2** (optionnel) pour autorisation avancée
- **API Gateway** gère l'authentification centralisée

## 📡 Communication Inter-Services

- **Synchronous**: REST (via API Gateway)
- **Asynchronous**: WebSocket pour temps réel
- **Service Discovery**: Eureka

## 🚀 Déploiement

### Ordre de démarrage
1. PostgreSQL (base de données)
2. Eureka Server
3. Services (Pilote, Avion, Radar, Météo, ATC)
4. API Gateway
5. Frontend

### Ports
- Eureka Server: 8761
- API Gateway: 8080
- Service Pilote: 8081
- Service Avion: 8082
- Service Radar: 8083
- Service Météo: 8084
- Service ATC: 8085

