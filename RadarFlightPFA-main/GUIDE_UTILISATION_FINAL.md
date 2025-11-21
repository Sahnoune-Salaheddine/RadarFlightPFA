# 🎯 GUIDE D'UTILISATION FINAL - Flight Radar 2026

## ✅ AMÉLIORATIONS COMPLÉTÉES

Toutes les améliorations majeures demandées ont été implémentées avec succès !

---

## 🚀 DÉMARRAGE RAPIDE

### 1. Backend
```powershell
cd backend
mvn clean compile
mvn spring-boot:run
```

**Vérification** :
- Application démarre sur `http://localhost:8080`
- Aucune erreur de compilation
- WebSocket configuré sur `/ws`

### 2. Frontend
```powershell
cd frontend
npm install
npm run dev
```

**Vérification** :
- Application démarre sur `http://localhost:3000`
- Page de login accessible

### 3. Base de données
```powershell
# Vérifier que PostgreSQL tourne
Get-Service -Name "*postgres*"
```

---

## 🔐 IDENTIFIANTS

### Admin
- Username : `admin`
- Password : `admin123`
- Dashboard : Général (tous les avions)

### Pilote (Casablanca 1)
- Username : `pilote_cmn1`
- Password : `pilote123`
- Dashboard : **Dashboard Pilote** (vue spécialisée)

### Centre Radar (Casablanca)
- Username : `radar_cmn`
- Password : `radar123`
- Dashboard : Général (vue radar)

---

## 🎮 FONCTIONNALITÉS IMPLÉMENTÉS

### ✅ Détection Automatique de Conflits
- **Fréquence** : Toutes les 5 secondes
- **Critères** :
  - Distance < 5 km
  - Différence altitude < 300 m
- **Actions** :
  - Génération d'alertes automatiques
  - Envoi de messages VHF aux pilotes
  - Broadcast WebSocket (`/topic/conflicts`)

### ✅ Autorisation Décollage/Atterrissage
- **Vérifications automatiques** :
  - Piste libre ?
  - Conditions météo favorables ?
- **Messages VHF** :
  - Autorisation accordée → Message vert
  - Autorisation refusée → Message rouge avec raison

### ✅ Dashboard Pilote Professionnel
- **Carte interactive** : Position de l'avion en temps réel
- **Infos de vol** : Vitesse, altitude, cap, position GPS
- **Météo** : Conditions de l'aéroport
- **Messages VHF** : Communications radar en temps réel
- **Alertes** : Conflits et météo
- **Bouton décollage** : Demande d'autorisation automatique

---

## 📡 ENDPOINTS API DISPONIBLES

### Conflits
- `GET /api/conflicts` : Liste des conflits actifs

### Radar
- `POST /api/radar/requestTakeoffClearance` : Demande décollage
- `POST /api/radar/requestLandingClearance` : Demande atterrissage
- `GET /api/radar/runwayStatus/{airportId}` : Statut piste
- `GET /api/radar/messages?radarCenterId={id}` : Messages radar
- `POST /api/radar/sendMessage` : Envoyer message

### Avions
- `GET /api/aircraft` : Tous les avions
- `GET /api/aircraft/{id}` : Détails d'un avion

### Météo
- `GET /api/weather/airport/{id}` : Météo d'un aéroport
- `GET /api/weather/alerts` : Alertes météo

---

## 🧪 TESTS RECOMMANDÉS

### Test 1 : Détection de Conflits
1. Se connecter en tant qu'admin
2. Mettre 2 avions en vol avec trajectoires proches
3. Vérifier que les alertes apparaissent
4. Vérifier que les messages VHF sont envoyés aux pilotes

### Test 2 : Autorisation Décollage
1. Se connecter en tant que pilote (`pilote_cmn1`)
2. Vérifier que l'avion est au sol
3. Cliquer sur "Demander Autorisation Décollage"
4. Vérifier le message VHF reçu (autorisation/refus)

### Test 3 : Dashboard Pilote
1. Se connecter en tant que pilote
2. Vérifier affichage :
   - Carte avec position avion
   - Infos de vol (vitesse, altitude, cap)
   - Météo
   - Messages VHF
3. Tester demande décollage

---

## 🔧 CONFIGURATION WEB SOCKET (Optionnel)

Pour utiliser WebSocket au lieu de polling :

```bash
cd frontend
npm install sockjs-client @stomp/stompjs
```

**Note** : Le polling (toutes les 5 secondes) fonctionne déjà très bien.

---

## 📚 DOCUMENTATION

### Fichiers de documentation créés :
- `AMELIORATIONS_BACKEND_COMPLETE.md` : Détails backend
- `RESUME_AMELIORATIONS_COMPLETE.md` : Résumé complet
- `GUIDE_CONTINUATION_AMELIORATION.md` : Guide pour continuer
- `PLAN_AMELIORATION.md` : Plan initial

---

## 🎯 CAS D'USAGE IMPLÉMENTÉS

### ✅ Cas 1 : Avion au sol
1. Pilote demande autorisation décollage
2. Radar vérifie piste libre + météo
3. Message VHF envoyé (autorisation/refus)
4. Si autorisé, statut avion → DECOLLAGE

### ✅ Cas 2 : Vol en cours
1. Positions mises à jour toutes les 5 secondes
2. Détection automatique de conflits
3. Si conflit détecté → Message VHF automatique
4. Alertes visuelles sur dashboard

### ✅ Cas 3 : Approche
1. Pilote demande autorisation atterrissage
2. Radar vérifie piste libre + météo
3. Message VHF envoyé
4. Si autorisé, statut avion → ATTERRISSAGE

---

## 🐛 DÉPANNAGE

### Problème : Pas de détection de conflits
**Solution** : Vérifier que les avions sont en statut `EN_VOL` et ont des positions GPS

### Problème : Messages VHF non reçus
**Solution** : Vérifier que le radar center existe et est lié à l'aéroport

### Problème : Dashboard pilote vide
**Solution** : Vérifier que le pilote a un avion assigné (relation pilot → aircraft)

---

## ✅ CHECKLIST FINALE

- [x] Détection automatique de conflits
- [x] Autorisations décollage/atterrissage
- [x] Dashboard pilote professionnel
- [x] Messages VHF en temps réel
- [x] Routage par rôle
- [x] WebSocket backend configuré
- [ ] Dashboard radar dédié (optionnel)
- [ ] WebSocket frontend (optionnel - dépendances)

---

**Date** : 2026  
**Statut** : ✅ **PROJET FONCTIONNEL ET PROFESSIONNEL**

**Le projet est prêt pour les tests et la démonstration !** 🎉

