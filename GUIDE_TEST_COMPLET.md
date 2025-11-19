# 🧪 Guide de Test Complet - PFA 2026

## 📋 Prérequis

### 1. Services à démarrer

```bash
# 1. PostgreSQL (doit être démarré)
# Vérifier avec : Get-Service -Name "*postgres*"

# 2. Backend Spring Boot
cd backend
mvn spring-boot:run
# Attendre le message : "Started FlightRadarApplication"

# 3. Frontend React
cd frontend
npm run dev
# Attendre : "Local: http://localhost:3000" ou "http://localhost:3001"
```

### 2. Comptes de test

Vérifier que les comptes suivants existent dans la base de données :

**Pilote :**
- Username: `pilote_cmn1`
- Password: `pilote123`
- Rôle: `PILOTE`
- Doit avoir un avion assigné

**Radar :**
- Username: `radar_cmn1`
- Password: `radar123`
- Rôle: `CENTRE_RADAR`

**Admin :**
- Username: `admin`
- Password: `admin123`
- Rôle: `ADMIN`

---

## 🧪 Tests Backend (API)

### Test 1 : Liaison Pilote ⇄ Avion

**Endpoint :** `GET /api/pilots/{username}/aircraft`

**Test avec Postman/curl :**

```bash
# Récupérer l'avion d'un pilote
curl -X GET "http://localhost:8080/api/pilots/pilote_cmn1/aircraft" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Vérifications :**
- ✅ Status 200 OK
- ✅ Retourne les données de l'avion
- ✅ Contient `pilotId` ou `usernamePilote`

**Endpoint :** `GET /api/aircraft/pilot/{username}`

```bash
curl -X GET "http://localhost:8080/api/aircraft/pilot/pilote_cmn1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Vérifications :**
- ✅ Status 200 OK
- ✅ Retourne l'avion du pilote

---

### Test 2 : Dashboard Pilote Complet

**Endpoint :** `GET /api/pilots/{username}/dashboard`

**Test :**

```bash
curl -X GET "http://localhost:8080/api/pilots/pilote_cmn1/dashboard" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

**Vérifications :**

1. **Informations générales du vol** ✅
   - `flightNumber` présent
   - `airline` présent
   - `aircraftType` présent
   - `route` au format "CMN → RAK"

2. **Position & mouvement (ADS-B)** ✅
   - `latitude`, `longitude` présents
   - `altitude`, `altitudeFeet` présents
   - `groundSpeed`, `airSpeed` présents
   - `heading` présent
   - `verticalSpeed` présent

3. **Statut du vol** ✅
   - `flightStatus` présent
   - `actualDeparture`, `actualArrival` présents
   - `scheduledDeparture`, `scheduledArrival` présents
   - `delayMinutes` calculé correctement

4. **Météo du vol** ✅
   - `weather` objet présent
   - `weather.windSpeed`, `weather.windDirection` présents
   - `weather.visibility` présent
   - `weather.temperature`, `weather.pressure` présents

5. **Communications ATC** ✅
   - `lastATCMessage` présent
   - `currentInstructions` array présent
   - `radarCenterName` présent
   - `atcHistory` array présent

6. **Sécurité / Suivi ADS-B** ✅
   - `transponderCode` présent
   - `trajectory` array présent
   - `alerts` array présent
   - `riskLevel` présent

7. **KPIs** ✅
   - `kpis` objet présent
   - `kpis.remainingDistance` présent
   - `kpis.estimatedArrival` présent
   - `kpis.weatherSeverity` présent
   - `kpis.trafficDensity30km` présent
   - `kpis.aircraftHealthScore` présent

**Réponse attendue (exemple) :**

```json
{
  "flightNumber": "AT1001",
  "airline": "Royal Air Maroc",
  "aircraftType": "A320",
  "route": "CMN → RAK",
  "latitude": 33.5731,
  "longitude": -7.5898,
  "altitude": 1000.0,
  "altitudeFeet": 3280.84,
  "groundSpeed": 800.0,
  "airSpeed": 820.0,
  "heading": 45.0,
  "verticalSpeed": 5.0,
  "flightStatus": "En vol",
  "weather": { ... },
  "kpis": { ... }
}
```

---

### Test 3 : Demander Autorisation de Décollage

**Endpoint :** `POST /api/atc/request-takeoff-clearance`

**Test :**

```bash
curl -X POST "http://localhost:8080/api/atc/request-takeoff-clearance" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "aircraftId": 1
  }'
```

**Scénarios de test :**

#### Scénario 1 : Conditions favorables (GRANTED)

**Prérequis :**
- Avion au sol (`status = AU_SOL`)
- Piste libre
- Météo favorable (visibilité > 550m, vent < 55 km/h)
- Pas de trafic proche
- Pas d'alertes météo critiques

**Réponse attendue :**

```json
{
  "status": "GRANTED",
  "message": "Autorisation de décollage accordée",
  "details": "Toutes les conditions sont remplies. Vous pouvez décoller.",
  "timestamp": "2026-01-15T10:30:00"
}
```

#### Scénario 2 : Piste occupée (PENDING)

**Prérequis :**
- Un autre avion en train de décoller ou d'atterrir

**Réponse attendue :**

```json
{
  "status": "PENDING",
  "message": "Piste occupée. Veuillez patienter.",
  "details": "Un autre avion est en train de décoller ou d'atterrir.",
  "timestamp": "2026-01-15T10:30:00"
}
```

#### Scénario 3 : Conditions météo défavorables (REFUSED)

**Prérequis :**
- Visibilité < 550m OU vent > 55 km/h OU vent travers > 28 km/h

**Réponse attendue :**

```json
{
  "status": "REFUSED",
  "message": "Conditions météo défavorables",
  "details": "Visibilité insuffisante: 0.40 km (minimum requis: 0.55 km)",
  "timestamp": "2026-01-15T10:30:00"
}
```

#### Scénario 4 : Trafic aérien dense (PENDING)

**Prérequis :**
- Avion proche à moins de 5.5 km

**Réponse attendue :**

```json
{
  "status": "PENDING",
  "message": "Trafic aérien dense. Veuillez patienter.",
  "details": "Avion trop proche: 4.20 km (séparation minimale: 5.50 km)",
  "timestamp": "2026-01-15T10:30:00"
}
```

#### Scénario 5 : Alertes météo critiques (REFUSED)

**Prérequis :**
- Tempête, cisaillement de vent, ou turbulence sévère

**Réponse attendue :**

```json
{
  "status": "REFUSED",
  "message": "Alertes météo critiques détectées",
  "details": "Tempête détectée, Cisaillement de vent possible",
  "timestamp": "2026-01-15T10:30:00"
}
```

---

### Test 4 : Règles ICAO/FAA

**Vérifier que les règles sont correctement appliquées :**

1. **Visibilité minimale :** 550m (0.55 km)
   - Tester avec visibilité = 0.40 km → REFUSED
   - Tester avec visibilité = 0.60 km → GRANTED (si autres conditions OK)

2. **Vent maximum :** 55 km/h
   - Tester avec vent = 60 km/h → REFUSED
   - Tester avec vent = 50 km/h → GRANTED (si autres conditions OK)

3. **Vent travers maximum :** 28 km/h
   - Tester avec vent travers = 30 km/h → REFUSED
   - Tester avec vent travers = 25 km/h → GRANTED (si autres conditions OK)

4. **Distance minimale entre avions :** 5.5 km
   - Tester avec distance = 4.0 km → PENDING
   - Tester avec distance = 6.0 km → GRANTED (si autres conditions OK)

---

## 🧪 Tests Frontend

### Test 1 : Connexion et Redirection

**Étapes :**

1. Ouvrir `http://localhost:3000` ou `http://localhost:3001`
2. Se connecter avec `pilote_cmn1` / `pilote123`
3. Vérifier la redirection automatique vers `/pilot`

**Vérifications :**
- ✅ Redirection vers `/pilot` après connexion
- ✅ Header affiche "Dashboard Pilote"
- ✅ Affiche le numéro de vol et la compagnie
- ✅ Affiche le username et le rôle

---

### Test 2 : Affichage Dashboard Complet

**Vérifications par section :**

#### Section 1 : Informations Générales du Vol ✅

- ✅ Numéro de vol affiché
- ✅ Compagnie aérienne affichée
- ✅ Type d'avion affiché
- ✅ Route "CMN → RAK" affichée
- ✅ Départ et arrivée affichés

#### Section 2 : Position & Mouvement (ADS-B) ✅

- ✅ Carte interactive affichée
- ✅ Marqueur de position visible
- ✅ Latitude/Longitude affichées sous la carte
- ✅ Altitude en pieds affichée
- ✅ Vitesse sol et vitesse air affichées
- ✅ Cap affiché
- ✅ Taux montée/descente affiché avec couleur :
  - Vert si montée
  - Rouge si descente
  - Gris si stable
- ✅ Code transpondeur affiché

#### Section 3 : Statut du Vol ✅

- ✅ Statut affiché avec couleur :
  - Vert : "En vol"
  - Jaune : "Au sol"
  - Rouge : "Atterrissage"
- ✅ Heures de départ/arrivée affichées
- ✅ Retard affiché en jaune si > 0
- ✅ Porte et piste affichées

#### Section 4 : Météo du Vol ✅

- ✅ Vent (vitesse et direction) affiché
- ✅ Visibilité affichée
- ✅ Précipitations affichées
- ✅ Turbulence affichée
- ✅ Température affichée
- ✅ Pression affichée
- ✅ Alertes météo affichées en rouge si présentes

#### Section 5 : Communications ATC ✅

- ✅ Dernier message ATC affiché en bleu
- ✅ Instructions en cours listées
- ✅ Centre radar affiché
- ✅ Historique ATC scrollable
- ✅ Messages ATC en bleu
- ✅ Messages PILOT en vert
- ✅ Timestamps formatés correctement

#### Section 6 : Sécurité / Suivi ADS-B ✅

- ✅ Alertes affichées si présentes
- ✅ Code couleur selon sévérité :
  - Rouge : CRITICAL
  - Orange : HIGH
  - Jaune : MEDIUM
  - Gris : LOW

#### Section 7 : KPIs ✅

**KPIs Temps Réel :**
- ✅ Distance restante affichée
- ✅ ETA affiché
- ✅ Consommation carburant affichée
- ✅ Niveau carburant affiché
- ✅ Vitesse moyenne affichée
- ✅ Altitude stable : Vert si oui, Rouge si non
- ✅ Turbulence : Vert si aucune, Rouge si détectée

**KPIs Radar / Sécurité :**
- ✅ Sévérité météo affichée (0-100%)
- ✅ Risque trajectoire affiché (0-100)
- ✅ Densité trafic affichée
- ✅ Score santé avion affiché avec couleur :
  - Vert : ≥ 80
  - Jaune : 50-79
  - Rouge : < 50

---

### Test 3 : Bouton "Demander Autorisation de Décollage"

**Prérequis :**
- L'avion doit être au sol (`status = AU_SOL`)

**Étapes :**

1. Vérifier que le bouton est visible quand l'avion est au sol
2. Cliquer sur "✈️ Demander Autorisation de Décollage"
3. Observer l'état de chargement ("Envoi en cours...")
4. Vérifier l'affichage de la réponse

**Scénarios :**

#### Scénario 1 : Autorisation Accordée (GRANTED)

**Vérifications :**
- ✅ Message vert affiché
- ✅ Texte "✅ Autorisation Accordée"
- ✅ Message explicatif affiché
- ✅ Détails affichés

#### Scénario 2 : Autorisation Refusée (REFUSED)

**Vérifications :**
- ✅ Message rouge affiché
- ✅ Texte "❌ Autorisation Refusée"
- ✅ Raison du refus affichée
- ✅ Détails affichés

#### Scénario 3 : En Attente (PENDING)

**Vérifications :**
- ✅ Message jaune affiché
- ✅ Texte "⏳ En Attente"
- ✅ Message explicatif affiché
- ✅ Détails affichés

---

### Test 4 : Rafraîchissement Automatique

**Étapes :**

1. Ouvrir le dashboard
2. Ouvrir la console du navigateur (F12)
3. Observer les requêtes réseau
4. Vérifier qu'une requête est envoyée toutes les 5 secondes

**Vérifications :**
- ✅ Requête `GET /api/pilots/{username}/dashboard` toutes les 5 secondes
- ✅ Les données se mettent à jour automatiquement
- ✅ Pas d'erreurs dans la console

---

### Test 5 : Gestion d'Erreurs

**Scénarios :**

#### Scénario 1 : Pas d'avion assigné

**Étapes :**
1. Se connecter avec un pilote sans avion assigné

**Vérifications :**
- ✅ Message "Aucun avion assigné" affiché
- ✅ Message informatif affiché
- ✅ Pas d'erreur dans la console

#### Scénario 2 : Backend non accessible

**Étapes :**
1. Arrêter le backend
2. Recharger la page

**Vérifications :**
- ✅ Message d'erreur gracieux affiché
- ✅ Pas de crash de l'application
- ✅ Erreur loggée dans la console

---

## 🧪 Tests d'Intégration

### Test 1 : Flux Complet Décollage

**Étapes :**

1. **Connexion**
   - Se connecter avec un compte pilote
   - Vérifier la redirection vers `/pilot`

2. **Vérification Dashboard**
   - Vérifier que toutes les sections sont affichées
   - Vérifier que l'avion est "Au sol"

3. **Demande d'Autorisation**
   - Cliquer sur "Demander Autorisation de Décollage"
   - Vérifier la réponse (GRANTED, REFUSED, ou PENDING)

4. **Si GRANTED**
   - Vérifier que le statut change
   - Vérifier que les communications ATC sont mises à jour
   - Vérifier que les KPIs se mettent à jour

---

### Test 2 : Règles ICAO/FAA

**Tester chaque règle individuellement :**

1. **Visibilité minimale**
   - Modifier la visibilité dans la base de données
   - Tester la demande d'autorisation
   - Vérifier le refus si < 550m

2. **Vent maximum**
   - Modifier la vitesse du vent
   - Tester la demande d'autorisation
   - Vérifier le refus si > 55 km/h

3. **Vent travers**
   - Modifier le vent travers
   - Tester la demande d'autorisation
   - Vérifier le refus si > 28 km/h

4. **Distance minimale**
   - Créer un avion proche
   - Tester la demande d'autorisation
   - Vérifier le PENDING si < 5.5 km

---

## 📊 Checklist de Test

### Backend ✅

- [ ] Endpoint `/api/pilots/{username}/aircraft` fonctionne
- [ ] Endpoint `/api/pilots/{username}/dashboard` retourne toutes les données
- [ ] Endpoint `/api/atc/request-takeoff-clearance` fonctionne
- [ ] Règles ICAO/FAA appliquées correctement
- [ ] Gestion d'erreurs correcte
- [ ] Réponses JSON valides

### Frontend ✅

- [ ] Connexion et redirection fonctionnent
- [ ] Toutes les 7 sections affichées
- [ ] Carte interactive fonctionne
- [ ] Bouton "Demander Autorisation" fonctionne
- [ ] Affichage des réponses correct
- [ ] Rafraîchissement automatique fonctionne
- [ ] Gestion d'erreurs gracieuse
- [ ] Responsive (mobile + desktop)

### Intégration ✅

- [ ] Flux complet décollage fonctionne
- [ ] Règles ICAO/FAA testées
- [ ] Communications ATC fonctionnent
- [ ] KPIs calculés correctement

---

## 🐛 Dépannage

### Problème : "Aucun avion assigné"

**Solution :**
1. Vérifier dans la base de données que le pilote a un avion assigné
2. Vérifier la liaison `pilots.user_id` → `users.id`
3. Vérifier la liaison `aircraft.pilot_id` → `pilots.id`

### Problème : Dashboard vide

**Solution :**
1. Vérifier que le backend est démarré
2. Vérifier les logs du backend pour les erreurs
3. Vérifier la console du navigateur pour les erreurs
4. Vérifier que le JWT token est valide

### Problème : Autorisation toujours refusée

**Solution :**
1. Vérifier les conditions météo dans la base de données
2. Vérifier qu'il n'y a pas d'autres avions sur la piste
3. Vérifier les logs du backend pour les raisons du refus

---

## 📝 Rapport de Test

Après chaque test, noter :

- ✅ **Succès** : Test réussi
- ❌ **Échec** : Test échoué (noter la raison)
- ⚠️ **Partiel** : Test partiellement réussi (noter les détails)

**Template :**

```
Test: [Nom du test]
Date: [Date]
Résultat: ✅ / ❌ / ⚠️
Détails: [Description]
Erreurs: [Si applicable]
```

---

## 🚀 Prochaines Étapes

Après les tests :

1. **Corriger les bugs** identifiés
2. **Optimiser les performances** si nécessaire
3. **Améliorer l'UI/UX** selon les retours
4. **Documenter** les résultats des tests

