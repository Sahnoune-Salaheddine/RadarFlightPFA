# 🛫 Simulation de Vol en Temps Réel - Documentation Complète

## 📋 Résumé

Implémentation complète d'un système de simulation de vol en temps réel pour FlightRadar24-like. Après autorisation de décollage par le centre radar, le pilote peut lancer une simulation de vol qui met à jour automatiquement la position, l'altitude, la vitesse et le cap de l'avion toutes les 5 secondes.

---

## 🎯 Fonctionnalités Implémentées

### 1. ✅ Service de Simulation de Vol (`FlightSimulationService`)

**Fichier** : `backend/src/main/java/com/flightradar/service/FlightSimulationService.java`

**Fonctionnalités** :
- Simulation réaliste de vol avec 3 phases : montée, croisière, descente
- Calcul automatique de trajectoire entre deux aéroports (ligne droite simplifiée)
- Mise à jour périodique (toutes les 5 secondes) de :
  - Position (latitude, longitude)
  - Altitude (montée jusqu'à 10 000 m, puis descente)
  - Vitesse (250 km/h au décollage → 800 km/h en croisière)
  - Cap (calculé automatiquement vers la destination)
  - Distance restante
  - ETA (Estimated Time of Arrival)
- Gestion automatique de l'atterrissage
- Mise à jour de la base de données en temps réel
- Diffusion via WebSocket pour tous les clients connectés

**Constantes de simulation** :
- Altitude de croisière : 10 000 mètres
- Vitesse de croisière : 800 km/h
- Vitesse au décollage : 250 km/h
- Taux de montée : 10 m/s
- Taux de descente : 8 m/s
- Intervalle de mise à jour : 5 secondes

---

### 2. ✅ Mise à Jour de la Base de Données

**Modifications** :
- Ajout du champ `estimated_arrival` dans la table `flights`
- Mise à jour automatique de `actual_departure` au décollage
- Mise à jour automatique de `actual_arrival` à l'atterrissage
- Statut du vol : `PLANIFIE` → `EN_COURS` → `TERMINE`

**Script SQL** :
```sql
ALTER TABLE flights ADD COLUMN IF NOT EXISTS estimated_arrival TIMESTAMP;
```

---

### 3. ✅ Endpoints API REST

#### `POST /api/flight/simulate-takeoff`
**Description** : Démarre la simulation d'un vol après autorisation de décollage

**Authentification** : Requis (PILOTE ou ADMIN)

**Body** :
```json
{
  "aircraftId": 1,
  "departureAirportId": 1,
  "arrivalAirportId": 2
}
```

**Réponse** :
```json
{
  "success": true,
  "flightId": 123,
  "flightNumber": "ATABC1234",
  "estimatedArrival": "2026-01-15T14:30:00",
  "message": "Simulation de vol démarrée avec succès"
}
```

**Sécurité** :
- Vérifie que le pilote est autorisé pour cet avion
- Vérifie qu'aucun vol n'est déjà en cours pour cet avion
- Retourne 403 si le pilote n'est pas autorisé

#### `GET /api/flight/{flightId}`
**Description** : Récupère les informations d'un vol en temps réel

**Authentification** : Requis (PILOTE, CENTRE_RADAR ou ADMIN)

**Réponse** :
```json
{
  "flightId": 123,
  "flightNumber": "ATABC1234",
  "status": "EN_COURS",
  "departureAirport": "CMN",
  "arrivalAirport": "RBA",
  "actualDeparture": "2026-01-15T12:00:00",
  "estimatedArrival": "2026-01-15T14:30:00",
  "currentLatitude": 33.567500,
  "currentLongitude": -7.589800,
  "currentAltitude": 8500.0,
  "currentSpeed": 800.0,
  "currentHeading": 45.0,
  "distanceRemaining": 150.5
}
```

#### `GET /api/flight`
**Description** : Récupère tous les vols (pour le dashboard admin)

**Authentification** : Requis (ADMIN ou CENTRE_RADAR)

**Réponse** : Liste de tous les vols avec leurs informations complètes

---

### 4. ✅ WebSocket - Mises à Jour Temps Réel

**Topics disponibles** :
- `/topic/aircraft/{aircraftId}` : Mises à jour spécifiques d'un avion
- `/topic/flight/{flightId}` : Mises à jour spécifiques d'un vol

**Format des messages** :
```json
{
  "type": "flight_update",
  "flightId": 123,
  "aircraftId": 1,
  "latitude": 33.567500,
  "longitude": -7.589800,
  "altitude": 8500.0,
  "speed": 800.0,
  "heading": 45.0,
  "distanceRemaining": 150.5,
  "estimatedArrival": "2026-01-15T14:30:00",
  "timestamp": 1705320000000
}
```

**Message de fin de vol** :
```json
{
  "type": "flight_completed",
  "flightId": 123,
  "aircraftId": 1,
  "timestamp": 1705327200000
}
```

---

### 5. ✅ Dashboard Pilote - Améliorations

**Fichier** : `frontend/src/pages/PilotDashboard.jsx`

**Nouvelles fonctionnalités** :
1. **Bouton "Décoller"** : Apparaît après autorisation GRANTED
2. **Sélection aéroport de destination** : Dropdown avec tous les aéroports disponibles
3. **Affichage temps réel** :
   - Position (lat, lon) mise à jour automatiquement
   - Altitude en temps réel
   - Vitesse en temps réel
   - Cap en temps réel
   - Distance restante
   - ETA (Estimated Time of Arrival)
4. **Intégration WebSocket** : Connexion automatique pour recevoir les mises à jour
5. **Statut visuel** : Affichage clair "EN VOL" avec toutes les informations

**Flux utilisateur** :
1. Pilote demande autorisation de décollage
2. Si autorisation accordée (GRANTED), le bouton "Décoller" apparaît
3. Pilote sélectionne l'aéroport de destination
4. Pilote clique sur "Décoller"
5. La simulation démarre et les données se mettent à jour en temps réel
6. Affichage de la position, altitude, vitesse, cap, ETA, distance restante

---

### 6. ✅ Dashboard Admin - Tableau des Vols

**Fichier** : `frontend/src/pages/AdminDashboard.jsx`

**Nouvelle section** : "Vols en Cours et Planifiés"

**Colonnes du tableau** :
- **Matricule** : Numéro d'immatriculation de l'avion
- **Numéro de vol** : Identifiant du vol (ex: ATABC1234)
- **Départ** : Aéroport de départ (nom + code IATA)
- **Arrivée** : Aéroport d'arrivée (nom + code IATA)
- **Heure départ** : Heure réelle ou prévue de départ
- **ETA** : Estimated Time of Arrival (heure d'arrivée estimée)
- **Statut** : PLANIFIE, EN_COURS, TERMINE, ANNULE, RETARDE

**Fonctionnalités** :
- Rafraîchissement automatique toutes les 10 secondes
- Affichage coloré du statut (badges)
- Tri et filtrage possibles (à implémenter si besoin)

---

## 🔒 Sécurité et Permissions

### Endpoints Protégés

1. **`POST /api/flight/simulate-takeoff`** :
   - Rôle requis : `PILOTE` ou `ADMIN`
   - Vérification : Le pilote doit être autorisé pour l'avion spécifié
   - Retourne 403 si non autorisé

2. **`GET /api/flight/{flightId}`** :
   - Rôle requis : `PILOTE`, `CENTRE_RADAR` ou `ADMIN`

3. **`GET /api/flight`** :
   - Rôle requis : `ADMIN` ou `CENTRE_RADAR`

### Vérifications de Sécurité

- ✅ Vérification que le pilote est bien assigné à l'avion
- ✅ Vérification qu'aucun vol n'est déjà en cours pour l'avion
- ✅ Validation des IDs d'aéroports (doivent exister dans la base)
- ✅ Protection CSRF via Spring Security
- ✅ Authentification JWT requise

---

## 📊 Architecture Technique

### Backend

**Services** :
- `FlightSimulationService` : Gère la simulation de vol
- `RealtimeUpdateService` : Diffuse les mises à jour via WebSocket
- `AircraftService` : Gère les avions
- `FlightRepository` : Accès aux données de vol

**Configuration** :
- `AsyncConfig` : Active les méthodes asynchrones (@Async)
- `WebSocketConfig` : Configuration WebSocket existante
- `SecurityConfig` : Protection des endpoints

### Frontend

**Composants** :
- `PilotDashboard.jsx` : Dashboard pilote avec simulation
- `AdminDashboard.jsx` : Dashboard admin avec tableau des vols

**Hooks** :
- WebSocket intégré directement dans `PilotDashboard.jsx`
- Utilisation de `@stomp/stompjs` et `sockjs-client`

---

## 🧪 Tests et Validation

### Tests Manuels Recommandés

1. **Test du flux complet** :
   - Se connecter en tant que pilote
   - Demander autorisation de décollage
   - Vérifier que le bouton "Décoller" apparaît
   - Sélectionner un aéroport de destination
   - Cliquer sur "Décoller"
   - Vérifier que la simulation démarre
   - Vérifier que les données se mettent à jour en temps réel

2. **Test du dashboard admin** :
   - Se connecter en tant qu'admin
   - Vérifier que le tableau des vols s'affiche
   - Vérifier que les données sont correctes
   - Vérifier le rafraîchissement automatique

3. **Test des permissions** :
   - Tenter de lancer une simulation avec un pilote non autorisé
   - Vérifier que l'erreur 403 est retournée

---

## 📝 Notes d'Implémentation

### Calcul de Trajectoire

La trajectoire est actuellement calculée comme une **ligne droite** entre les deux aéroports. Pour une simulation plus réaliste, on pourrait :
- Utiliser des waypoints intermédiaires
- Suivre des routes aériennes réelles
- Prendre en compte les restrictions d'espace aérien

### Performance

- Les simulations sont exécutées de manière **asynchrone** pour ne pas bloquer le thread principal
- Les mises à jour sont envoyées via WebSocket pour réduire la charge HTTP
- Le cache est utilisé pour optimiser les requêtes

### Limitations Actuelles

1. **Trajectoire simplifiée** : Ligne droite uniquement
2. **Pas de gestion du vent** : La vitesse est constante en croisière
3. **Pas de gestion du trafic** : Pas de détection de conflits en vol
4. **Pas de gestion du carburant** : Pas de calcul de consommation

---

## 🚀 Prochaines Améliorations Possibles

1. **Trajectoires réalistes** :
   - Intégration d'une API d'aéroports (OurAirports, OpenAIP)
   - Calcul de routes aériennes avec waypoints
   - Prise en compte des restrictions d'espace aérien

2. **Simulation avancée** :
   - Gestion du vent (vent de face/vent arrière)
   - Calcul de consommation de carburant
   - Gestion de la météo en temps réel

3. **Détection de conflits** :
   - Détection en temps réel pendant le vol
   - Alertes automatiques
   - Suggestions de changement de cap/altitude

4. **Historique des vols** :
   - Stockage des trajectoires réelles
   - Analyse des performances
   - Statistiques de vol

---

## 📚 Références

- **Documentation Spring WebSocket** : https://docs.spring.io/spring-framework/reference/web/websocket.html
- **STOMP Protocol** : https://stomp.github.io/
- **OurAirports Data** : https://ourairports.com/data/
- **OpenAIP** : https://www.openaip.net/

---

## ✅ Checklist de Déploiement

- [x] Service de simulation créé
- [x] Endpoints API créés
- [x] WebSocket intégré
- [x] Dashboard pilote mis à jour
- [x] Dashboard admin mis à jour
- [x] Sécurité et permissions configurées
- [x] Base de données mise à jour
- [ ] Tests unitaires (à créer)
- [ ] Tests d'intégration (à créer)
- [ ] Documentation API (Swagger) (à mettre à jour)

---

**Date de création** : 2026-01-15  
**Version** : 1.0.0  
**Auteur** : AI Assistant

