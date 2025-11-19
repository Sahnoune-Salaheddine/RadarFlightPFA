# 🚀 Démarrer le Frontend React

## ✅ ÉTAT ACTUEL

- ✅ Frontend installé (`npm install` terminé)
- ✅ Backend Spring Boot en cours d'exécution (port 8080)
- ✅ PostgreSQL configuré et fonctionnel
- ✅ Données initialisées (4 aéroports, 8 avions, etc.)

---

## 🎯 DÉMARRER LE FRONTEND

### Étape 1 : Vérifier que le backend tourne

Le backend doit être accessible sur `http://localhost:8080`

**Test rapide** :
```powershell
curl http://localhost:8080/api/airports
```

**Attendu** : JSON avec la liste des aéroports

---

### Étape 2 : Démarrer le frontend

```powershell
cd frontend
npm run dev
```

**Attendu** :
```
  VITE v5.0.8  ready in XXX ms

  ➜  Local:   http://localhost:3000/
  ➜  Network: use --host to expose
```

---

### Étape 3 : Ouvrir dans le navigateur

Ouvrir : **http://localhost:3000**

---

## 🔧 CONFIGURATION

### Backend API
- **URL** : `http://localhost:8080/api`
- **Configuré dans** : `frontend/src/services/api.js`
- **Proxy Vite** : `frontend/vite.config.js` (redirige `/api` vers `http://localhost:8080`)

### Ports
- **Frontend** : `3000` (Vite)
- **Backend** : `8080` (Spring Boot)
- **PostgreSQL** : `5432`

---

## 🔐 IDENTIFIANTS DE CONNEXION

### Admin
- **Username** : `admin`
- **Password** : `admin123`

### Centre Radar (Casablanca)
- **Username** : `radar_cmn`
- **Password** : `radar123`

### Pilote (Casablanca 1)
- **Username** : `pilote_cmn1`
- **Password** : `pilote123`

---

## ✅ VÉRIFICATION

### 1. Page de connexion accessible
- Ouvrir http://localhost:3000
- Voir la page de login

### 2. Connexion fonctionne
- Se connecter avec `admin` / `admin123`
- Redirection vers le dashboard

### 3. API fonctionne
- Ouvrir la console du navigateur (F12)
- Vérifier qu'il n'y a pas d'erreurs 404 ou CORS

### 4. Données affichées
- Voir les aéroports sur la carte
- Voir les avions sur la carte
- Voir les données météo

---

## 🐛 DÉPANNAGE

### Problème : Erreur CORS

**Solution** : Vérifier `backend/src/main/java/com/flightradar/config/SecurityConfig.java`
- CORS doit autoriser `http://localhost:3000`

### Problème : Erreur 404 sur `/api/...`

**Solution** : Vérifier que le backend tourne sur le port 8080
```powershell
# Tester
curl http://localhost:8080/api/airports
```

### Problème : Page blanche

**Solution** : Vérifier la console du navigateur (F12) pour les erreurs

### Problème : Endpoints français vs anglais

**Note** : Certains composants peuvent encore utiliser les anciens endpoints français (`/aeroports`, `/meteo`). Ils doivent être mis à jour vers les nouveaux endpoints anglais (`/airports`, `/weather`).

---

## 📋 COMMANDES UTILES

```powershell
# Démarrer le frontend
cd frontend
npm run dev

# Build de production
npm run build

# Prévisualiser le build
npm run preview

# Vérifier les vulnérabilités
npm audit

# Corriger les vulnérabilités (optionnel)
npm audit fix
```

---

## 🎯 PROCHAINES ÉTAPES

1. ✅ Démarrer le frontend : `npm run dev`
2. ✅ Ouvrir http://localhost:3000
3. ✅ Se connecter avec `admin` / `admin123`
4. ✅ Tester les fonctionnalités :
   - Carte interactive (avions, aéroports)
   - Données météo
   - Communications radar
   - Liste des avions

---

**Date** : 2026  
**Statut** : Prêt à démarrer

