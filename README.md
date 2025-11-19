# 🛫 FlightRadar24-like - Système de Suivi Aérien en Temps Réel

## 📋 Description

Application web complète similaire à Flightradar24 pour le suivi en temps réel des avions, de la météo, et des communications entre centres radar et aéroports pour améliorer la sécurité aérienne.

## ✨ Fonctionnalités

### 🔐 Authentification
- Système d'authentification JWT complet
- 3 rôles : **ADMIN**, **CENTRE_RADAR**, **PILOTE**
- Protection des routes par rôle
- API de gestion des comptes (ADMIN)

### 📊 Dashboard ADMIN
- KPIs aéronautiques en temps réel
- Graphiques de trafic par aéroport
- Statistiques de retards
- Indicateurs de sécurité
- Performance ATC
- DMAN (Target Takeoff Time)

### 📡 Dashboard CENTRE RADAR
- Carte radar interactive (secteur 50 km)
- Position des avions en temps réel
- Données ATIS (météo aéroport)
- Historique des communications ATC
- Détection de conflits

### ✈️ Dashboard PILOTE
- Informations générales du vol
- Position & mouvement (latitude, longitude, altitude, vitesse, cap)
- Statut du vol
- Météo du vol
- Communications ATC
- Sécurité / ADS-B Tracking
- KPIs
- Demande d'autorisation de décollage

## 🛠️ Technologies

### Backend
- **Spring Boot** 3.x
- **PostgreSQL** - Base de données
- **JPA/Hibernate** - ORM
- **Spring Security** - Sécurité
- **JWT** - Authentification
- **WebSocket** - Temps réel
- **Open-Meteo API** - Données météo

### Frontend
- **React.js** 18
- **Vite** - Build tool
- **Tailwind CSS** - Styling
- **Leaflet** - Cartes interactives
- **Chart.js** - Graphiques
- **Axios** - HTTP client

## 🚀 Installation

### Prérequis
- Java 17+
- Node.js 18+
- PostgreSQL 14+
- Maven 3.8+

### Configuration

1. **Cloner le repository**
```bash
git clone https://github.com/VOTRE_USERNAME/PFA-2026.git
cd PFA-2026
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

4. **Configurer le Frontend**
```bash
cd frontend
npm install
```

## 🏃 Démarrage

### Backend
```bash
cd backend
mvn spring-boot:run
```
Le backend démarre sur `http://localhost:8080`

### Frontend
```bash
cd frontend
npm run dev
```
Le frontend démarre sur `http://localhost:3000`

## 👤 Comptes par Défaut

Les comptes sont créés automatiquement au démarrage :

- **ADMIN** : `admin` / `admin`
- **CENTRE_RADAR** : `radar_cmn`, `radar_rba`, `radar_rak`, `radar_tng` / `radar123`
- **PILOTE** : `pilot1`, `pilot2`, etc. / `pilot123`

## 📚 Documentation

- `GUIDE_UTILISATION_COMPLET.md` - Guide d'utilisation complet
- `PLAN_ARCHITECTURE_COMPLETE.md` - Architecture détaillée
- `ETAT_IMPLEMENTATION.md` - État d'implémentation
- `RESUME_FINAL_IMPLEMENTATION.md` - Résumé final

## 🔧 Scripts Utiles

- `ASSIGNER_AEROPORTS_RADAR_AUTO.ps1` - Assigner aéroports aux centres radar
- `VERIFIER_ET_ASSIGNER_AVION.ps1` - Assigner avions aux pilotes
- `DEMARRER_BACKEND.ps1` - Démarrer le backend
- `DEMARRER_FRONTEND.ps1` - Démarrer le frontend

## 📝 API Endpoints

### Authentification
- `POST /api/auth/login` - Connexion
- `POST /api/auth/register` - Créer compte (ADMIN)
- `GET /api/auth/users` - Liste utilisateurs (ADMIN)

### Dashboard ADMIN
- `GET /api/admin/dashboard` - Dashboard complet
- `GET /api/admin/kpis` - KPIs temps réel
- `GET /api/admin/statistics` - Statistiques

### Dashboard RADAR
- `GET /api/radar/dashboard` - Dashboard complet
- `GET /api/radar/dashboard/aircraft` - Avions dans le secteur
- `GET /api/radar/dashboard/atis` - Données ATIS

### Dashboard PILOTE
- `GET /api/pilots/{username}/dashboard` - Dashboard complet
- `POST /api/atc/request-takeoff-clearance` - Demander autorisation décollage

## 🔒 Sécurité

- JWT avec expiration de 24h
- Protection par rôle sur toutes les routes
- Mots de passe hashés avec BCrypt
- CORS configuré pour localhost:3000 et localhost:3001

## 📊 Base de Données

Le schéma de base de données est créé automatiquement via Hibernate (`ddl-auto=update`).

Tables principales :
- `users` - Utilisateurs
- `pilots` - Pilotes
- `aircraft` - Avions
- `airports` - Aéroports
- `radar_centers` - Centres radar
- `flights` - Vols
- `atc_messages` - Messages ATC
- `atis_data` - Données ATIS
- `weather_data` - Données météo

## 🤝 Contribution

Les contributions sont les bienvenues ! N'hésitez pas à ouvrir une issue ou une pull request.

## 📄 Licence

Ce projet est un projet universitaire (PFA 2026).

## 👨‍💻 Auteur

Projet développé dans le cadre du Projet de Fin d'Année (PFA) 2026.

---

**Note** : Ce projet est à des fins éducatives. Pour un usage en production, assurez-vous de :
- Changer la clé JWT secrète
- Configurer des credentials de base de données sécurisés
- Activer HTTPS
- Configurer CORS correctement pour votre domaine
