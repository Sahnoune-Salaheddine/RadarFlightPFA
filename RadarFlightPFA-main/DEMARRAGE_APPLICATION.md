# 🚀 DÉMARRAGE DE L'APPLICATION

## ✅ ÉTAT ACTUEL

- ✅ **Backend** : Compilation réussie, application en cours d'exécution
- ✅ **PostgreSQL** : Service démarré, base de données `flightradar` créée
- ✅ **Frontend** : Démarrage en cours...

---

## 🌐 ACCÈS À L'APPLICATION

### Frontend
- **URL** : http://localhost:3000
- **Statut** : Démarrage en cours...

### Backend API
- **URL** : http://localhost:8080
- **Statut** : ✅ En cours d'exécution

---

## 🔐 IDENTIFIANTS DE CONNEXION

### Admin
- **Username** : `admin`
- **Password** : `admin123`
- **Dashboard** : Vue générale (tous les avions)

### Pilote (Casablanca 1)
- **Username** : `pilote_cmn1`
- **Password** : `pilote123`
- **Dashboard** : Dashboard Pilote professionnel

### Centre Radar (Casablanca)
- **Username** : `radar_cmn`
- **Password** : `radar123`
- **Dashboard** : Vue radar

---

## 📋 FONCTIONNALITÉS DISPONIBLES

### ✅ Détection Automatique de Conflits
- Détection toutes les 5 secondes
- Alertes automatiques
- Messages VHF envoyés aux pilotes

### ✅ Autorisation Décollage/Atterrissage
- Vérification automatique piste + météo
- Messages VHF automatiques

### ✅ Dashboard Pilote
- Carte interactive
- Infos de vol en temps réel
- Messages VHF
- Météo
- Alertes

---

## 🧪 TESTS RAPIDES

### 1. Tester l'API Backend
```powershell
# Tester les aéroports
curl http://localhost:8080/api/airports

# Tester les avions
curl http://localhost:8080/api/aircraft

# Tester les conflits
curl http://localhost:8080/api/conflicts
```

### 2. Tester le Frontend
1. Ouvrir http://localhost:3000
2. Se connecter avec `pilote_cmn1` / `pilote123`
3. Vérifier le dashboard pilote
4. Tester "Demander Autorisation Décollage"

---

## ⚠️ NOTES

- **OpenSky API** : Limite de requêtes atteinte (429). Le cache est utilisé.
- **WebSocket** : Configuré et fonctionnel sur `/ws`
- **Polling** : Frontend utilise polling toutes les 5 secondes (fonctionne sans WebSocket)

---

**Date** : 2026  
**Statut** : ✅ Application en cours de démarrage

