# Gestion des Vols - Documentation

## 📋 Vue d'ensemble

Ce document décrit le module **"Gestion des Vols"** ajouté au dashboard Admin et au dashboard Pilote. Ce module permet la création, modification et suppression de vols avec intégration complète des données météorologiques et journalisation des actions.

## 🎯 Objectifs

Le module de gestion des vols permet :
- **Admin** : Créer, modifier et supprimer des vols avec tous les détails nécessaires
- **Pilote** : Visualiser automatiquement ses vols assignés avec météo intégrée
- **Système** : Journaliser toutes les actions pour audit
- **Sécurité** : Empêcher la modification/suppression de vols en cours

## 🏗️ Architecture

### Backend

#### Modèle Flight Enrichi

**Fichier** : `backend/src/main/java/com/flightradar/model/Flight.java`

**Nouveaux champs ajoutés :**
- `cruiseAltitude` (Integer) - Altitude de croisière en pieds
- `cruiseSpeed` (Integer) - Vitesse de croisière en nœuds
- `flightType` (Enum) - Type de vol (COMMERCIAL, CARGO, PRIVATE, MILITARY, TRAINING)
- `alternateAirportId` (Long) - Aéroport alternatif (optionnel)
- `estimatedTimeEnroute` (Integer) - ETE en minutes (calculé automatiquement)
- `pilotId` (Long) - Pilote assigné directement au vol

#### Service FlightManagementService

**Fichier** : `backend/src/main/java/com/flightradar/service/FlightManagementService.java`

**Méthodes principales :**
- `createFlight()` - Crée un nouveau vol avec validation
- `updateFlight()` - Met à jour un vol (uniquement si pas en vol)
- `deleteFlight()` - Supprime un vol (uniquement si pas en vol)
- `getFlightsByPilot()` - Récupère les vols d'un pilote
- `getFlightDetails()` - Récupère les détails complets avec météo

**Fonctionnalités :**
- Calcul automatique de l'ETE (Estimated Time Enroute) à partir des dates STD/STA
- Validation des aéroports et avions
- Intégration des données météo (METAR/TAF) pour départ et arrivée
- Journalisation automatique de toutes les actions

#### Endpoints REST

**Fichier** : `backend/src/main/java/com/flightradar/controller/FlightController.java`

**Nouveaux endpoints :**

1. **POST /api/flight/manage** (ADMIN uniquement)
   - Crée un nouveau vol
   - Body: JSON avec tous les champs du vol
   - Retourne le vol créé

2. **PUT /api/flight/manage/{flightId}** (ADMIN uniquement)
   - Met à jour un vol existant
   - Vérifie que le vol n'est pas en cours
   - Body: JSON avec les champs à modifier
   - Retourne le vol modifié

3. **DELETE /api/flight/manage/{flightId}** (ADMIN uniquement)
   - Supprime un vol
   - Vérifie que le vol n'est pas en cours
   - Retourne un message de succès

4. **GET /api/flight/manage/{flightId}/details** (ADMIN, PILOTE, CENTRE_RADAR)
   - Récupère les détails complets d'un vol
   - Inclut les données météo pour départ et arrivée
   - Retourne toutes les informations du vol

5. **GET /api/flight/pilot/{pilotId}** (ADMIN, PILOTE)
   - Récupère les vols assignés à un pilote
   - Vérifie les permissions (pilote ne peut voir que ses propres vols)
   - Retourne la liste des vols

6. **GET /api/flight/pilot/username/{username}** (ADMIN, PILOTE)
   - Récupère les vols assignés à un pilote par son username
   - Inclut les détails complets avec météo
   - Vérifie les permissions

### Frontend

#### Composant FlightManagement

**Fichier** : `frontend/src/components/FlightManagement.jsx`

**Fonctionnalités :**
- Formulaire de création/modification de vol
- Liste de tous les vols avec actions (modifier/supprimer)
- Validation des champs obligatoires
- Calcul automatique de l'ETE (affiché dans le formulaire)
- Désactivation des actions pour les vols en cours

**Champs du formulaire :**
- Numéro de vol / Callsign (obligatoire)
- Compagnie aérienne (obligatoire)
- Avion (obligatoire)
- Pilote assigné (optionnel)
- Aéroport de départ (obligatoire)
- Aéroport d'arrivée (obligatoire)
- Aéroport alternatif (optionnel)
- STD / STA (obligatoires)
- Altitude de croisière (optionnel)
- Vitesse de croisière (optionnel)
- Type de vol (obligatoire)
- Statut initial (PLANIFIE ou RETARDE)

#### Modification PilotDashboard

**Fichier** : `frontend/src/pages/PilotDashboard.jsx`

**Nouvelle section : "Mes Vols Assignés"**

**Affichage :**
- Liste de tous les vols assignés au pilote connecté
- Pour chaque vol :
  - Informations de base (numéro, compagnie, statut)
  - Plan de vol complet (départ, arrivée, alternatif, STD, STA, ETE)
  - Altitude et vitesse de croisière
  - Météo pour l'aéroport de départ (température, vent, visibilité, conditions, alertes)
  - Météo pour l'aéroport d'arrivée (température, vent, visibilité, conditions, alertes)
  - Alertes météo visuelles si présentes

## 📊 Données Météorologiques

### Intégration METAR/TAF

Les données météo sont récupérées automatiquement via le `WeatherService` qui utilise l'API Open-Meteo.

**Pour chaque vol :**
- Météo de l'aéroport de départ récupérée automatiquement
- Météo de l'aéroport d'arrivée récupérée automatiquement
- Alertes météo affichées si conditions critiques détectées

**Données affichées :**
- Température (°C)
- Vitesse du vent (kt)
- Direction du vent (degrés)
- Visibilité (km)
- Conditions météo (Clear, Fog, Mist, etc.)
- Indicateur d'alerte (si conditions dangereuses)

## 🔒 Sécurité et Permissions

### RBAC (Role-Based Access Control)

**ADMIN :**
- ✅ Créer des vols
- ✅ Modifier des vols (sauf en cours)
- ✅ Supprimer des vols (sauf en cours)
- ✅ Voir tous les vols
- ✅ Voir les détails complets de tous les vols

**PILOTE :**
- ❌ Créer des vols
- ❌ Modifier des vols
- ❌ Supprimer des vols
- ✅ Voir uniquement ses propres vols assignés
- ✅ Voir les détails complets de ses vols avec météo

**CENTRE_RADAR :**
- ❌ Créer des vols
- ❌ Modifier des vols
- ❌ Supprimer des vols
- ✅ Voir les détails complets des vols (lecture seule)

### Protection contre la modification de vols en cours

- Un vol avec le statut `EN_COURS` ne peut pas être modifié
- Un vol avec le statut `EN_COURS` ne peut pas être supprimé
- Les boutons de modification/suppression sont désactivés dans l'interface

## 📝 Journalisation

Toutes les actions critiques sont automatiquement journalisées dans la table `activity_logs` :

**Types d'activités loggées :**
- `FLIGHT_CREATED` - Création d'un vol
- `FLIGHT_UPDATED` - Modification d'un vol
- `FLIGHT_CANCELLED` - Suppression d'un vol

**Informations journalisées :**
- Username de l'utilisateur
- Type d'activité
- Description (ex: "Création du vol AT123")
- Entity type: "FLIGHT"
- Entity ID: ID du vol
- Timestamp
- Sévérité (INFO pour création/modification, WARNING pour suppression)

## 🗄️ Base de Données

### Migration SQL

**Fichier** : `backend/database/add_flight_fields.sql`

**Champs ajoutés à la table `flights` :**
```sql
- cruise_altitude INTEGER
- cruise_speed INTEGER
- flight_type VARCHAR(20)
- alternate_airport_id BIGINT
- estimated_time_enroute INTEGER
- pilot_id BIGINT
```

**Index créés :**
- `idx_flights_pilot_id` - Pour les requêtes par pilote
- `idx_flights_alternate_airport_id` - Pour les requêtes par aéroport alternatif
- `idx_flights_flight_type` - Pour les filtres par type de vol

**Contraintes :**
- Clé étrangère vers `airports` pour `alternate_airport_id`
- Clé étrangère vers `pilots` pour `pilot_id`
- CHECK constraint pour `flight_type`

## 🚀 Utilisation

### Pour l'Administrateur

1. **Créer un vol :**
   - Accéder au dashboard Admin
   - Cliquer sur "Gestion des Vols"
   - Cliquer sur "+ Nouveau Vol"
   - Remplir le formulaire
   - Cliquer sur "Créer"

2. **Modifier un vol :**
   - Dans la liste des vols, cliquer sur "Modifier"
   - Modifier les champs souhaités
   - Cliquer sur "Modifier"
   - ⚠️ Impossible si le vol est en cours

3. **Supprimer un vol :**
   - Dans la liste des vols, cliquer sur "Supprimer"
   - Confirmer la suppression
   - ⚠️ Impossible si le vol est en cours

### Pour le Pilote

1. **Voir ses vols assignés :**
   - Se connecter au dashboard Pilote
   - La section "Mes Vols Assignés" s'affiche automatiquement
   - Tous les vols assignés sont listés avec météo intégrée

2. **Consulter les détails :**
   - Chaque vol affiche :
     - Plan de vol complet
     - Météo départ et arrivée
     - Alertes météo si présentes

## 🔄 Calcul Automatique de l'ETE

L'ETE (Estimated Time Enroute) est calculé automatiquement lors de la création ou modification d'un vol :

**Formule :**
```
ETE (minutes) = STA - STD
```

**Exemple :**
- STD: 2024-01-15 10:00
- STA: 2024-01-15 12:30
- ETE: 150 minutes (2h30)

## 📚 Références

- **ICAO Annex 3** - Standards pour météo et alertes aéronautiques
- **FlightAware** - Dashboard de suivi aérien
- **Eurocontrol** - Dashboard opérationnel européen
- **FAA Ops Dashboard** - Dashboard opérationnel américain

## ⚠️ Notes Importantes

- **Ne pas modifier les fonctionnalités existantes** : Le module de gestion des vols est un ajout qui n'altère pas les fonctionnalités existantes
- **Performance** : Les requêtes sont optimisées avec des index, mais pour de très grandes quantités de données, envisager une pagination
- **Météo** : Les données météo sont mises à jour toutes les 10 minutes via le service WeatherService
- **Journalisation** : Tous les logs sont stockés indéfiniment. Envisager une politique de rétention si nécessaire

## 🔧 Maintenance

### Ajout de nouveaux types de vol

Pour ajouter un nouveau type de vol :

1. Ajouter la valeur dans l'enum `Flight.FlightType`
2. Mettre à jour la contrainte CHECK dans la table SQL
3. Ajouter l'option dans le formulaire frontend

### Modification du calcul de l'ETE

Pour modifier la logique de calcul de l'ETE :

1. Modifier la méthode dans `FlightManagementService.createFlight()` ou `updateFlight()`
2. La logique actuelle calcule simplement la différence entre STA et STD
3. Pour un calcul plus complexe (distance, vitesse, etc.), enrichir la méthode

