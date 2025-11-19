# 📘 Guide d'Utilisation Complet - PFA 2026

## 🚀 Démarrage Rapide

### 1. Démarrer le Backend

```powershell
cd backend
mvn spring-boot:run
```

Le backend démarre sur `http://localhost:8080`

### 2. Démarrer le Frontend

```powershell
cd frontend
npm install  # Si première fois
npm run dev
```

Le frontend démarre sur `http://localhost:3000` ou `http://localhost:3001`

## 🔐 Authentification

### Rôles Disponibles

1. **ADMIN** - Accès complet à tous les dashboards et gestion des comptes
2. **CENTRE_RADAR** - Dashboard radar avec vue secteur, ATIS, communications ATC
3. **PILOTE** - Dashboard pilote avec informations de vol, météo, communications

### Comptes par Défaut

Les comptes par défaut sont créés automatiquement au démarrage via `DataInitializer.java`.

Pour créer un nouveau compte (ADMIN seulement) :
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "nouveau_user",
  "password": "motdepasse",
  "role": "PILOTE",
  "pilotId": 1  // Optionnel selon le rôle
}
```

## 📊 Dashboards

### Dashboard ADMIN

**URL:** `/admin`

**Fonctionnalités:**
- ✅ KPIs temps réel (avions en vol, pilotes connectés, décollages/atterrissages)
- ✅ Graphiques trafic par aéroport
- ✅ Charge des centres radar
- ✅ Statistiques de retards
- ✅ Indicateurs de sécurité
- ✅ Performance ATC

**Endpoints Backend:**
- `GET /api/admin/dashboard` - Dashboard complet
- `GET /api/admin/kpis` - KPIs temps réel
- `GET /api/admin/statistics` - Statistiques performance

### Dashboard RADAR

**URL:** `/radar`

**Fonctionnalités:**
- ✅ Carte radar interactive (secteur 50 km)
- ✅ Position des avions en temps réel
- ✅ Données ATIS (vent, visibilité, pression, température)
- ✅ Liste des avions dans le secteur
- ✅ Historique des communications ATC

**Endpoints Backend:**
- `GET /api/radar/dashboard` - Dashboard complet
- `GET /api/radar/dashboard/aircraft` - Avions dans le secteur
- `GET /api/radar/dashboard/atis` - Données ATIS

**Prérequis:**
- L'utilisateur RADAR doit avoir un `airportId` associé dans la table `users`

### Dashboard PILOTE

**URL:** `/pilot`

**Fonctionnalités:**
- ✅ Informations générales du vol
- ✅ Position & mouvement (latitude, longitude, altitude, vitesse, cap)
- ✅ Statut du vol
- ✅ Météo du vol
- ✅ Communications ATC
- ✅ Sécurité / ADS-B Tracking
- ✅ KPIs
- ✅ Bouton "Demander autorisation de décollage"

**Endpoints Backend:**
- `GET /api/pilots/{username}/dashboard` - Dashboard complet
- `POST /api/atc/request-takeoff-clearance` - Demander autorisation décollage

**Prérequis:**
- L'utilisateur PILOTE doit avoir un `pilotId` associé dans la table `users`
- Le pilote doit avoir un avion assigné (`assignedAircraftId` dans la table `pilots`)

## 🔧 API Gestion Comptes (ADMIN seulement)

### Créer un compte

```http
POST /api/auth/register
Authorization: Bearer <token_admin>
Content-Type: application/json

{
  "username": "nouveau_user",
  "password": "motdepasse",
  "role": "PILOTE",
  "pilotId": 1
}
```

### Lister tous les utilisateurs

```http
GET /api/auth/users
Authorization: Bearer <token_admin>
```

### Modifier un utilisateur

```http
PUT /api/auth/users/{id}
Authorization: Bearer <token_admin>
Content-Type: application/json

{
  "username": "nouveau_username",
  "role": "CENTRE_RADAR",
  "airportId": 1
}
```

### Supprimer un utilisateur

```http
DELETE /api/auth/users/{id}
Authorization: Bearer <token_admin>
```

## 📡 WebSockets

Les WebSockets sont disponibles pour les mises à jour en temps réel :

- `/ws` - Endpoint WebSocket principal
- `/topic/aircraft` - Positions des avions
- `/topic/weather-alerts` - Alertes météo
- `/topic/conflicts` - Conflits détectés

## 🗄️ Base de Données

### Tables Principales

- `users` - Utilisateurs (ADMIN, CENTRE_RADAR, PILOTE)
- `pilots` - Pilotes
- `aircraft` - Avions
- `airports` - Aéroports
- `radar_centers` - Centres radar
- `flights` - Vols
- `atc_messages` - Messages ATC
- `atis_data` - Données ATIS
- `weather_data` - Données météo

### Relations

- `users.airport_id` → `airports.id` (si rôle = CENTRE_RADAR)
- `users.pilot_id` → `pilots.id` (si rôle = PILOTE)
- `pilots.assigned_aircraft_id` → `aircraft.id`
- `aircraft.pilot_id` → `pilots.id`
- `aircraft.airport_id` → `airports.id`
- `radar_centers.airport_id` → `airports.id`

## 🔒 Sécurité

### JWT Token

- **Durée de vie:** 24 heures
- **Header:** `Authorization: Bearer <token>`
- **Validation:** Automatique via `JwtAuthenticationFilter`

### Protection par Rôle

- Routes `/api/admin/**` → `hasRole('ADMIN')`
- Routes `/api/radar/**` → `hasAnyRole('CENTRE_RADAR', 'ADMIN')`
- Routes `/api/pilots/**` → `hasAnyRole('PILOTE', 'ADMIN')`

## 🧪 Tests

### Test API avec PowerShell

```powershell
# Login
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
  -Method POST -ContentType "application/json" `
  -Body '{"username":"admin","password":"admin"}'
$token = $response.token

# Dashboard Admin
Invoke-RestMethod -Uri "http://localhost:8080/api/admin/dashboard" `
  -Headers @{Authorization="Bearer $token"}

# Dashboard Radar
Invoke-RestMethod -Uri "http://localhost:8080/api/radar/dashboard" `
  -Headers @{Authorization="Bearer $token"}
```

## 📝 Notes Importantes

1. **Premier démarrage:** Les données initiales sont créées automatiquement via `DataInitializer.java`

2. **Assignation Avion-Pilote:** 
   - Utiliser le script SQL `FIX_ASSIGNER_AVION.sql` ou
   - Utiliser le script PowerShell `VERIFIER_ET_ASSIGNER_AVION.ps1`

3. **Météo:** Les données météo sont récupérées automatiquement via Open-Meteo API (gratuit, pas de clé API)

4. **Rafraîchissement:** 
   - Dashboard Admin: 10 secondes
   - Dashboard Radar: 5 secondes
   - Dashboard Pilote: 5 secondes

5. **CORS:** Configuré pour `http://localhost:3000` et `http://localhost:3001`

## 🐛 Dépannage

### Erreur "Token JWT invalide"
- Vérifier que le token est inclus dans le header `Authorization: Bearer <token>`
- Vérifier que le token n'est pas expiré (24h)

### Dashboard vide
- Vérifier que l'utilisateur a les bonnes associations (airportId pour RADAR, pilotId pour PILOTE)
- Vérifier que les données existent en base de données

### Erreur CORS
- Vérifier que le frontend tourne sur `http://localhost:3000` ou `http://localhost:3001`
- Vérifier la configuration dans `SecurityConfig.java`

## 📚 Documentation Technique

- `PLAN_ARCHITECTURE_COMPLETE.md` - Plan d'architecture détaillé
- `ETAT_IMPLEMENTATION.md` - État d'implémentation
- `RESUME_ARCHITECTURE_COMPLETE.md` - Résumé architecture

