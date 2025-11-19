# 🚀 Plan de Migration vers Architecture Microservices - PFA 2026

## 📋 Vue d'ensemble

Migration progressive du projet monolithique vers une architecture microservices tout en conservant le fonctionnement actuel.

## 🎯 Objectifs

1. ✅ Conserver toutes les fonctionnalités existantes
2. ✅ Ajouter la liaison Pilote ⇄ Avion (pilotId, usernamePilote)
3. ✅ Créer Dashboard Pilote complet avec toutes les informations
4. ✅ Implémenter "Demander Autorisation de Décollage" avec règles ICAO/FAA
5. ✅ Ajouter KPIs dans le Dashboard
6. ✅ Créer documentation Swagger/OpenAPI
7. ✅ Générer diagrammes UML

## 🏗️ Architecture Cible

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (React)                        │
│  - Dashboard Pilote amélioré                               │
│  - Dashboard Radar                                        │
└───────────────────────┬───────────────────────────────────┘
                        │ HTTP/REST + WebSocket
┌───────────────────────▼───────────────────────────────────┐
│              API Gateway (Port 8080)                       │
│  - Routing vers microservices                              │
│  - Authentification JWT                                    │
└───────────────────────┬───────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┬───────────────┐
        │               │               │               │
┌───────▼──────┐ ┌──────▼──────┐ ┌──────▼──────┐ ┌──────▼──────┐
│   Eureka     │ │   Service   │ │   Service   │ │   Service   │
│   Server     │ │   Pilote    │ │   Avion     │ │   Radar     │
│   Port:8761  │ │  Port:8081  │ │  Port:8082  │ │  Port:8083  │
└──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘
                        │               │               │
┌───────────────────────▼───────────────▼───────────────▼───────┐
│                    Service Météo (Port 8084)                  │
│                    Service ATC (Port 8085)                   │
└───────────────────────────────────────────────────────────────┘
                        │
┌───────────────────────▼───────────────────────────────────┐
│              PostgreSQL (flightradar_db)                  │
└───────────────────────────────────────────────────────────┘
```

## 📝 Phase 1 : Préparation (Sans casser l'existant)

### 1.1 Améliorer les modèles existants
- ✅ Ajouter `usernamePilote` dans `Aircraft`
- ✅ Améliorer `Flight` avec numéro de vol, compagnie aérienne
- ✅ Ajouter champs ADS-B (transponderCode, airSpeed, verticalSpeed)

### 1.2 Créer DTOs pour Dashboard Pilote
- ✅ `PilotDashboardDTO` - Données complètes dashboard
- ✅ `FlightInfoDTO` - Informations du vol
- ✅ `WeatherInfoDTO` - Météo du vol
- ✅ `ATCMessageDTO` - Messages ATC
- ✅ `KPIDTO` - KPIs temps réel

### 1.3 Améliorer les services existants
- ✅ `PilotService` - Liaison Pilote ⇄ Avion
- ✅ `AircraftService` - Position ADS-B temps réel
- ✅ `RadarService` - Messages ATC
- ✅ `ATCService` - Règles ICAO/FAA

## 📝 Phase 2 : Nouvelles fonctionnalités

### 2.1 Liaison Pilote ⇄ Avion
- ✅ Endpoint `GET /api/pilots/{username}/aircraft`
- ✅ Endpoint `GET /api/aircraft/pilot/{username}`
- ✅ Redirection automatique vers dashboard pilote

### 2.2 Dashboard Pilote Complet
**Informations à afficher :**
1. Informations générales du vol
2. Position & mouvement (ADS-B)
3. Statut du vol
4. Météo du vol
5. Communications ATC
6. Sécurité / Suivi ADS-B

**Endpoint :** `GET /api/pilots/{username}/dashboard`

### 2.3 Bouton "Demander Autorisation de Décollage"
- ✅ Endpoint `POST /api/atc/request-takeoff-clearance`
- ✅ Analyse en temps réel :
  - Trafic aérien
  - Météo
  - Disponibilité piste
  - Risques potentiels
- ✅ Réponse : Autorisation accordée/refusée/en attente + message

### 2.4 Règles ICAO/FAA
**Conditions minimales pour décollage :**
- Visibilité minimale : 550m (1800ft) pour CAT I
- Vent : Max 30 kt (55 km/h) pour décollage
- Vent travers : Max 15 kt (28 km/h)
- Distance minimale entre avions : 3 NM (5.5 km)
- Alertes météo : Storm, turbulence, wind shear

### 2.5 KPIs Dashboard
**KPIs Temps Réel :**
- Distance restante
- ETA (Estimated Time of Arrival)
- Consommation carburant estimée
- Niveau de carburant
- Vitesse moyenne
- Altitude stable (oui/non)
- Turbulence détectée

**KPIs Radar/Sécurité :**
- Sévérité météo (0-100%)
- Indice de risque de trajectoire
- Densité de trafic dans 30 km
- Score d'état avion

## 📝 Phase 3 : Migration Microservices (Optionnel - Phase future)

### 3.1 Eureka Server
- Service discovery
- Port 8761

### 3.2 API Gateway
- Spring Cloud Gateway
- Routing vers services
- Authentification centralisée

### 3.3 Services Microservices
- Service Pilote (Port 8081)
- Service Avion (Port 8082)
- Service Radar (Port 8083)
- Service Météo (Port 8084)
- Service ATC (Port 8085)

## 🔧 Implémentation Progressive

### Étape 1 : Améliorer le monolithe (SANS casser)
- ✅ Ajouter champs manquants dans modèles
- ✅ Créer DTOs
- ✅ Améliorer services
- ✅ Créer nouveaux endpoints

### Étape 2 : Frontend Dashboard Pilote
- ✅ Améliorer `PilotDashboard.jsx`
- ✅ Afficher toutes les informations
- ✅ Bouton "Demander Autorisation"
- ✅ Panneau KPIs

### Étape 3 : Documentation
- ✅ Swagger/OpenAPI
- ✅ Diagrammes UML

## 🚨 Contraintes

1. **Ne pas casser les endpoints existants**
2. **Ne pas modifier les fonctionnalités actuelles**
3. **Ajouter uniquement ce qui est demandé**
4. **Tester après chaque modification**

## 📊 Livrables

1. ✅ Architecture mise à jour
2. ✅ UML (classes, séquence, cas d'usage)
3. ✅ Code nouveaux services/contrôleurs/DTO
4. ✅ API liaison pilote-avion
5. ✅ Front-end dashboard pilote
6. ✅ Code bouton "Demander Autorisation"
7. ✅ Microservice radar amélioré
8. ✅ KPIs
9. ✅ Sécurité (JWT)
10. ✅ Documentation Swagger/OpenAPI

