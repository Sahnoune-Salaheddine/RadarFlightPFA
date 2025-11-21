# 🚀 Guide de Démarrage Rapide - Tests

## 📋 Ordre de Démarrage

### 1. Démarrer PostgreSQL

```powershell
# Vérifier que PostgreSQL est démarré
Get-Service -Name "*postgres*"

# Si non démarré :
Start-Service postgresql-x64-16
```

### 2. Démarrer le Backend

**Option A : Script PowerShell (Recommandé)**
```powershell
.\DEMARRER_BACKEND.ps1
```

**Option B : Manuel**
```powershell
cd backend
mvn spring-boot:run
```

**Attendre :** `Started FlightRadarApplication`

### 3. Démarrer le Frontend (Nouveau Terminal)

**Option A : Script PowerShell (Recommandé)**
```powershell
.\DEMARRER_FRONTEND.ps1
```

**Option B : Manuel**
```powershell
cd frontend
npm run dev
```

**Attendre :** `Local: http://localhost:3000`

---

## 🧪 Tests Rapides

### Test 1 : Test Automatique (Recommandé)

```powershell
# Exécuter le test rapide
.\TEST_RAPIDE.ps1
```

**Ce script va :**
- ✅ Vérifier que le backend est accessible
- ✅ Tester le login
- ✅ Tester le dashboard
- ✅ Tester l'autorisation de décollage

### Test 2 : Test Complet

```powershell
# Exécuter tous les tests
.\test_api.ps1
```

### Test 3 : Test Manuel avec cURL

Voir `COMMANDES_CURL_TEST.md` pour les commandes détaillées.

---

## ✅ Vérifications

### Backend Accessible ?

```powershell
# Test simple
Invoke-RestMethod -Uri "http://localhost:8080/api/airports"
```

**Si ça fonctionne :** ✅ Backend OK  
**Si erreur :** ❌ Vérifier que le backend est démarré

### Frontend Accessible ?

Ouvrir dans le navigateur : `http://localhost:3000`

**Si la page s'affiche :** ✅ Frontend OK  
**Si erreur :** ❌ Vérifier que le frontend est démarré

---

## 🎯 Tests Frontend

1. **Ouvrir** : `http://localhost:3000`
2. **Se connecter** :
   - Username: `pilote_cmn1`
   - Password: `pilote123`
3. **Vérifier** :
   - ✅ Redirection vers `/pilot`
   - ✅ Dashboard affiché
   - ✅ Toutes les sections visibles
   - ✅ Bouton "Demander Autorisation" visible (si au sol)

---

## 📊 Résultats Attendus

### Test Backend ✅

```
🧪 Test Rapide des APIs
=======================

1️⃣  Vérification du Backend...
   ✅ Backend accessible

2️⃣  Test Login...
   ✅ Login réussi
   📝 Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

3️⃣  Test Dashboard...
   ✅ Dashboard récupéré
   📊 Vol: AT1001 | CMN → RAK
   📍 Position: 33.5731, -7.5898
   ✈️  Statut: En vol

4️⃣  Test Autorisation Décollage...
   ✅ Réponse reçue
   📋 Statut: GRANTED (ou REFUSED ou PENDING)
   💬 Message: Autorisation de décollage accordée

✅ Tests terminés!
```

---

## 🐛 Problèmes Courants

### Backend ne démarre pas

**Solution :**
1. Vérifier PostgreSQL : `Get-Service -Name "*postgres*"`
2. Vérifier le port 8080 : `netstat -ano | findstr :8080`
3. Vérifier les logs dans la console

### Frontend ne démarre pas

**Solution :**
1. Installer les dépendances : `cd frontend; npm install`
2. Vérifier Node.js : `node --version`

### Erreur 401 (Unauthorized)

**Solution :**
- Se reconnecter pour obtenir un nouveau token
- Vérifier les identifiants : `pilote_cmn1` / `pilote123`

### Erreur 404 (Not Found)

**Solution :**
- Vérifier que le pilote a un avion assigné
- Vérifier l'URL de l'endpoint

---

## 📝 Checklist

- [ ] PostgreSQL démarré
- [ ] Backend démarré (port 8080)
- [ ] Frontend démarré (port 3000)
- [ ] Test rapide réussi
- [ ] Dashboard frontend affiché
- [ ] Bouton autorisation fonctionne

---

## 🎉 Prêt !

Une fois tous les tests réussis, vous pouvez :
- ✅ Utiliser le dashboard pilote
- ✅ Tester les autorisations de décollage
- ✅ Vérifier les KPIs en temps réel
- ✅ Consulter les communications ATC
