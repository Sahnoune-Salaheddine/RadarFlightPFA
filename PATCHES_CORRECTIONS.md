# 🔧 PATCHES DE CORRECTION - Flight Radar 2026

## 📋 RÉSUMÉ ACTIONNABLE

**Objectif** : Supprimer toutes les anciennes entités/services/contrôleurs en français et utiliser uniquement les nouvelles entités en anglais (Airport, Aircraft, Pilot, RadarCenter, WeatherData, Communication, Flight, User, Runway).

**Actions** :
1. Supprimer 13 fichiers backend (anciennes entités, repositories, services, contrôleurs)
2. Corriger pom.xml (ligne 18 : `<n>` → `<name>`)
3. Supprimer 2 fichiers frontend (AvionList.jsx, MeteoPanel.jsx - déjà remplacés)
4. Vérifier que DataInitializer utilise les nouvelles entités (déjà corrigé)
5. Vérifier que tous les imports pointent vers les nouvelles entités

**Impact** : Le projet sera compilable, démarrable et fonctionnel avec un seul set d'entités cohérent.

---

## 🗑️ FICHIERS À SUPPRIMER

### Backend - Anciennes Entités (5 fichiers)
```
backend/src/main/java/com/flightradar/model/Aeroport.java
backend/src/main/java/com/flightradar/model/Avion.java
backend/src/main/java/com/flightradar/model/Pilote.java
backend/src/main/java/com/flightradar/model/CentreRadar.java
backend/src/main/java/com/flightradar/model/Meteo.java
```

**Justification** : Ces entités sont remplacées par Airport, Aircraft, Pilot, RadarCenter, WeatherData.

### Backend - Anciens Repositories (5 fichiers)
```
backend/src/main/java/com/flightradar/repository/AeroportRepository.java
backend/src/main/java/com/flightradar/repository/AvionRepository.java
backend/src/main/java/com/flightradar/repository/PiloteRepository.java
backend/src/main/java/com/flightradar/repository/CentreRadarRepository.java
backend/src/main/java/com/flightradar/repository/MeteoRepository.java
```

**Justification** : Ces repositories référencent les anciennes entités supprimées.

### Backend - Anciens Services (2 fichiers)
```
backend/src/main/java/com/flightradar/service/AvionService.java
backend/src/main/java/com/flightradar/service/MeteoService.java
```

**Justification** : Remplacés par AircraftService et WeatherService.

### Backend - Anciens Contrôleurs (3 fichiers)
```
backend/src/main/java/com/flightradar/controller/AvionController.java
backend/src/main/java/com/flightradar/controller/AeroportController.java
backend/src/main/java/com/flightradar/controller/MeteoController.java
```

**Justification** : Remplacés par AircraftController, AirportController, WeatherController.

### Backend - Ancien Service Communication (1 fichier)
```
backend/src/main/java/com/flightradar/service/CommunicationService.java
```

**Justification** : Remplacé par RadarService.

### Frontend - Anciens Composants (2 fichiers)
```
frontend/src/components/AvionList.jsx
frontend/src/components/MeteoPanel.jsx
```

**Justification** : Remplacés par AircraftList.jsx et WeatherPanel.jsx.

**Total à supprimer : 18 fichiers**

---

## 📝 COMMANDES GIT POUR SUPPRESSION

```bash
# Backend - Entités
git rm backend/src/main/java/com/flightradar/model/Aeroport.java
git rm backend/src/main/java/com/flightradar/model/Avion.java
git rm backend/src/main/java/com/flightradar/model/Pilote.java
git rm backend/src/main/java/com/flightradar/model/CentreRadar.java
git rm backend/src/main/java/com/flightradar/model/Meteo.java

# Backend - Repositories
git rm backend/src/main/java/com/flightradar/repository/AeroportRepository.java
git rm backend/src/main/java/com/flightradar/repository/AvionRepository.java
git rm backend/src/main/java/com/flightradar/repository/PiloteRepository.java
git rm backend/src/main/java/com/flightradar/repository/CentreRadarRepository.java
git rm backend/src/main/java/com/flightradar/repository/MeteoRepository.java

# Backend - Services
git rm backend/src/main/java/com/flightradar/service/AvionService.java
git rm backend/src/main/java/com/flightradar/service/MeteoService.java
git rm backend/src/main/java/com/flightradar/service/CommunicationService.java

# Backend - Contrôleurs
git rm backend/src/main/java/com/flightradar/controller/AvionController.java
git rm backend/src/main/java/com/flightradar/controller/AeroportController.java
git rm backend/src/main/java/com/flightradar/controller/MeteoController.java

# Frontend
git rm frontend/src/components/AvionList.jsx
git rm frontend/src/components/MeteoPanel.jsx
```

---

## 🔧 PATCHES / FICHIERS MODIFIÉS

### 1. backend/pom.xml

**Type** : MODIFY

**Raison** : Correction de la balise XML invalide `<n>` en `<name>` pour que Maven puisse parser le POM.

**Patch** :
```diff
--- a/backend/pom.xml
+++ b/backend/pom.xml
@@ -15,7 +15,7 @@
     <groupId>com.flightradar</groupId>
     <artifactId>flightradar-backend</artifactId>
     <version>1.0.0</version>
-    <n>Flight Radar Backend</n>
+    <name>Flight Radar Backend</name>
     <description>Backend API for Flight Radar Application</description>
```

**Contenu complet** (ligne 18 uniquement) :
```xml
    <name>Flight Radar Backend</name>
```

---

### 2. backend/src/main/java/com/flightradar/config/DataInitializer.java

**Type** : MODIFY (déjà corrigé, vérification)

**Raison** : Utilise maintenant les nouvelles entités (Airport, Aircraft, Pilot, RadarCenter) au lieu des anciennes.

**Statut** : ✅ Déjà corrigé dans les modifications précédentes. Vérifier qu'il n'y a plus d'imports des anciennes entités.

**Vérification** :
```bash
grep -E "import.*Aeroport|import.*Avion|import.*Pilote|import.*CentreRadar|import.*Meteo" backend/src/main/java/com/flightradar/config/DataInitializer.java
```
**Attendu** : Aucune ligne (0 résultats)

---

### 3. backend/src/main/resources/application.properties

**Type** : MODIFY (vérification uniquement)

**Raison** : Vérifier que la configuration est correcte pour PostgreSQL et l'API météo.

**Contenu actuel** : ✅ Correct, aucune modification nécessaire.

**Note** : La clé API météo est `your-openweathermap-api-key` - à remplacer par une vraie clé ou laisser vide pour utiliser le fallback.

---

### 4. frontend/src/services/api.js

**Type** : MODIFY (vérification uniquement)

**Raison** : Vérifier que l'URL de base est correcte.

**Contenu actuel** : ✅ Correct, aucune modification nécessaire.

---

## ✅ FICHIERS DÉJÀ CORRIGÉS (VÉRIFICATION)

Les fichiers suivants ont déjà été corrigés dans les modifications précédentes :

1. ✅ `backend/src/main/java/com/flightradar/config/DataInitializer.java` - Utilise les nouvelles entités
2. ✅ `frontend/src/components/FlightMap.jsx` - Utilise `/api/aircraft` et `/api/airports`
3. ✅ `frontend/src/components/AircraftList.jsx` - Nouveau composant créé
4. ✅ `frontend/src/components/WeatherPanel.jsx` - Nouveau composant créé
5. ✅ `frontend/src/components/Dashboard.jsx` - Utilise les nouveaux composants
6. ✅ `frontend/src/components/CommunicationPanel.jsx` - Utilise `/api/radar/*`
7. ✅ `frontend/src/components/AlertPanel.jsx` - Utilise `/api/weather/alerts`

---

## 🧪 COMMANDES DE VÉRIFICATION

### 1. Vérifier la compilation backend

```bash
cd backend
mvn clean compile
```

**Attendu** :
```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

**Regex à chercher** : `BUILD SUCCESS`

---

### 2. Vérifier le démarrage backend

```bash
cd backend
mvn spring-boot:run
```

**Attendu** (dans les logs) :
```
Started FlightRadarApplication in X.XXX seconds
```

**Regex à chercher** : `Started FlightRadarApplication`

**Timeout** : Attendre 30-60 secondes pour le démarrage complet.

---

### 3. Vérifier l'initialisation de la base de données

```bash
psql -d flightradar -c "SELECT COUNT(*) FROM airports;"
psql -d flightradar -c "SELECT COUNT(*) FROM aircraft;"
psql -d flightradar -c "SELECT COUNT(*) FROM pilots;"
psql -d flightradar -c "SELECT COUNT(*) FROM users;"
```

**Attendu** :
```
 airports | 4
 aircraft | 8
 pilots   | 8
 users    | 13 (1 admin + 4 radar + 8 pilots)
```

---

### 4. Vérifier le frontend

```bash
cd frontend
npm install
npm run dev
```

**Attendu** :
```
  VITE v5.x.x  ready in XXX ms

  ➜  Local:   http://localhost:3000/
```

**Vérification manuelle** : Ouvrir http://localhost:3000 et vérifier que la page de login s'affiche.

---

## 📡 EXEMPLES CURL POUR TESTER LES ENDPOINTS

### 1. POST /api/auth/login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Attendu** :
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "role": "ADMIN"
}
```

**Note** : Sauvegarder le token pour les requêtes suivantes si nécessaire.

---

### 2. GET /api/aircraft

```bash
curl -X GET http://localhost:8080/api/aircraft \
  -H "Content-Type: application/json"
```

**Attendu** :
```json
[
  {
    "id": 1,
    "registration": "CN-AT01",
    "model": "A320",
    "status": "AU_SOL",
    "positionLat": 33.3675,
    "positionLon": -7.5898,
    "altitude": 0.0,
    "speed": 0.0,
    "heading": 0.0
  },
  ...
]
```

**Vérification** : Doit retourner 8 avions.

---

### 3. GET /api/airports

```bash
curl -X GET http://localhost:8080/api/airports \
  -H "Content-Type: application/json"
```

**Attendu** :
```json
[
  {
    "id": 1,
    "name": "Aéroport Mohammed V",
    "city": "Casablanca",
    "codeIATA": "CMN",
    "latitude": 33.3675,
    "longitude": -7.5898
  },
  ...
]
```

**Vérification** : Doit retourner 4 aéroports.

---

### 4. GET /api/weather/airport/{id}

```bash
curl -X GET http://localhost:8080/api/weather/airport/1 \
  -H "Content-Type: application/json"
```

**Attendu** :
```json
{
  "id": 1,
  "airport": {
    "id": 1,
    "name": "Aéroport Mohammed V",
    "city": "Casablanca",
    "codeIATA": "CMN"
  },
  "windSpeed": 10.0,
  "windDirection": 180.0,
  "visibility": 10.0,
  "temperature": 20.0,
  "humidity": 60,
  "pressure": 1013.25,
  "conditions": "Clear",
  "crosswind": 5.0,
  "alert": false,
  "timestamp": "2026-01-01T10:00:00"
}
```

**Note** : Si l'API OpenWeatherMap n'est pas configurée, retournera des données par défaut.

---

### 5. GET /api/radar/messages

```bash
curl -X GET "http://localhost:8080/api/radar/messages?radarCenterId=1" \
  -H "Content-Type: application/json"
```

**Attendu** :
```json
[
  {
    "id": 1,
    "senderType": "RADAR",
    "senderId": 1,
    "receiverType": "AIRCRAFT",
    "receiverId": 1,
    "message": "Message de test",
    "frequency": 121.5,
    "timestamp": "2026-01-01T10:00:00"
  },
  ...
]
```

**Note** : Peut retourner un tableau vide si aucune communication n'a été créée.

---

## 🔍 CHECKS AUTOMATIQUES - RECHERCHE DE RÉFÉRENCES ORPHELINES

### 1. Vérifier qu'aucun code ne référence les anciennes entités

```bash
# Backend
grep -r "import.*Aeroport" backend/src/main/java --exclude-dir=target | grep -v "Airport"
grep -r "import.*Avion" backend/src/main/java --exclude-dir=target | grep -v "Aircraft"
grep -r "import.*Pilote" backend/src/main/java --exclude-dir=target | grep -v "Pilot"
grep -r "import.*CentreRadar" backend/src/main/java --exclude-dir=target | grep -v "RadarCenter"
grep -r "import.*Meteo" backend/src/main/java --exclude-dir=target | grep -v "WeatherData"
```

**Attendu** : Aucune ligne (0 résultats après suppression des fichiers)

---

### 2. Vérifier les noms de classes dans le code

```bash
# Chercher les utilisations directes des anciennes classes
grep -r "Aeroport\|Avion\|Pilote\|CentreRadar\|Meteo" backend/src/main/java --exclude-dir=target | grep -v "Airport\|Aircraft\|Pilot\|RadarCenter\|WeatherData"
```

**Attendu** : Aucune ligne (0 résultats)

---

### 3. Vérifier les endpoints dans le frontend

```bash
# Chercher les anciens endpoints
grep -r "/api/avions\|/api/aeroports\|/api/meteo" frontend/src
```

**Attendu** : Aucune ligne (0 résultats)

---

### 4. Vérifier les propriétés JSON dans le frontend

```bash
# Chercher les anciennes propriétés
grep -r "\.nom\|\.numeroVol\|\.modele\|\.vitesseVent\|\.visibilite\|\.alerteMeteo" frontend/src
```

**Attendu** : Aucune ligne (0 résultats)

---

## 📊 CHECKLIST DE VALIDATION FINALE

### Backend
- [ ] `mvn clean compile` réussit sans erreur
- [ ] `mvn spring-boot:run` démarre sans crash
- [ ] Les logs montrent "Started FlightRadarApplication"
- [ ] La base de données contient 4 aéroports
- [ ] La base de données contient 8 avions
- [ ] La base de données contient 8 pilotes
- [ ] La base de données contient 13 utilisateurs
- [ ] Aucune référence aux anciennes entités dans le code

### Frontend
- [ ] `npm install` réussit sans erreur
- [ ] `npm run dev` démarre sans erreur
- [ ] La page http://localhost:3000 s'affiche
- [ ] La page de login fonctionne
- [ ] La connexion avec admin/admin123 fonctionne
- [ ] La carte s'affiche avec les aéroports
- [ ] Les avions s'affichent sur la carte
- [ ] Les données météo s'affichent

### API
- [ ] `POST /api/auth/login` retourne un token
- [ ] `GET /api/aircraft` retourne 8 avions
- [ ] `GET /api/airports` retourne 4 aéroports
- [ ] `GET /api/weather/airport/1` retourne des données météo
- [ ] `GET /api/radar/messages` fonctionne (peut être vide)

---

## 🎯 CONCLUSION / NIVEAU DE CONFIANCE

### ✅ OK POUR DÉMARRAGE

**Raison** :
- ✅ DataInitializer utilise les nouvelles entités (déjà corrigé)
- ✅ Tous les composants frontend utilisent les nouveaux endpoints (déjà corrigé)
- ✅ Les nouveaux contrôleurs/services/repositories existent et sont fonctionnels
- ⚠️ Il reste à supprimer les 18 fichiers listés ci-dessus
- ⚠️ Il reste à corriger pom.xml ligne 18

**Actions requises** :
1. Exécuter les commandes `git rm` pour supprimer les 18 fichiers
2. Appliquer le patch pom.xml (ligne 18)
3. Exécuter `mvn clean compile` pour vérifier
4. Exécuter `mvn spring-boot:run` pour tester
5. Vérifier la base de données avec les commandes psql
6. Tester le frontend

**Temps estimé** : 10-15 minutes

**Risques** : Faible - Les fichiers à supprimer ne sont plus utilisés, les nouveaux fichiers sont déjà en place.

---

## 📝 NOTES FINALES

1. **Clé API Météo** : Si vous n'avez pas de clé OpenWeatherMap, le système utilisera des données par défaut (fallback).

2. **WebSocket** : Le backend est configuré pour WebSocket, mais le frontend utilise actuellement le polling (refresh toutes les 5 secondes). C'est fonctionnel et suffisant pour le projet.

3. **Base de données** : Si la base de données existe déjà avec les anciennes tables, vous devrez peut-être la supprimer et la recréer :
   ```bash
   dropdb flightradar
   createdb flightradar
   ```

4. **Tests supplémentaires** : Après avoir appliqué les corrections, tester manuellement :
   - Créer une communication via l'interface
   - Vérifier que les positions des avions se mettent à jour
   - Vérifier que les alertes météo s'affichent si conditions dangereuses

---

**Date** : 2026
**Version** : 1.0
**Statut** : ✅ Prêt pour application

