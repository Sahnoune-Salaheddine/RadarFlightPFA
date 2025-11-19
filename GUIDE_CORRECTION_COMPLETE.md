# 🔧 Guide de Correction Complète - Flight Radar 2026

## 📊 RÉSUMÉ EXÉCUTIF

**Problème principal identifié** : Le projet contient **DEUX versions d'entités** (anciennes en français et nouvelles en anglais) qui entrent en conflit, empêchant le projet de fonctionner.

**Solution** : Supprimer toutes les anciennes entités et utiliser uniquement les nouvelles.

---

## ❌ PROBLÈMES CRITIQUES IDENTIFIÉS

### 🔴 CRITIQUE 1 : Double Set d'Entités JPA
- **Impact** : ❌ Le projet ne peut pas compiler/démarrer
- **Solution** : Supprimer toutes les anciennes entités

### 🔴 CRITIQUE 2 : DataInitializer Utilise les Anciennes Entités
- **Impact** : ❌ Crash au démarrage
- **Solution** : ✅ **CORRIGÉ** - Voir `DataInitializer.java`

### 🔴 CRITIQUE 3 : Contrôleurs REST Dupliqués
- **Impact** : ❌ Endpoints conflictuels
- **Solution** : Supprimer les anciens contrôleurs

### 🔴 CRITIQUE 4 : Frontend Utilise les Anciens Endpoints
- **Impact** : ❌ Les appels API échouent (404)
- **Solution** : ✅ **CORRIGÉ** - Tous les composants React mis à jour

### 🔴 CRITIQUE 5 : Noms de Champs Incohérents
- **Impact** : ❌ Les données ne s'affichent pas
- **Solution** : ✅ **CORRIGÉ** - Frontend utilise les nouveaux noms

### 🔴 CRITIQUE 6 : Erreur dans pom.xml
- **Impact** : ❌ Maven ne peut pas parser le POM
- **Solution** : Vérifier ligne 18 (`<n>` → `<name>`)

---

## ✅ MODIFICATIONS OBLIGATOIRES

### Phase 1 : Nettoyer le Backend (OBLIGATOIRE)

#### 1.1 Supprimer les Anciennes Entités
Supprimer ces fichiers :
```
backend/src/main/java/com/flightradar/model/Aeroport.java
backend/src/main/java/com/flightradar/model/Avion.java
backend/src/main/java/com/flightradar/model/Pilote.java
backend/src/main/java/com/flightradar/model/CentreRadar.java
backend/src/main/java/com/flightradar/model/Meteo.java
```

#### 1.2 Supprimer les Anciens Repositories
Supprimer ces fichiers :
```
backend/src/main/java/com/flightradar/repository/AeroportRepository.java
backend/src/main/java/com/flightradar/repository/AvionRepository.java
backend/src/main/java/com/flightradar/repository/PiloteRepository.java
backend/src/main/java/com/flightradar/repository/CentreRadarRepository.java
backend/src/main/java/com/flightradar/repository/MeteoRepository.java
```

#### 1.3 Supprimer les Anciens Services
Supprimer ces fichiers :
```
backend/src/main/java/com/flightradar/service/AvionService.java
backend/src/main/java/com/flightradar/service/MeteoService.java (si c'est l'ancien)
backend/src/main/java/com/flightradar/service/CommunicationService.java
```

#### 1.4 Supprimer les Anciens Contrôleurs
Supprimer ces fichiers :
```
backend/src/main/java/com/flightradar/controller/AvionController.java
backend/src/main/java/com/flightradar/controller/AeroportController.java
backend/src/main/java/com/flightradar/controller/MeteoController.java
backend/src/main/java/com/flightradar/controller/CommunicationController.java
```

#### 1.5 Vérifier pom.xml
Vérifier que ligne 18 contient `<name>` et non `<n>` :
```xml
<name>Flight Radar Backend</name>
```

#### 1.6 DataInitializer
✅ **DÉJÀ CORRIGÉ** - Utilise maintenant les nouvelles entités

---

### Phase 2 : Nettoyer le Frontend (OBLIGATOIRE)

#### 2.1 Supprimer les Anciens Composants
Supprimer ces fichiers :
```
frontend/src/components/AvionList.jsx
frontend/src/components/MeteoPanel.jsx
```

#### 2.2 Nouveaux Composants
✅ **DÉJÀ CRÉÉS** :
- `AircraftList.jsx` (remplace AvionList.jsx)
- `WeatherPanel.jsx` (remplace MeteoPanel.jsx)

#### 2.3 Composants Mis à Jour
✅ **DÉJÀ CORRIGÉS** :
- `FlightMap.jsx` - Utilise `/api/aircraft` et `/api/airports`
- `Dashboard.jsx` - Utilise les nouveaux composants
- `CommunicationPanel.jsx` - Utilise `/api/radar/*`
- `AlertPanel.jsx` - Utilise `/api/weather/alerts`

---

### Phase 3 : Vérifications (OBLIGATOIRE)

#### 3.1 Compiler le Backend
```bash
cd backend
mvn clean compile
```

Si erreurs :
- Vérifier que tous les imports pointent vers les nouvelles entités
- Vérifier que tous les anciens fichiers sont supprimés

#### 3.2 Tester le Backend
```bash
cd backend
mvn spring-boot:run
```

Vérifier :
- ✅ L'application démarre sans erreur
- ✅ Les tables sont créées dans PostgreSQL
- ✅ Les données sont initialisées (4 aéroports, 8 avions, etc.)

#### 3.3 Tester le Frontend
```bash
cd frontend
npm install
npm run dev
```

Vérifier :
- ✅ La page de login s'affiche
- ✅ La connexion fonctionne (admin/admin123)
- ✅ La carte s'affiche avec les aéroports
- ✅ Les avions s'affichent sur la carte
- ✅ Les données météo s'affichent

---

## 🎯 MAPPING DES CHANGEMENTS

### Endpoints API

| Ancien | Nouveau | Statut |
|--------|---------|--------|
| `/api/avions` | `/api/aircraft` | ✅ Corrigé |
| `/api/aeroports` | `/api/airports` | ✅ Corrigé |
| `/api/meteo/aeroport/{id}` | `/api/weather/airport/{id}` | ✅ Corrigé |
| `/api/meteo/alertes` | `/api/weather/alerts` | ✅ Corrigé |
| `/api/communications` | `/api/radar/messages` | ✅ Corrigé |

### Propriétés JSON

| Ancien | Nouveau | Statut |
|--------|---------|--------|
| `aeroport.nom` | `airport.name` | ✅ Corrigé |
| `aeroport.codeIATA` | `airport.codeIATA` | ✅ Corrigé |
| `avion.numeroVol` | `aircraft.registration` | ✅ Corrigé |
| `avion.modele` | `aircraft.model` | ✅ Corrigé |
| `avion.altitude` | `aircraft.altitude` | ✅ Corrigé |
| `avion.vitesse` | `aircraft.speed` | ✅ Corrigé |
| `avion.direction` | `aircraft.heading` | ✅ Corrigé |
| `avion.statut` | `aircraft.status` | ✅ Corrigé |
| `meteo.temperature` | `weather.temperature` | ✅ Corrigé |
| `meteo.vitesseVent` | `weather.windSpeed` | ✅ Corrigé |
| `meteo.visibilite` | `weather.visibility` | ✅ Corrigé |
| `meteo.ventTravers` | `weather.crosswind` | ✅ Corrigé |
| `meteo.alerteMeteo` | `weather.alert` | ✅ Corrigé |

---

## 🚀 AMÉLIORATIONS OPTIONNELLES

### 1. WebSocket Frontend (OPTIONNEL)
Actuellement, le frontend utilise le polling (refresh toutes les 5 secondes).
Pour utiliser WebSocket :
- Installer `@stomp/stompjs` et `sockjs-client`
- Implémenter un client STOMP
- Écouter les topics `/topic/aircraft` et `/topic/weather-alerts`

### 2. Gestion d'Erreurs (RECOMMANDÉ)
- Ajouter des try/catch avec messages utilisateur
- Afficher des toasts/notifications pour les erreurs
- Gérer les cas où l'API est indisponible

### 3. Loading States (RECOMMANDÉ)
- Ajouter des spinners pendant le chargement
- Afficher "Chargement..." pendant les requêtes

### 4. Validation (OPTIONNEL)
- Valider les formulaires côté frontend
- Ajouter validation côté backend avec `@Valid`

### 5. Tests (OPTIONNEL)
- Tests unitaires pour les services
- Tests d'intégration pour les contrôleurs
- Tests E2E pour le frontend

---

## 📋 CHECKLIST FINALE

### Backend
- [ ] Supprimer toutes les anciennes entités
- [ ] Supprimer tous les anciens repositories
- [ ] Supprimer tous les anciens services
- [ ] Supprimer tous les anciens contrôleurs
- [ ] Vérifier pom.xml
- [x] DataInitializer corrigé
- [ ] Compiler sans erreur
- [ ] Démarrer sans crash
- [ ] Vérifier que les données sont initialisées

### Frontend
- [ ] Supprimer AvionList.jsx
- [ ] Supprimer MeteoPanel.jsx
- [x] AircraftList.jsx créé
- [x] WeatherPanel.jsx créé
- [x] FlightMap.jsx corrigé
- [x] Dashboard.jsx corrigé
- [x] CommunicationPanel.jsx corrigé
- [x] AlertPanel.jsx corrigé
- [ ] Tester que tout fonctionne

### Tests Fonctionnels
- [ ] Authentification fonctionne
- [ ] Carte affiche les aéroports
- [ ] Carte affiche les avions
- [ ] Liste des avions fonctionne
- [ ] Météo s'affiche
- [ ] Alertes s'affichent
- [ ] Communications fonctionnent
- [ ] Positions se mettent à jour

---

## 🆘 EN CAS DE PROBLÈME

### Backend ne compile pas
1. Vérifier que tous les anciens fichiers sont supprimés
2. Vérifier les imports dans tous les fichiers
3. Exécuter `mvn clean` puis `mvn compile`

### Backend ne démarre pas
1. Vérifier que PostgreSQL est démarré
2. Vérifier les paramètres dans `application.properties`
3. Vérifier les logs pour les erreurs

### Frontend ne charge pas les données
1. Vérifier que le backend est démarré
2. Vérifier la console du navigateur (F12)
3. Vérifier que les endpoints sont corrects
4. Vérifier CORS dans SecurityConfig

### Base de données vide
1. Vérifier que DataInitializer s'exécute
2. Vérifier les logs au démarrage
3. Vérifier que `airportRepository.count() == 0` est vrai au premier démarrage

---

## 📞 SUPPORT

Si vous rencontrez des problèmes après avoir appliqué toutes les corrections :
1. Vérifier les logs backend (console)
2. Vérifier la console du navigateur (F12)
3. Vérifier que tous les fichiers listés sont supprimés
4. Vérifier que tous les nouveaux fichiers sont présents

---

## ✅ RÉSULTAT ATTENDU

Après avoir appliqué toutes les corrections :
- ✅ Le backend compile sans erreur
- ✅ Le backend démarre sans crash
- ✅ La base de données est initialisée avec 4 aéroports, 8 avions, etc.
- ✅ Le frontend se connecte au backend
- ✅ La carte affiche les aéroports et avions
- ✅ Les données météo s'affichent
- ✅ Les communications fonctionnent
- ✅ Les positions se mettent à jour toutes les 5 secondes

---

**Date de création** : 2026
**Dernière mise à jour** : Après analyse complète du projet

